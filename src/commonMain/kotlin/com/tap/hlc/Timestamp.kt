package com.tap.hlc

import kotlinx.datetime.Clock
import kotlin.jvm.JvmInline

@JvmInline
value class Timestamp(val epochMillis: Long) {
    companion object {
        fun now(clock: Clock) = Timestamp(clock.now().toEpochMilliseconds())
    }
}
