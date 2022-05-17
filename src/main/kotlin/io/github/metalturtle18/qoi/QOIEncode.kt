package io.github.metalturtle18.qoi

import java.awt.Color
import java.awt.image.BufferedImage

val QOI_OP_INDEX = 0x00


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

    // Temporary variables for encoding
    var color: Color
    var R: Byte = 0
    var G: Byte = 0
    var B: Byte = 0
    var A: Byte = 0

    var prevR: Byte = 0
    var prevG: Byte = 0
    var prevB: Byte = 0
    var prevA: Byte = 255.toByte()

    val array = IntArray(64)

    // Header
    image.writeBytes('q'.code.toByte(), 'o'.code.toByte(), 'i'.code.toByte(), 'f'.code.toByte()) // magic bytes
    image.writeBytes(
        (width shr 24 and 0xFF).toByte(),
        (width shr 16 and 0xFF).toByte(),
        (width shr 8 and 0xFF).toByte(),
        (width and 0xFF).toByte()
    ) // width as 32 bit unsigned int
    image.writeBytes(
        (height shr 24 and 0xFF).toByte(),
        (height shr 16 and 0xFF).toByte(),
        (height shr 8 and 0xFF).toByte(),
        (height and 0xFF).toByte()
    ) // height as 32 bit unsigned int
    image.writeBytes(if (hasAlpha) 4 else 3) // number of channels
    image.writeBytes(0x01) // TODO: figure out how this (colorspace) actually works

    // Main compression loop
    for (y in 0 until height)
        for (x in 0 until width) {
            // Get pixel color information
            color = Color(getRGB(x, y), hasAlpha)
            R = color.red.toByte()
            G = color.green.toByte()
            B = color.blue.toByte()
            if (hasAlpha)
                A = color.alpha.toByte()

            // Encode pixel
            // 1: QOI_OP_INDEX
            if (array[hash(R, G, B, A)] == color.rgb)
                TODO("code")

            // 2: QOI_OP_DIFF
            // 3: QOI_OP_LUMA
            // 4: QOI_OP_RUN
            // 5: QOI_OP_RGB(A)

            // Add pixel to the running array
            array[hash(R, G, B, A)] = color.rgb

            // Change previous pixel values to this pixel
            prevR = R
            prevG = G
            prevB = B
            prevA = A
        }

    return image
}

fun hash(r: Byte, g: Byte, b: Byte, a: Byte) = (r * 3 + g * 5 + b * 7 + a * 11) % 64