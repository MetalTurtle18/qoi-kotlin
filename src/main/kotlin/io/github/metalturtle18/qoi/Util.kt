package io.github.metalturtle18.qoi

import java.awt.Color
import java.awt.image.BufferedImage
import io.github.metalturtle18.qoi.Channel.*
import io.github.metalturtle18.qoi.core.QOIImage
import io.github.metalturtle18.qoi.core.encodeIterableImage

/**
 * Create an extension [Iterator] operator for the [BufferedImage] class so that it can be formed into a byte [Sequence]
 *
 * @receiver the [BufferedImage] to return an [Iterator] for
 * @return the [Iterator]
 */
operator fun BufferedImage.iterator(): Iterator<Byte> =
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

/**
 * An enum class to store the four channel options
 */
enum class Channel { RED, GREEN, BLUE, ALPHA }

/**
 * Creates a [QOIImage] from a [BufferedImage]
 *
 * @receiver the [BufferedImage] to be encoded
 */
fun BufferedImage.toQOI() = QOIImage(width, height, colorModel.numComponents).also {
    encodeIterableImage(Sequence(this::iterator), it)
}