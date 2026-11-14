package io.github.ryoaonetsuki.mira.backend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerKeyProbeCacheTest {

    @Test
    fun getIfFreshReturnsValueWithinTtl() {
        val cache = ServerKeyProbeCache(ttlMs = 300_000L)
        cache.put(true, 1_000L)
        assertEquals(true, cache.getIfFresh(1_000L + 60_000L))
    }

    @Test
    fun getIfFreshReturnsNullAfterTtl() {
        val cache = ServerKeyProbeCache(ttlMs = 300_000L)
        cache.put(true, 1_000L)
        assertNull(cache.getIfFresh(1_000L + 300_000L))
    }

    @Test
    fun getIfFreshReturnsNullWhenEmpty() {
        val cache = ServerKeyProbeCache()
        assertNull(cache.getIfFresh(1_000L))
    }

    @Test
    fun lastKnownOrFalseReturnsCachedTrueAfterExpiry() {
        val cache = ServerKeyProbeCache(ttlMs = 300_000L)
        cache.put(true, 1_000L)
        assertTrue(cache.lastKnownOrFalse())
        assertTrue(cache.lastKnownOrFalse())
    }

    @Test
    fun lastKnownOrFalseReturnsFalseWhenEmpty() {
        val cache = ServerKeyProbeCache()
        assertFalse(cache.lastKnownOrFalse())
    }

    @Test
    fun putOverwritesPreviousValue() {
        val cache = ServerKeyProbeCache(ttlMs = 300_000L)
        cache.put(true, 1_000L)
        cache.put(false, 2_000L)
        assertEquals(false, cache.getIfFresh(2_000L + 1_000L))
        assertFalse(cache.lastKnownOrFalse())
    }
}
