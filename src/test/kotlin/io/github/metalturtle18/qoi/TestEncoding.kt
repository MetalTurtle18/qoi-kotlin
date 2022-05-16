package io.github.metalturtle18.qoi

import java.awt.Color
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO


fun main() {
    // Will be used to compare compression techniques (negative control)
    saveUncompressedImage("src/test/resources/test.png", "src/test/resources/test.bin")
}

/**
 * Take any png or jpg image and save it as a binary file where each pixel is represented by 3 or 4 bytes, depending on whether the image has an alpha channel
 * @param source the path to the source image
 * @param destination the path to the destination binary file
 */
fun saveUncompressedImage(source: String, destination: String) {
    val image = ImageIO.read(File(source))
    val hasAlpha = image.colorModel.hasAlpha()
    var color: Color
    FileOutputStream(destination).use {
        for (y in 0 until image.height)
            for (x in 0 until image.width) {
                println("Current Pixel: ${y * image.width + x + 1}/${image.width * image.height} (${(y * image.width + x + 1) / (image.width * image.height).toDouble() * 100}%) ($x, $y)")
                color = Color(image.getRGB(x, y), image.colorModel.hasAlpha())
                it.write(color.red)
                it.write(color.green)
                it.write(color.blue)
                if (hasAlpha)
                    it.write(color.alpha)
            }
    }
}
