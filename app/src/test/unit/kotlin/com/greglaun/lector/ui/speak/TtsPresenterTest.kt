package com.greglaun.lector.ui.speak

import com.greglaun.lector.LectorApplication
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class TtsPresenterTest {
    var ttsPresenter: TtsPresenter? = null
    var stateMachine: DeprecatedTtsStateMachine? = null
    var audioView: TTSContract.AudioView? = null
    val articleState = ArticleState("MyTitle", listOf("some", "paragraphs"))

    @Before
    fun setUp() {
        audioView = mock(TTSContract.AudioView::class.java)
        stateMachine = mock(DeprecatedTtsStateMachine::class.java)
        ttsPresenter = TtsPresenter(audioView!!, stateMachine!!, LectorApplication.AppStore)
    }

    @Test
    fun onStart() {
        val stateListener = mock(TtsStateListener::class.java)
        ttsPresenter!!.deprecatedOnStart(stateListener)
        verify(stateMachine, times(1))!!.startMachine(
                ttsPresenter!!,
                stateListener, LectorApplication.AppStore)
    }

    @Test
    fun onStop() {
        ttsPresenter!!.deprecatedOnStop()
        verify(stateMachine, times(1))!!.stopMachine()
    }

    @Test
    fun speakInLoop() {
        runBlocking {
            ttsPresenter!!.deprecatedSpeakInLoop(null)
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
            ttsPresenter!!.deprecatedOnArticleChanged(articleState)
            verify(stateMachine, times(1))!!.updateArticle(articleState)
        }
    }

    @Test
    fun stopSpeaking() {
        runBlocking {
            ttsPresenter!!.deprecatedStopSpeaking()
        }
        runBlocking {
            verify(stateMachine, times(1))!!.actionStopSpeaking()
        }
    }

    @Test
    fun onArticleChanged() {
        val articleState = ArticleState("Test", listOf("A", "B"))
        runBlocking {
            ttsPresenter!!.deprecatedOnArticleChanged(articleState)
            verify(stateMachine, times(1))!!.updateArticle(articleState)
        }
    }

    @Test
    fun setHandsomeBritish() {
        ttsPresenter!!.deprecatedHandsomeBritish(true)
        verify(audioView, times(1))!!.setHandsomeBritish(true)

        ttsPresenter!!.deprecatedHandsomeBritish(false)
        verify(audioView, times(1))!!.setHandsomeBritish(false)
    }

    @Test
    fun setSpeechRate() {
        ttsPresenter!!.deprecatedSetSpeechRate(12f)
        verify(audioView, times(1))!!.setSpeechRate(12f)
    }
}
