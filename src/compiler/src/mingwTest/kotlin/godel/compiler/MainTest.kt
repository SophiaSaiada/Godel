package godel.compiler

import kotlin.test.Test
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun testHello() {
        assertTrue("Kotlin/Native (compiler)" in hello())
    }
}