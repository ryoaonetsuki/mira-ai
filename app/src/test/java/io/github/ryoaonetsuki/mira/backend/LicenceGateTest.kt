package io.github.ryoaonetsuki.mira.backend

import kotlin.Result
import org.junit.Assert.assertEquals
import org.junit.Test

class LicenceGateTest {

    @Test
    fun testLicenceDisabled_Pass() {
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(false, null, null))
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(false, "key", Result.success(true)))
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(false, "key", Result.success(false)))
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(false, "key", Result.failure(Exception("net"))))
    }

    @Test
    fun testLicenceEnabled_BlankKey_BlockNoKey() {
        assertEquals(LicenceGateOutcome.BLOCK_NO_KEY, resolveLicenceGate(true, null, null))
        assertEquals(LicenceGateOutcome.BLOCK_NO_KEY, resolveLicenceGate(true, "", null))
        assertEquals(LicenceGateOutcome.BLOCK_NO_KEY, resolveLicenceGate(true, "   ", null))
        assertEquals(LicenceGateOutcome.BLOCK_NO_KEY, resolveLicenceGate(true, null, Result.failure(Exception("net"))))
    }

    @Test
    fun testLicenceEnabled_KeyPresent_Valid_Pass() {
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(true, "abc", Result.success(true)))
    }

    @Test
    fun testLicenceEnabled_KeyPresent_Invalid_BlockInvalid() {
        assertEquals(LicenceGateOutcome.BLOCK_INVALID, resolveLicenceGate(true, "abc", Result.success(false)))
    }

    @Test
    fun testLicenceEnabled_KeyPresent_Failure_Pass() {
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(true, "abc", Result.failure(Exception("network"))))
    }

    @Test
    fun testLicenceEnabled_KeyPresent_NullValidation_Pass() {
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(true, "abc", null))
    }

    @Test
    fun testSplashBranchMatchesLicenceGateOutcome() {
        // BLOCK_NO_KEY -> Splash shows "Licence Required"
        assertEquals(LicenceGateOutcome.BLOCK_NO_KEY, resolveLicenceGate(true, null, null))
        assertEquals(LicenceGateOutcome.BLOCK_NO_KEY, resolveLicenceGate(true, "", null))
        assertEquals(LicenceGateOutcome.BLOCK_NO_KEY, resolveLicenceGate(true, "   ", null))
        // BLOCK_INVALID -> Splash shows "Licence Invalid"
        assertEquals(LicenceGateOutcome.BLOCK_INVALID, resolveLicenceGate(true, "abc", Result.success(false)))
        // PASS -> Splash continues to setup/home (fail-open on failure / null)
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(true, "abc", Result.success(true)))
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(true, "abc", Result.failure(Exception("net"))))
        assertEquals(LicenceGateOutcome.PASS, resolveLicenceGate(true, "abc", null))
    }
}
