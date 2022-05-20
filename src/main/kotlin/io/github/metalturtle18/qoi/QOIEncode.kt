package io.github.metalturtle18.qoi

import java.awt.Color
import java.awt.image.BufferedImage

const val QOI_OP_INDEX = 0x00
const val QOI_OP_DIFF = 0x40
const val QOI_OP_LUMA = 0x80
const val QOI_OP_RUN = 0xC0
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
    var r: Byte = 0
    var g: Byte = 0
    var b: Byte = 0
    var a: Byte = -1 // Actually 255 unsigned

    var prevR: Byte = 0
    var prevG: Byte = 0
    var prevB: Byte = 0
    var prevA: Byte = -1 // Actually 255 unsigned

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
            r = color.red.toByte()
            g = color.green.toByte()
            b = color.blue.toByte()
            if (hasAlpha)
                a = color.alpha.toByte()
            hash = hash(r, g, b, a)

            // Encode pixel
            // 1: QOI_OP_INDEX
            if (array[hash] == color.rgb)
                image.writeBytes((QOI_OP_INDEX or (hash(r, g, b, a) and 0x3F)).toByte())

            // 2: QOI_OP_DIFF
            else if (
                r - prevR <= 1 && r - prevR >= -2 &&
                g - prevG <= 1 && g - prevG >= -2 &&
                b - prevB <= 1 && b - prevB >= -2
            )
                image.writeBytes(
                    (QOI_OP_DIFF or
                            (r - prevR + 2 shl 4) or // Move the two bit red difference into the top two bits of the 6 data bits
                            (g - prevG + 2 shl 2) or // Move the two bit green difference into the middle two bits of the 6 data bits
                            (b - prevB + 2) // Two bit blue difference doesn't need to move
                            ).toByte()
                )

            // 3: QOI_OP_LUMA
            // TODO
            // 4: QOI_OP_RUN
            // TODO

            // 5: QOI_OP_RGBA
            else if (hasAlpha)
                image.writeBytes(QOI_OP_RGBA.toByte(), r, g, b, a)

            // 6: QOI_OP_RGB
            else
                image.writeBytes(QOI_OP_RGB.toByte(), r, g, b)

            // Add pixel to the running array
            array[hash] = color.rgb

            // Change previous pixel values to this pixel
            prevR = r
            prevG = g
            prevB = b
            prevA = a
        }

    return image
}

/**
 * Hash function for four color channels according to the QOI specification
 *
 * @param r Red channel
 * @param g Green channel
 * @param b Blue channel
 * @param a Alpha channel
 * @return Hash value
 */
fun hash(r: Byte, g: Byte, b: Byte, a: Byte) =
    (r.toUByte().toInt() * 3 +
            g.toUByte().toInt() * 5 +
            b.toUByte().toInt() * 7 +
            a.toUByte().toInt() * 11) % 64 // this seems like there should be a better way TODO