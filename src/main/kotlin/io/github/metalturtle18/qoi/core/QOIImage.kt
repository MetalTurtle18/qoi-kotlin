package io.github.metalturtle18.qoi.core

import java.io.ByteArrayOutputStream

/**
 * A class representing a QOI image
 *
 * @property width      the width of the image
 * @property height     the height of the image
 * @property channels   the number of channels in the image
 * @property colorSpace the colorspace the image is using; either sRGB or linear
 * @property data       the qoi encoded data of the image
 */
class QOIImage internal constructor(
    val width: Int,
    val height: Int,
    val channels: Int,
    val colorSpace: ColorSpace = ColorSpace.SRGB, // TODO: Figure out how to actually figure this out
    val data: ByteArrayOutputStream = ByteArrayOutputStream()
) {
    /**
     * Write the given bytes to the byte output stream
     *
     * @param data the bytes to write
     */
    internal fun writeBytes(vararg data: Byte) {
        this.data.write(ByteArray(data.size) { data[it] })
    }

    /**
     * Write the given integers to the byte output stream
     *
     * This method DOES NOT write the integers as 4 bytes. It writes only the final 8 bits of the integer and discards the rest
     *
     * @param data the bytes to write
     */
    internal fun writeBytes(vararg data: Int) {
        writeBytes(*data.map { it.toByte() }.toByteArray())
    }

    /**
     * An enum class to represent the two colorspace types in a QOI image
     */
    enum class ColorSpace(val value: Byte) {
        SRGB(0x00),
        LINEAR(0x01)
    }
}
