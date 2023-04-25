package com.tap.hlc

import com.benasher44.uuid.uuid4
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HLCPersistTest {

    @Test
    fun `can store the hlc into a file at a given path`() {
        val fileSystem = FakeFileSystem()

        val epochMillis = 943920000000L
        val counter = 15
        val node = uuid4()

        val clock = HybridLogicalClock(Timestamp(epochMillis), NodeID.mint(node), counter)
        val path = "/Users/alice".toPath()
        val filename = "test.hlc"

        HybridLogicalClock.store(clock, path, fileSystem, filename)

        val expectedEncoded = "${epochMillis.toString().padStart(15, '0')}:${counter.toString(36).padStart(5, '0')}:${node.toString().replace("-", "").takeLast(16)}"
        val result = fileSystem.read(path / filename) {
            readUtf8()
        }

        assertEquals(expectedEncoded, result)
        fileSystem.checkNoOpenFiles()
    }

    @Test
    fun `can load a hlc from a given path`() {
        val fileSystem = FakeFileSystem()
        val path = "/Users/alice".toPath()
        fileSystem.createDirectories(path)
        val filename = "test.hlc"

        val epochMillis = 943920000000L
        val counter = 15
        val node = uuid4().toString().replace("-", "").takeLast(16)

        val encoded = "${epochMillis.toString().padStart(15, '0')}:${counter.toString(36).padStart(5, '0')}:$node"

        fileSystem.write(path / filename) {
            writeUtf8(encoded)
        }

        val result = HybridLogicalClock.load(path, fileSystem, filename)

        assertNotNull(result)
        assertEquals(result.timestamp.epochMillis, epochMillis)
        assertEquals(result.counter, counter)
        assertEquals(result.node.identifier, node)
        fileSystem.checkNoOpenFiles()
    }

    @Test
    fun `can store and load a hlc to and from a given path`() {
        val fileSystem = FakeFileSystem()
        val path = "/Users/alice".toPath()
        fileSystem.createDirectories(path)
        val filename = "test.hlc"

        val epochMillis = 943920000000L
        val counter = 15
        val node = uuid4()

        val clock = HybridLogicalClock(Timestamp(epochMillis), NodeID.mint(node), counter)
        HybridLogicalClock.store(clock, path, fileSystem, filename)
        val result = HybridLogicalClock.load(path, fileSystem, filename)

        assertNotNull(result)
        assertEquals(result.timestamp.epochMillis, epochMillis)
        assertEquals(result.counter, counter)
        assertEquals(result.node.identifier, NodeID.mint(node).identifier)
        fileSystem.checkNoOpenFiles()
    }
}
