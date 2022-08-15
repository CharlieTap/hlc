package com.tap.hlc

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import kotlinx.datetime.Clock
import kotlin.math.max
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HLCReceiveTest {

    private val localNode = NodeID.mint()
    private val remoteNode = NodeID.mint()

    private val now: Timestamp = Timestamp.now(Clock.System)

    @Test
    fun `test that when receiving a hlc with a duplicate node id an error is thrown`() {
        val localClock = HybridLogicalClock(now, localNode)
        val remoteClock = HybridLogicalClock(now, localNode)

        val result = HybridLogicalClock.remoteTock(localClock, remoteClock, now)

        assertFalse(result.fold({ true }, { false }))
        assertEquals(HLCError.DuplicateNodeError(localNode), result.getError())
    }

    @Test
    fun `test that when receiving a hlc with a timestamp that has drifted an error is thrown`() {
        // Only forward drift from remote matters
        val driftedTimestamp = Timestamp(now.epochMillis + (1000 * 60 * 60))

        val localClock = HybridLogicalClock(now, localNode)
        val remoteClock = HybridLogicalClock(driftedTimestamp, remoteNode)

        val result = HybridLogicalClock.remoteTock(localClock, remoteClock, now)

        assertFalse(result.fold({ true }, { false }))
        assertEquals(HLCError.ClockDriftError(remoteClock.timestamp, now), result.getError())
    }

    @Test
    fun `test that when receiving a hlc with a counter that is too large an error is throw`() {
        val localClock = HybridLogicalClock(now, localNode)
        val remoteClock = HybridLogicalClock(now, remoteNode, (36f.pow(5) + 1).toInt())

        val result = HybridLogicalClock.remoteTock(localClock, remoteClock, now)

        assertFalse(result.fold({ true }, { false }))
        assertEquals(HLCError.CausalityOverflowError, result.getError())
    }

    @Test
    fun `test when receiving a hlc for a future event inside acceptable drift the timestamp is updated`() {
        val futureTimestamp = Timestamp(now.epochMillis + (1000 * 59))

        val localClock = HybridLogicalClock(now, localNode)
        val remoteClock = HybridLogicalClock(futureTimestamp, remoteNode)

        val result = HybridLogicalClock.remoteTock(localClock, remoteClock, now)

        assertTrue(result.fold({ true }, { false }))
        assertEquals(localNode, result.get()?.node)
        assertEquals(futureTimestamp, result.get()?.timestamp)
    }

    @Test
    fun `test that when receiving a hlc for an event in the past the local nodes counter is incremented`() {
        val pastTimestamp = Timestamp(now.epochMillis - (1000 * 60 * 60))

        val localClock = HybridLogicalClock(now, localNode, 1)
        val remoteClock = HybridLogicalClock(pastTimestamp, remoteNode, 3)

        val result = HybridLogicalClock.remoteTock(localClock, remoteClock, now)

        assertTrue(result.fold({ true }, { false }))
        assertEquals(localNode, result.get()?.node)
        assertEquals(now, result.get()?.timestamp)
        assertEquals(localClock.counter + 1, result.get()?.counter)
    }

    @Test
    fun `test that when receiving a hlc for an event in the future the remote nodes counter is incremented`() {
        val futureTimestamp = Timestamp(now.epochMillis + (1000 * 59))

        val localClock = HybridLogicalClock(now, localNode, 1)
        val remoteClock = HybridLogicalClock(futureTimestamp, remoteNode, 3)

        val result = HybridLogicalClock.remoteTock(localClock, remoteClock, now)

        assertTrue(result.fold({ true }, { false }))
        assertEquals(localNode, result.get()?.node)
        assertEquals(futureTimestamp, result.get()?.timestamp)
        assertEquals(remoteClock.counter + 1, result.get()?.counter)
    }

    @Test
    fun `test that when receiving a hlc with a matching timestamp the largest counter is incremented`() {
        val localClock = HybridLogicalClock(now, localNode, 1)
        val remoteClock = HybridLogicalClock(now, remoteNode, 3)

        val result = HybridLogicalClock.remoteTock(localClock, remoteClock, now)

        assertTrue(result.fold({ true }, { false }))
        assertEquals(localNode, result.get()?.node)
        assertEquals(now, result.get()?.timestamp)
        assertEquals(max(localClock.counter, remoteClock.counter) + 1, result.get()?.counter)
    }

    @Test
    fun `test that when both hlcs are behind the wall clock the counter is reset`() {
        val pastTimestamp = Timestamp(now.epochMillis - (1000 * 60 * 60))

        val localClock = HybridLogicalClock(pastTimestamp, localNode, 1)
        val remoteClock = HybridLogicalClock(pastTimestamp, remoteNode, 3)

        val result = HybridLogicalClock.remoteTock(localClock, remoteClock, now)

        assertTrue(result.fold({ true }, { false }))
        assertEquals(localNode, result.get()?.node)
        assertEquals(now, result.get()?.timestamp)
        assertEquals(0, result.get()?.counter)
    }
}
