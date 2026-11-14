package io.github.ryoaonetsuki.mira

import android.content.Intent
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShareIntentTest {

    @Test
    fun `normal text is converted to chat deep link`() {
        val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, "Hello world")
        val result = intentToChatDeepLink(intent)
        assertNotNull(result)
        assertEquals("mira://chat?prompt=Hello+world", result)
    }

    @Test
    fun `empty text returns null`() {
        val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, "")
        assertNull(intentToChatDeepLink(intent))
    }

    @Test
    fun `blank text returns null`() {
        val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, "   ")
        assertNull(intentToChatDeepLink(intent))
    }

    @Test
    fun `non-text action returns null`() {
        val intent = Intent(Intent.ACTION_VIEW)
        assertNull(intentToChatDeepLink(intent))
    }

    @Test
    fun `no extra text returns null`() {
        val intent = Intent(Intent.ACTION_SEND)
        assertNull(intentToChatDeepLink(intent))
    }

    @Test
    fun `long text is preserved in holder`() {
        SharedTextHolder.clear()
        val longText = "a".repeat(3000)
        val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, longText)
        val result = intentToChatDeepLink(intent)
        assertNotNull(result)
        assertTrue(result!!.startsWith("mira://chat?prompt=__shared_text__"))
        assertEquals(longText, SharedTextHolder.get())
        SharedTextHolder.clear()
    }

    @Test
    fun `short text does not use holder`() {
        SharedTextHolder.clear()
        val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, "short")
        val result = intentToChatDeepLink(intent)
        assertNotNull(result)
        assertEquals("mira://chat?prompt=short", result)
        assertNull(SharedTextHolder.get())
    }
}
