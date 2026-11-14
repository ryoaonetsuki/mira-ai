package io.github.ryoaonetsuki.mira.backend

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class VersionCompareTest {
    @Test
    fun `1_0_0 is older than 1_0_1`() {
        assertTrue(isVersionOlder("1.0.0", "1.0.1"))
    }

    @Test
    fun `1_0_1 is not older than 1_0_1`() {
        assertFalse(isVersionOlder("1.0.1", "1.0.1"))
    }

    @Test
    fun `1_0_1-beta is older than 1_0_1`() {
        assertTrue(isVersionOlder("1.0.1-beta", "1.0.1"))
    }

    @Test
    fun `1_0_9 is older than 1_0_10`() {
        assertTrue(isVersionOlder("1.0.9", "1.0.10"))
    }

    @Test
    fun `equal versions are not older`() {
        assertFalse(isVersionOlder("1.0.0", "1.0.0"))
        assertFalse(isVersionOlder("2.1.3", "2.1.3"))
    }

    @Test
    fun `blank target never counts as older`() {
        assertFalse(isVersionOlder("1.0.0", ""))
        assertFalse(isVersionOlder("1.0.0", "   "))
    }
}
