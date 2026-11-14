package io.github.ryoaonetsuki.mira.voice.pipecat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnsetBufferTest {

    @Test
    fun flushReturnsFramesInArrivalOrder() {
        val buffer = OnsetBuffer()
        val first = byteArrayOf(1)
        val second = byteArrayOf(2)
        buffer.add(first, 1000L)
        buffer.add(second, 1100L)
        val flushed = buffer.flush(1200L)
        assertEquals(2, flushed.size)
        assertTrue(flushed[0] === first)
        assertTrue(flushed[1] === second)
        assertEquals(0, buffer.size())
    }

    @Test
    fun flushDropsFramesOlderThanWindow() {
        val buffer = OnsetBuffer(maxAgeMs = 500L)
        buffer.add(byteArrayOf(1), 1000L)
        buffer.add(byteArrayOf(2), 1400L)
        val flushed = buffer.flush(1600L)
        assertEquals(1, flushed.size)
        assertEquals(2.toByte(), flushed[0][0])
    }

    @Test
    fun addPrunesStaleFrames() {
        val buffer = OnsetBuffer(maxAgeMs = 500L)
        buffer.add(byteArrayOf(1), 1000L)
        buffer.add(byteArrayOf(2), 2000L)
        assertEquals(1, buffer.size())
    }

    @Test
    fun emptyFlushReturnsEmptyList() {
        val buffer = OnsetBuffer()
        assertTrue(buffer.flush(1000L).isEmpty())
        assertEquals(0, buffer.size())
    }
}
