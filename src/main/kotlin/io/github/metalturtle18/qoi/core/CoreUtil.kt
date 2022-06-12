package io.github.metalturtle18.qoi.core

/**
 * Hash function for four color channels according to the QOI specification
 *
 * @param r red channel
 * @param g green channel
 * @param b blue channel
 * @param a alpha channel
 * @return hash value
 */
internal fun hash(r: Byte, g: Byte, b: Byte, a: Byte) =
    (r.toUByte().toInt() * 3 + g.toUByte().toInt() * 5 + b.toUByte().toInt() * 7 + a.toUByte().toInt() * 11) % 64

/**
 * Function to convert individual RGBA values into a single 32-bit integer
 *
 * @param r red channel
 * @param g green channel
 * @param b blue channel
 * @param a alpha channel
 * @return integer color value
 */
internal fun rgbToInt(r: Byte, g: Byte, b: Byte, a: Byte) =
    (a.toInt() and 0xFF shl 24) or (r.toInt() and 0xFF shl 16) or (g.toInt() and 0xFF shl 8) or (b.toInt() and 0xFF)

/**
 * Function to convert a 32-bit integer into a [ByteArray] of four bytes representing its 32 bits
 *
 * @receiver the integer to convert
 * @return a [ByteArray] representing the integer as four bytes
 */
internal fun Int.toByteArray() = byteArrayOf(
    (this shr 24 and 0xFF).toByte(),
    (this shr 16 and 0xFF).toByte(),
    (this shr 8 and 0xFF).toByte(),
    (this and 0xFF).toByte()
)