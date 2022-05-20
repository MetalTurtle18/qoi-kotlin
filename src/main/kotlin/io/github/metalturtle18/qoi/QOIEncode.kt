package io.github.metalturtle18.qoi

import java.awt.Color
import java.awt.image.BufferedImage

const val QOI_OP_INDEX = 0x00
const val QOI_OP_DIFF = 0x01
const val QOI_OP_LUMA = 0x02
const val QOI_OP_RUN = 0x03
const val QOI_OP_RGB = 0xFE
const val QOI_OP_RGBA = 0xFF


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
    var hash: Int
    var R: Byte = 0
    var G: Byte = 0
    var B: Byte = 0
    var A: Byte = 255.toByte()

    var prevR = R
    var prevG = G
    var prevB = B
    var prevA = A

    val array = IntArray(64)

    // Header
    image.writeBytes(
        'q'.code.toByte(),
        'o'.code.toByte(),
        'i'.code.toByte(),
        'f'.code.toByte()
    ) // magic bytes
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
            hash = hash(R, G, B, A)

            // Encode pixel
            // 1: QOI_OP_INDEX
            if (array[hash] == color.rgb)
                image.writeBytes((QOI_OP_INDEX or (hash(R, G, B, A) and 0x3F)).toByte())

            // 2: QOI_OP_DIFF

            // 3: QOI_OP_LUMA
            // 4: QOI_OP_RUN

            // 5: QOI_OP_RGBA
            else if (hasAlpha)
                image.writeBytes(QOI_OP_RGBA.toByte(), R, G, B, A)

            // 6: QOI_OP_RGB
            else
                image.writeBytes(QOI_OP_RGB.toByte(), R, G, B)

            // Add pixel to the running array
            array[hash] = color.rgb

            // Change previous pixel values to this pixel
            prevR = R
            prevG = G
            prevB = B
            prevA = A
        }

    return image
}

fun hash(r: Byte, g: Byte, b: Byte, a: Byte) = (r.toUByte().toInt() * 3 + g.toUByte().toInt() * 5 + b.toUByte().toInt() * 7 + a.toUByte().toInt() * 11) % 64 // this seems like there should be a better way TODO