package com.runesuite.cache.format.net

import com.runesuite.cache.format.Archive
import io.netty.buffer.ByteBuf

class FileResponse(override val input: ByteBuf) : Response(input) {

    companion object {
        const val HEADER_LENGTH = java.lang.Byte.BYTES + java.lang.Short.BYTES
    }

    val index = input.getUnsignedByte(0).toInt()

    val archive = input.getUnsignedShort(1)

    private val archiveSlice = input.slice().skipBytes(HEADER_LENGTH)

    val done = Archive.isValid(archiveSlice)

    val data: Archive by lazy {
        check(done)
        Archive(archiveSlice)
    }

    override fun toString(): String {
        return "FileResponse(index=$index, archive=$archive)"
    }
}