package com.greglaun.lector.ui.speak

import kotlinx.coroutines.runBlocking
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
    fun onStart() {
        val stateListener = mock(TtsStateListener::class.java)
        ttsPresenter!!.onStart(stateListener)
        verify(stateMachine, times(1))!!.startMachine(
                ttsPresenter!!,
                stateListener)
    }

    @Test
    fun onStop() {
        ttsPresenter!!.onStop()
        verify(stateMachine, times(1))!!.stopMachine()
    }

    @Test
    fun speakInLoop() {
        runBlocking {
            ttsPresenter!!.speakInLoop(null)
            verify(stateMachine, times(1))!!
                    .actionSpeakInLoop(null)
        }
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
        runBlocking {
            ttsPresenter!!.stopSpeaking()
        }
        runBlocking {
            verify(stateMachine, times(1))!!.actionStopSpeaking()
        }
    }

    @Test
    fun onArticleChanged() {
        val articleState = ArticleState("Test", listOf("A", "B"))
        runBlocking {
            ttsPresenter!!.onArticleChanged(articleState)
            verify(stateMachine, times(1))!!.updateArticle(articleState)
        }
    }

    @Test
    fun setHandsomeBritish() {
        ttsPresenter!!.setHandsomeBritish(true)
        verify(audioView, times(1))!!.setHandsomeBritish(true)

        ttsPresenter!!.setHandsomeBritish(false)
        verify(audioView, times(1))!!.setHandsomeBritish(false)
    }

    @Test
    fun setSpeechRate() {
        ttsPresenter!!.setSpeechRate(12f)
        verify(audioView, times(1))!!.setSpeechRate(12f)
    }
}
