package com.greglaun.lector.ui.speak

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class TtsPresenterTest {
    val audioView = mock(TTSContract.AudioView::class.java)
    var ttsPresenter : TtsPresenter? = null

    @Before
    fun setUp() {
        ttsPresenter = TtsPresenter(audioView)
    }

    @Test
    fun onUrlChanged() {
        assertTrue(false)
    }

    @Test
    fun startSpeaking() {
        assertTrue(false)
    }

    @Test
    fun stopSpeaking() {
        assertTrue(false)
    }
}