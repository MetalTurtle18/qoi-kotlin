package io.github.metalturtle18.qoi

import io.github.metalturtle18.qoi.core.hash
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TestAuxiliaryCoreFunctions : FunSpec({
    data class RGBA(val R: Byte, val G: Byte, val B: Byte, val A: Byte)
    context("testing hash function") {
        mapOf(
            RGBA(0, 0, 0, 0) to 0,
            RGBA(0, 3, 7, 0) to 0,
            // TODO: Write more tests
        ).forEach {
            test("test hash function for ${it.key}") {
                hash(it.key.R, it.key.G, it.key.B, it.key.A) shouldBe it.value
            }
        }
    }

})