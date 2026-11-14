package io.github.ryoaonetsuki.mira.voice.pipecat

import java.util.LinkedList

/**
 * Small ring buffer that preserves speech-onset frames arriving while the
 * EchoGate is still classifying them as echo. When the gate opens, the
 * buffered frames are flushed in order so the start of the user's speech
 * is not eaten. Frames older than [maxAgeMs] are dropped as stale.
 */
class OnsetBuffer(private val maxAgeMs: Long = 500L) {
    private val frames = LinkedList<Pair<ByteArray, Long>>()

    fun add(frame: ByteArray, nowMs: Long) {
        prune(nowMs)
        frames.addLast(frame to nowMs)
    }

    /** Removes and returns all buffered frames in arrival order. */
    fun flush(nowMs: Long): List<ByteArray> {
        prune(nowMs)
        val out = frames.map { it.first }
        frames.clear()
        return out
    }

    fun size(): Int = frames.size

    private fun prune(nowMs: Long) {
        while (frames.isNotEmpty() && nowMs - frames.first().second > maxAgeMs) {
            frames.removeFirst()
        }
    }
}
