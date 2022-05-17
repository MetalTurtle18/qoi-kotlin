package io.github.metalturtle18.qoi

import java.io.ByteArrayOutputStream

/**
 * A class representing a QOI image
 *
 * @property width    the width of the image
 * @property height   the height of the image
 * @property channels the number of channels in the image
 * @property data     the qoi encoded data of the image
 */
class QOIImage(
    val width: Int,
    val height: Int,
    val channels: Int,
    val colorSpace: ColorSpace = ColorSpace.sRGB,
    val data: ByteArrayOutputStream = ByteArrayOutputStream()
) {
    val bitBuffer = BooleanArray(8)

    /**
     * Write the given bytes to the byte output stream
     *
     * @param data the bytes to write
     */
    internal fun writeBytes(vararg data: Byte) {
        this.data.write(ByteArray(data.size) { data[it] })
    }
}

enum class ColorSpace {
    sRGB,
    LINEAR
}