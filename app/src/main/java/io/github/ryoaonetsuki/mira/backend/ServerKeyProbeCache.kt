package io.github.ryoaonetsuki.mira.backend

/**
 * TTL cache for the server-key health probe result.
 *
 * The probe is a network call, so callers must not pay it on every key
 * resolution. Within the TTL the cached value is returned; after expiry the
 * caller re-probes. When a fresh probe FAILS (network error, non-2xx, empty
 * body), the last known value is returned instead of flipping the key source
 * on a transient error — [lastKnownOrFalse].
 */
class ServerKeyProbeCache(private val ttlMs: Long = DEFAULT_TTL_MS) {
    companion object {
        const val DEFAULT_TTL_MS: Long = 5 * 60 * 1000L
    }

    private var cachedAtMs: Long = 0L
    private var cachedValue: Boolean = false
    private var hasValue: Boolean = false

    /** Cached value if it exists and is still within the TTL, else null. */
    fun getIfFresh(nowMs: Long): Boolean? =
        if (hasValue && nowMs - cachedAtMs < ttlMs) cachedValue else null

    fun put(value: Boolean, nowMs: Long) {
        cachedValue = value
        cachedAtMs = nowMs
        hasValue = true
    }

    /** Last known value regardless of age; false when nothing was ever cached. */
    fun lastKnownOrFalse(): Boolean = cachedValue && hasValue
}
