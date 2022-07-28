package com.tap.hlc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeIDTest {

    @Test
    fun `test minting a new node id is a success`() {
        val node = NodeID.mint()

        assertEquals(node.identifier.length, 16)
        assertTrue(node.identifier.contains("-").not())
    }
}
