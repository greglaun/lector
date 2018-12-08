package com.greglaun.lector.ui.speak

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class TtsPresenterTest {
    var ttsPresenter: TtsPresenter? = null
    var stateMachine: TtsStateMachine? = null
    var audioView: TTSContract.AudioView? = null

    @Before
    fun setUp() {
        audioView = mock(TTSContract.AudioView::class.java)
        stateMachine = mock(TtsStateMachine::class.java)

        ttsPresenter = TtsPresenter(audioView!!, stateMachine!!)
    }

    @Test
    fun onStop() {
        ttsPresenter!!.onStop()
        verify(stateMachine, times(1))!!.stopMachine()
    }

    @Test
    fun speakInLoop() {
        ttsPresenter!!.speakInLoop(null)
        verify(stateMachine, times(1))!!
                .actionSpeakInLoop(null)
    }

    @Test
    fun stopSpeechViewImmediately() {
        ttsPresenter!!.stopSpeechViewImmediately()
        verify(audioView, times(1))!!.stopImmediately()
    }

    @Test
    fun onUrlChanged() {
        ttsPresenter!!.onUrlChanged("Hello")
        verify(stateMachine, times(1))!!.actionChangeUrl("Hello")
    }

    @Test
    fun stopSpeaking() {
        ttsPresenter!!.stopSpeaking()
        verify(stateMachine, times(1))!!.actionStopSpeaking()
    }
}