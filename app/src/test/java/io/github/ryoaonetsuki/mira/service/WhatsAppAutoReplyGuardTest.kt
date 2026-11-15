package io.github.ryoaonetsuki.mira.service

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import io.github.ryoaonetsuki.mira.data.local.dao.WhatsAppGuardDao
import io.github.ryoaonetsuki.mira.data.local.entities.WhatsAppGuardEntity

class FakeWhatsAppGuardDao : WhatsAppGuardDao {
    private val store = mutableMapOf<String, WhatsAppGuardEntity>()

    override suspend fun insertOrUpdate(entity: WhatsAppGuardEntity) {
        val key = "${entity.conversation}|${entity.latestIncomingText}|${entity.notificationKey}"
        store[key] = entity
    }

    override suspend fun findByKey(conversation: String, latestIncomingText: String, notificationKey: String): WhatsAppGuardEntity? {
        val key = "$conversation|$latestIncomingText|$notificationKey"
        return store[key]
    }

    override suspend fun findByConversation(conversation: String): List<WhatsAppGuardEntity> {
        return store.values.filter { it.conversation == conversation }.toList()
    }

    override suspend fun findAll(): List<WhatsAppGuardEntity> {
        return store.values.toList()
    }

    override suspend fun pruneOlderThan(cutoff: Long) {
        store.entries.removeIf { (_, v) -> v.lastRepliedAt < cutoff }
    }

    override suspend fun deleteAll() {
        store.clear()
    }
}

class WhatsAppAutoReplyGuardTest {

    private fun newGuard(): WhatsAppAutoReplyGuard {
        return WhatsAppAutoReplyGuard(FakeWhatsAppGuardDao())
    }

    @Test
    fun duplicateSameMessage_repliesOnce() = runBlocking {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        assertTrue(guard.canReply("conv1", "Hello", "Template", "key1", now, now))
        guard.recordReply("conv1", "Hello", "key1", now)
        assertFalse(guard.canReply("conv1", "Hello", "Template", "key1", now, now))
    }

    @Test
    fun templateText_neverReplies() {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        assertFalse(guard.canReply("conv1", "Template", "Template", "key1", now, now))
    }

    @Test
    fun blankTemplate_neverReplies() {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        assertFalse(guard.canReply("conv1", "Hello", "", "key1", now, now))
        assertFalse(guard.canReply("conv1", "Hello", "   ", "key1", now, now))
    }

    @Test
    fun ownMessage_neverReplies() {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        assertFalse(guard.canReply("conv1", null, "Template", "key1", now, now))
        assertFalse(guard.canReply("conv1", "", "Template", "key1", now, now))
        assertFalse(guard.canReply("conv1", "   ", "Template", "key1", now, now))
    }

    @Test
    fun twoDifferentConversations_bothReply() {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        assertTrue(guard.canReply("conv1", "Hello", "Template", "key1", now, now))
        assertTrue(guard.canReply("conv2", "Hi", "Template", "key2", now, now))
    }

    @Test
    fun rateLimit_blocksSecondMessageWithin60s() = runBlocking {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        assertTrue(guard.canReply("conv1", "Hello", "Template", "key1", now, now))
        guard.recordReply("conv1", "Hello", "key1", now)
        assertFalse(guard.canReply("conv1", "Hello again", "Template", "key2", now, now + 10_000))
    }

    @Test
    fun newMessageAfter60s_repliesAgain() = runBlocking {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        assertTrue(guard.canReply("conv1", "Hello", "Template", "key1", now, now))
        guard.recordReply("conv1", "Hello", "key1", now)
        assertTrue(guard.canReply("conv1", "Hello again", "Template", "key2", now, now + 61_000))
    }

    @Test
    fun staleNotification_neverReplies() {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        assertFalse(guard.canReply("conv1", "Hello", "Template", "key1", now - 121_000, now))
    }

    @Test
    fun sameMessageAfterDedupWindow_repliesAgain() = runBlocking {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        assertTrue(guard.canReply("conv1", "Hello", "Template", "key1", now, now))
        guard.recordReply("conv1", "Hello", "key1", now)
        // Fresh re-post 301s later: fresh postTime (recency passes), dedup window expired.
        assertTrue(guard.canReply("conv1", "Hello", "Template", "key1", now + 301_000, now + 301_000))
    }

    // --- Round 4 additions ---

    @Test
    fun noReplyAction_nothingRecorded_secondNotificationStillReplyable() = runBlocking {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        // canReply passes but recordReply is never called (simulating no reply action found)
        assertTrue(guard.canReply("conv1", "Hello", "Template", "key1", now, now))
        // Second identical notification should still be replyable because nothing was recorded
        assertTrue(guard.canReply("conv1", "Hello", "Template", "key1", now, now))
    }

    @Test
    fun textContainingTemplate_neverReplies() {
        val guard = newGuard()
        val now = System.currentTimeMillis()
        // Latest text contains template as substring (e.g. "You: Template")
        assertFalse(guard.canReply("conv1", "You: Template", "Template", "key1", now, now))
        // Template contains latest text as substring
        assertFalse(guard.canReply("conv1", "Templ", "Template", "key1", now, now))
    }

    @Test
    fun persistedStateSurvivesGuardRecreation() = runBlocking {
        val dao = FakeWhatsAppGuardDao()
        val guard1 = WhatsAppAutoReplyGuard(dao)
        val now = System.currentTimeMillis()
        assertTrue(guard1.canReply("conv1", "Hello", "Template", "key1", now, now))
        guard1.recordReply("conv1", "Hello", "key1", now)

        // New guard with same DAO, load persisted state
        val guard2 = WhatsAppAutoReplyGuard(dao)
        guard2.load()
        assertFalse(guard2.canReply("conv1", "Hello", "Template", "key1", now, now))
    }
}
