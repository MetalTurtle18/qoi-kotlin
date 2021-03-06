package io.github.metalturtle18.qoi.core

const val QOI_OP_INDEX = 0b0000_0000
const val QOI_OP_DIFF = 0b0100_0000
const val QOI_OP_LUMA = 0b1000_0000
const val QOI_OP_RUN = 0b1100_0000
const val QOI_OP_RGB = 0b1111_1110
const val QOI_OP_RGBA = 0b1111_1111

val QOI_HEADER = intArrayOf('q'.code, 'o'.code, 'i'.code, 'f'.code)

/**
 * Encodes a supplied image in the form of a [Sequence] of bytes into a supplied [QOIImage] based on the [QOI specification](https://qoiformat.org/qoi-specification.pdf).
 *
 * @param data the byte [Sequence] to encode
 * @param image the [QOIImage] to write the encoded bytes to
 */
fun encodeIterableImage(data: Sequence<Byte>, image: QOIImage) {
    val hasAlpha = image.channels == 4

    // Temporary variables for encoding
    var hash: Int
    var run = 0

    var r: Byte = 0
    var g: Byte = 0
    var b: Byte = 0
    var a: Byte = -1 // Actually 255 unsigned

    var dr: Byte
    var dg: Byte
    var db: Byte

    var prevR = r
    var prevG = g
    var prevB = b
    var prevA = a

    val array = IntArray(64)

    // Header
    image.writeBytes(*QOI_HEADER) // magic bytes
    image.writeBytes(*image.width.toByteArray()) // width as 32-bit unsigned integer
    image.writeBytes(*image.height.toByteArray()) // height as a 32-bit unsigned integer
    image.writeBytes(image.channels) // number of channels
    image.writeBytes(image.colorSpace.value) // color space

    // Go through the data as a sequence and chunk it by how many bytes will be present per pixel
    data.chunked(image.channels).forEachIndexed { index, it ->
        r = it[0]
        g = it[1]
        b = it[2]
        if (hasAlpha)
            a = it[3]

        // CASE 1: QOI_OP_RUN
        if (r == prevR && g == prevG && b == prevB && a == prevA) {
            run++ // Increment run length
            if (run == 62 || (index == image.height * image.width - 1)) { // If the maximum run length is reached or the last pixel is reached
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
            if (array[hash] == rgbToInt(r, g, b, a))
                image.writeBytes(QOI_OP_INDEX or hash)
            else {
                array[hash] = rgbToInt(r, g, b, a)

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
}
