package com.tap.hlc

import com.benasher44.uuid.Uuid
import kotlin.jvm.JvmInline

@JvmInline
value class NodeID(val identifier: String) {
    companion object {
        fun mint(uuid: Uuid = Uuid.randomUUID()) = NodeID(uuid.toString().replace("-", "").takeLast(16))
    }
}
