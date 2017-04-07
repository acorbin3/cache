package com.runesuite.cache.fs

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import mu.KotlinLogging
import java.io.Closeable
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class BufFile(val file: Path) : AutoCloseable, Closeable {

    private val logger = KotlinLogging.logger {  }

    private val fileChannel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)

    private val mappedByteBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileChannel.size())

    val buffer: ByteBuf = Unpooled.wrappedBuffer(mappedByteBuffer.slice())

    init {
        check(fileChannel.size().toInt() == buffer.readableBytes())
        logger.debug { "${file.fileName} loaded. Size: ${buffer.readableBytes()}" }
    }

    override fun close() {
        fileChannel.close()
    }
}