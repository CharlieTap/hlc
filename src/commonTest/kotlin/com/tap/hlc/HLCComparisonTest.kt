package com.tap.hlc

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class HLCComparisonTest {

    private val localNode = NodeID.mint()
    private val remoteNode = NodeID.mint()

    private val now: Timestamp = Timestamp.now(Clock.System)
    private val earlier: Timestamp = Timestamp(now.epochMillis - (1000 * 60 * 60))

    @Test
    fun `test that when the local clocks timestamp is later comparing them returns a positive integer`() {
        val localClock = HybridLogicalClock(now, localNode)
        val remoteClock = HybridLogicalClock(earlier, remoteNode)

        assertTrue(localClock > remoteClock)
    }

    @Test
    fun `test that when the local clocks timestamp is earlier comparing them returns a negative integer`() {
        val localClock = HybridLogicalClock(earlier, localNode)
        val remoteClock = HybridLogicalClock(now, remoteNode)

        assertTrue(localClock < remoteClock)
    }

    @Test
    fun `test that when the local clocks timestamp is identical and counter is greater it returns a positive integer`() {
        val localClock = HybridLogicalClock(now, localNode, 1)
        val remoteClock = HybridLogicalClock(now, remoteNode)

        assertTrue(localClock > remoteClock)
    }

    @Test
    fun `test that when the local clocks timestamp is identical and counter is lesser it returns a negative integer`() {
        val localClock = HybridLogicalClock(now, localNode)
        val remoteClock = HybridLogicalClock(now, remoteNode, 1)

        assertTrue(localClock < remoteClock)
    }

    @Test
    fun `test that when the local clocks timestamp is identical and counter is equal it never returns 0`() {
        val localClock = HybridLogicalClock(now, localNode)
        val remoteClock = HybridLogicalClock(now, remoteNode)

        assertNotEquals(0, localClock.compareTo(remoteClock))
    }
}
