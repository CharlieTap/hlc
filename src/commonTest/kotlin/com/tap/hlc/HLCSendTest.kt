package com.tap.hlc

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.get
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HLCSendTest {

    private val localNode = NodeID.mint()

    private val now: Timestamp = Timestamp.now(Clock.System)

    @Test
    fun `test that when generating a local event the hlc advances to that of the current system clock`() {
        val currentCounter = 0
        val pastTimestamp = Timestamp(now.epochMillis - (1000 * 60))
        val localClock = HybridLogicalClock(pastTimestamp, localNode, currentCounter)

        val result = HybridLogicalClock.localTick(localClock, now)

        assertTrue(result.fold({ true }, { false }))
        assertEquals(result.get()?.timestamp, now)
        assertEquals(result.get()?.counter, currentCounter)
    }

    @Test
    fun `test that when generating a local event the hlc advances the counter`() {
        val currentCounter = 0
        val futureTimestamp = Timestamp(now.epochMillis + (1000 * 60))
        val localClock = HybridLogicalClock(futureTimestamp, localNode, currentCounter)

        val result = HybridLogicalClock.localTick(localClock, now)

        assertTrue(result.fold({ true }, { false }))
        assertEquals(result.get()?.timestamp, futureTimestamp)
        assertEquals(result.get()?.counter, currentCounter + 1)
    }
}
