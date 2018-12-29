package uk.tvidal.kraft.buffer

import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileChannel.MapMode.READ_WRITE
import java.nio.file.Files.newByteChannel
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE

private val options = setOf(CREATE, READ, WRITE)

internal fun openMemoryMappedFile(file: File, size: Long): ByteBuffer {
    val path = file.toPath()
    val channel = newByteChannel(path, options) as FileChannel
    return channel.use {
        it.map(READ_WRITE, 0, size)
    }
}
