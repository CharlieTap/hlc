package com.tap.hlc

sealed interface HLCDecodeError {
    data class TimestampDecodeFailure(val encodedClock: String) : HLCDecodeError
    data class CounterDecodeFailure(val encodedClock: String) : HLCDecodeError
    data class NodeDecodeFailure(val encodedClock: String) : HLCDecodeError
}
