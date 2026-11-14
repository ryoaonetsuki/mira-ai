package io.github.ryoaonetsuki.mira.backend

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ApiKeyResolverTest {

    @Test
    fun testUseProxySentinelConstant() {
        assertEquals("__MIRA_USE_PROXY__", ApiKeyResolver.USE_PROXY_SENTINEL)
    }

    // The probe cache's TTL + failure-fallback behaviour is covered by
    // ServerKeyProbeCacheTest (pure unit tests); resolveKey()/resolveKeyOrProxy()
    // need an Android Context + network and are covered by CI, not here.
}
