package org.runestar.cache.content.def

import io.netty.buffer.ByteBuf
import org.runestar.cache.content.load.DefinitionLoader
import org.runestar.cache.format.ReadableCache

class OverlayDefinition : CacheDefinition() {

    var color = 0
    var texture = -1
    var secondaryColor = -1
    var hideUnderlay = true

    override fun read(buffer: ByteBuf) {
        while (true) {
            val opcode = buffer.readUnsignedByte().toInt()
            when (opcode) {
                0 -> return
                1 -> color = buffer.readUnsignedMedium()
                2 -> texture = buffer.readUnsignedByte().toInt()
                5 -> hideUnderlay = false
                7 -> secondaryColor = buffer.readUnsignedMedium()
                else -> error(opcode)
            }
        }
    }

    override fun toString(): String {
        return "OverlayDefinition(color=$color, texture=$texture, secondaryColor=$secondaryColor, hideUnderlay=$hideUnderlay)"
    }

    class Loader(readableCache: ReadableCache) : DefinitionLoader.Record<OverlayDefinition>(readableCache, 2, 4) {
        override fun newDefinition() = OverlayDefinition()
    }
}