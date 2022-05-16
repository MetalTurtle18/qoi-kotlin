package io.github.metalturtle18.qoi

import java.awt.Color
import java.awt.image.BufferedImage

/**
 * Encodes a [BufferedImage] into a [QOIImage] based on the [QOI specification](https://qoiformat.org/qoi-specification.pdf).
 *
 * @receiver the [BufferedImage] to encode.
 * @return the [QOIImage] that was encoded.
 */
fun BufferedImage.toQoi(): QOIImage {
    val hasAlpha = this.colorModel.hasAlpha()
    val image = QOIImage(
        width = width,
        height = height,
        channels = if (hasAlpha) 4 else 3
    )


    var color: Color
    var R: Byte
    var G: Byte
    var B: Byte
    var A: Byte

    var prevR: Byte = 0
    var prevG: Byte = 0
    var prevB: Byte = 0
    var prevA: Byte = 255.toByte()

    // Header
    image.writeData('q'.code.toByte(), 'o'.code.toByte(), 'i'.code.toByte(), 'f'.code.toByte()) // magic bytes
    image.writeData(
        (width shr 24 and 0xFF).toByte(),
        (width shr 16 and 0xFF).toByte(),
        (width shr 8 and 0xFF).toByte(),
        (width and 0xFF).toByte()
    ) // width as 32 bit unsigned int
    image.writeData(
        (height shr 24 and 0xFF).toByte(),
        (height shr 16 and 0xFF).toByte(),
        (height shr 8 and 0xFF).toByte(),
        (height and 0xFF).toByte()
    ) // height as 32 bit unsigned int
    image.writeData(if (hasAlpha) 4 else 3) // number of channels
    image.writeData() // TODO: color space stuff


    // Main compression loop
    for (y in 0 until height)
        for (x in 0 until width) {
            color = Color(getRGB(x, y), hasAlpha)
            R = color.red.toByte()
            G = color.green.toByte()
            B = color.blue.toByte()
            if (hasAlpha)
                A = color.alpha.toByte()

            // Encode pixel
            TODO("Write encoding algorithm")
        }

    return image
}

