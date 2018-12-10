package godel.executor

import kotlin.test.Test
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun testHello() {
        assertTrue("Kotlin/Native (executor)!" in hello())
    }
}