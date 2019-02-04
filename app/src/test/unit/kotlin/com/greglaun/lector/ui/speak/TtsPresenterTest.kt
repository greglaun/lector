package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class TtsPresenterTest {
    var ttsPresenter: TtsPresenter? = null
    var stateMachine: TtsStateMachine? = null
    var audioView: TTSContract.AudioView? = null
    val articleState = ArticleState("MyTitle", listOf("some", "paragraphs"))

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
        runBlocking {
            ttsPresenter!!.onArticleChanged(articleState)
            verify(stateMachine, times(1))!!.updateArticle(articleState)
        }
    }

    @Test
    fun stopSpeaking() {
        ttsPresenter!!.stopSpeaking()
        verify(stateMachine, times(1))!!.actionStopSpeaking()
    }

    @Test
    fun speechViewSpeak() {
        assertTrue(false)
    }

    @Test
    fun onStart() {
        assertTrue(false)
    }

    @Test
    fun onArticleChanged() {
        assertTrue(false)
    }

    @Test
    fun advanceOne() {
        assertTrue(false)
    }

    @Test
    fun reverseOne() {
        assertTrue(false)
    }

    @Test
    fun setHandsomeBritish() {
        assertTrue(false)
    }

    @Test
    fun setSpeechRate() {
        assertTrue(false)
    }

}