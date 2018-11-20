package com.greglaun.lector.ui.speak

import junit.framework.Assert.assertTrue
import org.junit.Test

class NoOpTtsViewTest {
    val noOpTtsView = NoOpTtsPresenter()

    @Test
    fun speak() {
        val lock = Object()
        var myBool = false
        synchronized(lock) {
        noOpTtsView.speak("myString", { aString: String ->
            myBool = true
        })
        }
        synchronized(lock) {
            assertTrue(myBool)
        }
    }
}