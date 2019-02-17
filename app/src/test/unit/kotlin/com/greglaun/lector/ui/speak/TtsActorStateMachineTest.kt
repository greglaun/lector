package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class TtsActorStateMachineTest {
    val stateMachine = TtsActorStateMachine()
    val fakeClient = FakeTtsActorClient()
    val mockListener = mock(TtsStateListener::class.java)

    @Test
    fun startAndStopMachine() {
        stateMachine.startMachine(fakeClient, mockListener)
        assertFalse(stateMachine.ACTOR_LOOP!!.isClosedForSend)
        stateMachine.stopMachine()
        assertTrue(stateMachine.ACTOR_LOOP!!.isClosedForSend)
    }

    @Test
    fun changeStateUpdateArticle() {
        val articleState = ArticleState("Test", listOf("A", "B", "C"))
        stateMachine.startMachine(fakeClient, mockListener)
        runBlocking {
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.NOT_READY)
            stateMachine.updateArticle(articleState)
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState().await(), articleState)

            stateMachine.updateArticle(articleState.next()!!)
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState().await(), articleState.next())
        }
    }

    @Test
    fun actionSpeakOne() {
        val articleState = ArticleState("Test", listOf("A", "B", "C"))
        stateMachine.startMachine(fakeClient, mockListener)
        runBlocking {
            stateMachine.updateArticle(articleState)
            stateMachine.actionSpeakOne().await()
            verify(mockListener, times(1)).onUtteranceStarted(articleState)
            verify(mockListener, times(1)).onUtteranceEnded(articleState)
            verify(mockListener, times(0)).onArticleFinished(articleState)
        }
    }

    @Test
    fun actionSpeakInLoop() {
        val articleState = ArticleState("Test", listOf("A", "B", "C"))
        stateMachine.startMachine(fakeClient, mockListener)
        runBlocking {
            stateMachine.updateArticle(articleState)
            stateMachine.actionSpeakInLoop {}.await()
            stateMachine.SPEECH_LOOP!!.join()

            verify(mockListener, times(1)).onUtteranceStarted(articleState)
            verify(mockListener, times(1)).onUtteranceEnded(articleState)

            val bState = articleState.next()!!
            verify(mockListener, times(1)).onUtteranceStarted(bState)
            verify(mockListener, times(1)).onUtteranceEnded(bState)

            val cState = bState.next()!!
            verify(mockListener, times(1)).onUtteranceStarted(cState)
            verify(mockListener, times(1)).onUtteranceEnded(cState)

            verify(mockListener, times(1)).onArticleFinished(cState)

            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.NOT_READY)
        }
    }

    @Test
    fun transport() {
        val articleState = ArticleState("Test", listOf("A", "B"))
        stateMachine.startMachine(fakeClient, mockListener)
        runBlocking {
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.NOT_READY)
            stateMachine.updateArticle(articleState)
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState().await(), articleState)

            // Attempting to seek past beginning of utterance list
            stateMachine.stopReverseOneAndResume {}
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState().await(), articleState)

            // Now on B
            stateMachine.stopAdvanceOneAndResume {}
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState().await(), articleState.next())

            // Now on A
            stateMachine.stopReverseOneAndResume {}
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState().await(), articleState)

            // Now on B again
            stateMachine.stopAdvanceOneAndResume {}
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState().await(), articleState.next())

            // Attempting to seek past end of utterance list
            stateMachine.stopAdvanceOneAndResume {}
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState().await(), articleState.next())
        }
    }

    @Test
    fun stopSpeaking() {
        val articleState = ArticleState("Test", listOf("A", "B", "C"))
        stateMachine.startMachine(fakeClient, mockListener)

        runBlocking {
            stateMachine.updateArticle(articleState)
            stateMachine.actionSpeakOne().await()
            stateMachine.actionStopSpeaking().await()
            assertEquals(stateMachine.getSpeakerState().await(), SpeakerState.READY)
        }
    }
}