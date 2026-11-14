package io.github.ryoaonetsuki.mira

import android.content.Intent
import java.net.URLEncoder

object SharedTextHolder {
    private var stored: String? = null

    fun store(text: String): String {
        stored = text
        return "__shared_text__"
    }

    fun get(): String? {
        return stored
    }

    fun clear() {
        stored = null
    }
}

internal fun intentToChatDeepLink(intent: Intent): String? {
    if (intent.action != Intent.ACTION_SEND) return null
    val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
    if (text.isBlank()) return null
    val encoded = URLEncoder.encode(text, "UTF-8")
    return if (encoded.length > 2000) {
        val key = SharedTextHolder.store(text)
        "mira://chat?prompt=$key"
    } else {
        "mira://chat?prompt=$encoded"
    }
}
