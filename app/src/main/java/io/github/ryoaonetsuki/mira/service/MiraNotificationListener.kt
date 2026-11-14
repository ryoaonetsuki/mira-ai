package io.github.ryoaonetsuki.mira.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import io.github.ryoaonetsuki.mira.data.local.MiraDatabase
import io.github.ryoaonetsuki.mira.data.local.dao.WhatsAppGuardDao
import io.github.ryoaonetsuki.mira.data.local.entities.NotificationEntity
import io.github.ryoaonetsuki.mira.data.local.entities.WhatsAppGuardEntity
import io.github.ryoaonetsuki.mira.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MiraNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var guard: WhatsAppAutoReplyGuard

    override fun onCreate() {
        super.onCreate()
        val db = MiraDatabase.getInstance(applicationContext)
        guard = WhatsAppAutoReplyGuard(db.whatsAppGuardDao())
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        scope.launch {
            guard.load()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        if (packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b") {
            handleWhatsAppNotification(sbn)
        }
    }

    private fun handleWhatsAppNotification(sbn: StatusBarNotification) {
        scope.launch {
            try {
                val userPrefs = UserPreferences(applicationContext)
                val isAutoReplyEnabled = userPrefs.whatsappAutoReplyFlow.first()
                if (!isAutoReplyEnabled) return@launch

                val notification = sbn.notification ?: return@launch
                val extras = notification.extras ?: return@launch
                val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

                if (title.isBlank() || text.isBlank()) return@launch

                // Skip group notifications — auto-replying into a group is wrong (1:1 only)
                // (Framework MessagingStyle.extractMessagingStyleFromNotification is a hidden
                // API, so use the public AndroidX equivalent.)
                val messagingStyle = androidx.core.app.NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
                if (messagingStyle?.isGroupConversation == true) {
                    Log.w(TAG, "Skipping group conversation notification: $title")
                    return@launch
                }
                if (sbn.isGroup) {
                    Log.w(TAG, "Skipping group notification (sbn.isGroup): $title")
                    return@launch
                }
                if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
                    Log.w(TAG, "Skipping group summary notification (FLAG_GROUP_SUMMARY): $title")
                    return@launch
                }

                val template = userPrefs.autoReplyTemplateFlow.first()

                // Derive latestIncomingText from EXTRA_MESSAGES, fall back to EXTRA_TEXT
                val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)?.filterIsInstance<Notification.MessagingStyle.Message>() ?: emptyList()
                val latestIncomingText: String? = if (messages.isNotEmpty()) {
                    val last = messages.last()
                    if (last.sender == null) null else last.text?.toString()
                } else {
                    text
                }

                // Skip if latest text contains template or template contains latest text (echo/self-sent)
                val trimmedText = latestIncomingText?.trim()
                val trimmedTemplate = template.trim()
                if (trimmedText.isNullOrBlank()) return@launch
                if (trimmedTemplate.isBlank()) return@launch
                if (trimmedText.contains(trimmedTemplate) || trimmedTemplate.contains(trimmedText)) return@launch

                val now = System.currentTimeMillis()

                // Pure check — does not record state
                if (!guard.canReply(title, latestIncomingText, template, sbn.key, sbn.postTime, now)) return@launch

                // Look for wearable action / quick reply action with RemoteInput
                val actions = notification.actions ?: return@launch
                var replySent = false
                for (action in actions) {
                    val remoteInputs = action.remoteInputs
                    if (remoteInputs != null && remoteInputs.isNotEmpty()) {
                        for (remoteInput in remoteInputs) {
                            val replyIntent = Intent()
                            val bundle = Bundle()
                            bundle.putCharSequence(remoteInput.resultKey, template)
                            RemoteInput.addResultsToIntent(arrayOf(remoteInput), replyIntent, bundle)

                            action.actionIntent.send(applicationContext, 0, replyIntent)
                            Log.i(TAG, "Sent WhatsApp auto-reply to '$title': $template")
                            replySent = true
                            break
                        }
                    }
                    if (replySent) break
                }

                if (!replySent) {
                    Log.w(TAG, "No reply action with RemoteInput found for notification '$title' — nothing recorded")
                    return@launch
                }

                // Record only after the reply was actually sent
                guard.recordReply(title, latestIncomingText, sbn.key, now)

                // Save notification log to Room
                val db = MiraDatabase.getInstance(applicationContext)
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        title = "WhatsApp Auto-Reply",
                        message = "Replied to $title: \"$template\"",
                        type = "info"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to auto-reply to WhatsApp notification: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "MiraNotifListener"
    }
}

class WhatsAppAutoReplyGuard(private val dao: WhatsAppGuardDao) {
    companion object {
        const val RATE_LIMIT_MS = 60_000L
        const val DEDUP_WINDOW_MS = 300_000L
        const val RECENCY_MS = 120_000L
    }

    private val repliedKeys = mutableMapOf<String, Long>()
    private val conversationLastReply = mutableMapOf<String, Long>()

    suspend fun load() {
        val now = System.currentTimeMillis()
        val cutoff = now - DEDUP_WINDOW_MS
        dao.pruneOlderThan(cutoff)
        val all = dao.findAll()
        for (entity in all) {
            val key = "${entity.conversation}|${entity.latestIncomingText}|${entity.notificationKey}"
            repliedKeys[key] = entity.lastRepliedAt
            val current = conversationLastReply[entity.conversation] ?: 0L
            if (entity.lastRepliedAt > current) {
                conversationLastReply[entity.conversation] = entity.lastRepliedAt
            }
        }
    }

    fun canReply(
        conversation: String,
        latestIncomingText: String?,
        template: String,
        notificationKey: String,
        postTimeMs: Long,
        nowMs: Long
    ): Boolean {
        // (a) latestIncomingText null/blank → false
        if (latestIncomingText.isNullOrBlank()) return false
        // (b) blank template → false
        if (template.isBlank()) return false
        // (c) latestIncomingText contains template or template contains latestIncomingText → false
        val trimmedText = latestIncomingText.trim()
        val trimmedTemplate = template.trim()
        if (trimmedText.contains(trimmedTemplate) || trimmedTemplate.contains(trimmedText)) return false
        // (d) stale notification → false
        if (nowMs - postTimeMs > RECENCY_MS || postTimeMs > nowMs) return false

        val key = "$conversation|$trimmedText|$notificationKey"

        return synchronized(this) {
            // (e) dedup: same key replied within DEDUP_WINDOW_MS → false
            val lastReplied = repliedKeys[key]
            if (lastReplied != null && nowMs - lastReplied <= DEDUP_WINDOW_MS) return false

            // (f) rate limit: conversation replied within RATE_LIMIT_MS → false
            val lastConversationReply = conversationLastReply[conversation]
            if (lastConversationReply != null && nowMs - lastConversationReply <= RATE_LIMIT_MS) return false

            true
        }
    }

    suspend fun recordReply(
        conversation: String,
        latestIncomingText: String?,
        notificationKey: String,
        nowMs: Long
    ) {
        if (latestIncomingText.isNullOrBlank()) return

        val trimmedText = latestIncomingText.trim()
        val key = "$conversation|$trimmedText|$notificationKey"

        synchronized(this) {
            repliedKeys[key] = nowMs
            val current = conversationLastReply[conversation] ?: 0L
            if (nowMs > current) {
                conversationLastReply[conversation] = nowMs
            }
        }

        dao.insertOrUpdate(
            WhatsAppGuardEntity(
                conversation = conversation,
                latestIncomingText = trimmedText,
                notificationKey = notificationKey,
                lastRepliedAt = nowMs
            )
        )
    }
}
