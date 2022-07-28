package com.tap.hlc

import com.github.michaelbull.result.*
import kotlinx.datetime.Clock
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

/**
 * Implementation of a HLC [1][2]
 *
 * Largely a rip of Jared Forsyth's impl[3] with some kotlinisms
 *
 * [1]: https://cse.buffalo.edu/tech-reports/2014-04.pdf
 * [2]: https://muratbuffalo.blogspot.com/2014/07/hybrid-logical-clocks.html
 * [3]: https://jaredforsyth.com/posts/hybrid-logical-clocks/
 */
data class HybridLogicalClock(val timestamp: Timestamp = Timestamp.now(Clock.System), val node: NodeID = NodeID.mint(), val counter: Int = 0) : Comparable<HybridLogicalClock> {

    companion object {

        // Call this every time a new event is generated on the node, set the local clock and the events timestamp equal to the result
        fun increment(local: HybridLogicalClock, wallClockTime: Timestamp = Timestamp.now(Clock.System), maxClockDrift: Int = 1000 * 60): Result<HybridLogicalClock, HLCError> {
            return if (wallClockTime.epochMillis > local.timestamp.epochMillis) {
                Ok(local.copy(timestamp = wallClockTime))
            } else Ok(local.copy(counter = local.counter + 1)).flatMap { clock ->
                validate(clock, wallClockTime, maxClockDrift)
            }
        }

        // Call this on all events from external nodes to create a new local hlc which factors in the remote event
        fun receive(local: HybridLogicalClock, remote: HybridLogicalClock, wallClockTime: Timestamp = Timestamp.now(Clock.System), maxClockDrift: Int = 1000 * 60): Result<HybridLogicalClock, HLCError> {
            return when {
                local.node.identifier == remote.node.identifier -> {
                    Err(HLCError.DuplicateNodeError(local.node))
                }
                wallClockTime.epochMillis > local.timestamp.epochMillis &&
                    wallClockTime.epochMillis > remote.timestamp.epochMillis -> {
                    Ok(local.copy(timestamp = wallClockTime, counter = 0))
                }
                local.timestamp.epochMillis == remote.timestamp.epochMillis -> {
                    Ok(local.copy(counter = max(local.counter, remote.counter) + 1))
                }
                local.timestamp.epochMillis > remote.timestamp.epochMillis -> {
                    Ok(local.copy(counter = local.counter + 1))
                }
                else -> {
                    Ok(local.copy(timestamp = remote.timestamp, counter = remote.counter + 1))
                }
            }.flatMap { clock ->
                validate(clock, wallClockTime, maxClockDrift)
            }
        }

        private fun validate(clock: HybridLogicalClock, now: Timestamp, maxClockDrift: Int): Result<HybridLogicalClock, HLCError> {
            if (clock.counter > 36f.pow(5).toInt()) {
                return Err(HLCError.CausalityOverflowError)
            }

            if (abs(clock.timestamp.epochMillis - now.epochMillis) > maxClockDrift) {
                return Err(HLCError.ClockDriftError(clock.timestamp, now))
            }

            return Ok(clock)
        }

        fun encodeToString(hlc: HybridLogicalClock): String {
            return with(hlc) {
                "${timestamp.epochMillis.toString().padStart(15, '0')}:${counter.toString(36).padStart(5, '0')}:${node.identifier}"
            }
        }

        fun decodeFromString(encoded: String): Result<HybridLogicalClock, HLCDecodeError> {
            val parts = encoded.split(":")

            val timestamp = parts.firstOrNull()?.let {
                Timestamp(it.toLong())
            } ?: return Err(HLCDecodeError.TimestampDecodeFailure(encoded))

            val counter = parts.getOrNull(1)?.toInt(36) ?: return Err(HLCDecodeError.CounterDecodeFailure(encoded))
            val node = parts.getOrNull(2)?.let { NodeID(it) } ?: return Err(HLCDecodeError.NodeDecodeFailure(encoded))

            return Ok(HybridLogicalClock(timestamp, node, counter))
        }
    }

    override fun compareTo(other: HybridLogicalClock): Int {
        return if (timestamp.epochMillis == other.timestamp.epochMillis) {
            if (counter == other.counter) {
                node.identifier.compareTo(other.node.identifier)
            } else {
                counter - other.counter
            }
        } else {
            timestamp.epochMillis.compareTo(other.timestamp.epochMillis)
        }
    }
}
