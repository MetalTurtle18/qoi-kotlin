package io.github.metalturtle18.qoi

import java.awt.Color
import java.awt.image.BufferedImage

const val QOI_OP_INDEX = 0b0000_0000
const val QOI_OP_DIFF  = 0b0100_0000
const val QOI_OP_LUMA  = 0b1000_0000
const val QOI_OP_RUN   = 0b1100_0000
const val QOI_OP_RGB   = 0b1111_1110
const val QOI_OP_RGBA  = 0b1111_1111


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
    var run = 0

    var r: Byte = 0
    var g: Byte = 0
    var b: Byte = 0
    var a: Byte = -1 // Actually 255 unsigned

    var dr: Byte = 0
    var dg: Byte = 0
    var db: Byte = 0

    var prevR = r
    var prevG = g
    var prevB = b
    var prevA = a

    val array = IntArray(64)

    // Header
    image.writeBytes(
        'q'.code,
        'o'.code,
        'i'.code,
        'f'.code
    ) // magic bytes
    image.writeBytes(
        width shr 24 and 0xFF,
        width shr 16 and 0xFF,
        width shr 8 and 0xFF,
        width and 0xFF
    ) // width as 32 bit unsigned int
    image.writeBytes(
        height shr 24 and 0xFF,
        height shr 16 and 0xFF,
        height shr 8 and 0xFF,
        height and 0xFF
    ) // height as 32 bit unsigned int
    image.writeBytes(image.channels) // number of channels
    image.writeBytes(image.colorSpace.value) // color space

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

            // CASE 1: QOI_OP_RUN
            if (r == prevR && g == prevG && b == prevB && a == prevA) {
                run++ // Increment run length
                if (run == 62 || (y == height - 1 && x == width - 1)) { // If the maximum run length is reached or the last pixel is reached
                    image.writeBytes(QOI_OP_RUN or --run) // Write the run length and
                    run = 0
                }
            } else {
                if (run > 0) { // If this is the end of a run, write it
                    image.writeBytes(QOI_OP_RUN or --run)
                    run = 0
                }

                hash = hash(r, g, b, a)

                // CASE 2: QOI_OP_INDEX
                if (array[hash] == color.rgb)
                    image.writeBytes(QOI_OP_INDEX or hash)
                else {
                    array[hash] = color.rgb

                    if (a == prevA) { // The following operations assume that the alpha value doesn't change
                        dr = (r - prevR).toByte()
                        dg = (g - prevG).toByte()
                        db = (b - prevB).toByte()

                        // CASE 3: QOI_OP_DIFF
                        if (
                            dr >= -2 && dr <= 1 &&
                            dg >= -2 && dg <= 1 &&
                            db >= -2 && db <= 1
                        )
                            image.writeBytes(
                                QOI_OP_DIFF or
                                        (dr + 2 shl 4) or // Move the two bit red difference into the top two bits of the 6 data bits
                                        (dg + 2 shl 2) or // Move the two bit green difference into the middle two bits of the 6 data bits
                                        (db + 2) // Two bit blue difference doesn't need to move

                            )
                        // CASE 4: QOI_OP_LUMA
                        else if (
                            dg >= -32 && dg <= 31 &&
                            dr - dg >= -8 && dr - dg <= 7 &&
                            db - dg >= -8 && db - dg <= 7
                        )
                            image.writeBytes(
                                QOI_OP_LUMA or dg + 32,
                                (dr - dg + 8 shl 4) or db - dg + 8
                            )
                        // CASE 5: QOI_OP_RGB
                        else
                            image.writeBytes(QOI_OP_RGB.toByte(), r, g, b)
                    } else // CASE 6: QOI_OP_RGBA
                        image.writeBytes(QOI_OP_RGBA.toByte(), r, g, b, a)
                }

            }

            // Update previous pixel color variables
            prevR = r
            prevG = g
            prevB = b
            prevA = a
        }

    // End of stream bytes
    image.writeBytes(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01)

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
private fun hash(r: Byte, g: Byte, b: Byte, a: Byte) = (
        r.toUByte().toInt() * 3 +
        g.toUByte().toInt() * 5 +
        b.toUByte().toInt() * 7 +
        a.toUByte().toInt() * 11
        ) % 64
