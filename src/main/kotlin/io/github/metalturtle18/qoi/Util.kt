package io.github.metalturtle18.qoi

import java.awt.Color
import java.awt.image.BufferedImage
import io.github.metalturtle18.qoi.Channel.*
import java.awt.image.ColorModel

/**
 * A wrapper class for a buffered image that implements [Iterable]
 *
 * This class exists because creating an extension operator function does not allow the [BufferedImage] class to be passed as a type parameter that implements iterable, even if it effectively does in this program
 *
 * @param original The original [BufferedImage] to be contained in this class
 */
class IterableBufferedImage(private val original: BufferedImage) : Iterable<Byte> {
    val colorModel: ColorModel get() = original.colorModel
    val width: Int get() = original.width
    val height: Int get() = original.height
    fun getRGB(x: Int, y: Int) = original.getRGB(x, y)

    override operator fun iterator(): Iterator<Byte> =
        object : Iterator<Byte> {
            val channels = colorModel.numComponents
            val numBytes = width * height * channels
            var nextToReturn = 0
            var nextChannel = RED

            override fun hasNext() = nextToReturn < numBytes

            override fun next(): Byte {
                val x = nextToReturn / channels % width
                val y = nextToReturn / channels / width
                val color = Color(getRGB(x, y), colorModel.hasAlpha())
                val byte = with(color) {
                    when (nextChannel) {
                        RED -> red
                        GREEN -> green
                        BLUE -> blue
                        ALPHA -> alpha
                    }
                }.toByte()

                nextToReturn++
                nextChannel = when (nextChannel) {
                    RED -> GREEN
                    GREEN -> BLUE
                    BLUE -> if (colorModel.hasAlpha()) ALPHA else RED
                    ALPHA -> RED
                }
                return byte
            }
        }
}

/**
 * An enum class to store the four channel options
 */
enum class Channel { RED, GREEN, BLUE, ALPHA }