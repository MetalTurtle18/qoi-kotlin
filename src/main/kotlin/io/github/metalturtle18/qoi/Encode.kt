package io.github.metalturtle18.qoi

import java.awt.Color
import java.awt.image.BufferedImage

/**
 * Encodes a [BufferedImage] into a [QOIImage] based on the [QOI specification](https://qoiformat.org/qoi-specification.pdf).
 *
 * @receiver the [BufferedImage] to encode.
 */
fun BufferedImage.toQoi(): QOIImage {
    val hasAlpha = this.colorModel.hasAlpha()
    var color: Color
    var red: UByte
    var green: UByte
    var blue: UByte
    var alpha: UByte

    // Main compression loop
    for (y in 0 until height)
        for (x in 0 until width) {
            color = Color(getRGB(x, y), hasAlpha)
            red = color.red.toUByte()
            green = color.green.toUByte()
            blue = color.blue.toUByte()
            if (hasAlpha)
                alpha = color.alpha.toUByte()

            // Encode pixel
        }

    return QOIImage()
}

/**
 * A class representing a QOI image
 */
class QOIImage