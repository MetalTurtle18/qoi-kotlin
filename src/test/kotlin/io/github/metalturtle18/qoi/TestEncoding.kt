package io.github.metalturtle18.qoi

fun main() {
    TestEncoding().main()
}

class TestEncoding {
    fun main() {
        val bytes = javaClass.getResourceAsStream("/dice.qoi")?.readAllBytes()
        bytes?.take(14)?.forEach { println(it) }
    }
}