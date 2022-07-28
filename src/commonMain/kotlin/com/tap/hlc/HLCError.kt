package com.tap.hlc

sealed interface HLCError {
    data class DuplicateNodeError(val nodeID: NodeID) : HLCError
    data class ClockDriftError(val local: Timestamp, val now: Timestamp) : HLCError
    object CausalityOverflowError : HLCError
}
