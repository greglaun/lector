package com.greglaun.lector.ui.speak

import com.greglaun.lector.LectorApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class TtsActorStateMachineTest {
    val stateMachine = TtsActorStateMachine()
    val fakeClient = FakeTtsActorClient()
    val mockListener = mock(TtsStateListener::class.java)

    @Test
    fun startAndStopMachine() {
        stateMachine.attach(fakeClient, mockListener, LectorApplication.AppStore)
        assertFalse(stateMachine.ACTOR_LOOP!!.isClosedForSend)
        stateMachine.detach()
        assertTrue(stateMachine.ACTOR_LOOP!!.isClosedForSend)
    }

    @Test
    fun changeStateUpdateArticle() {
        val articleState = ArticleState("Test", listOf("A", "B", "C"))
        stateMachine.attach(fakeClient, mockListener, LectorApplication.AppStore)
        runBlocking {
            assertEquals(stateMachine.getSpeakerState(), SpeakerState.NOT_READY)
            stateMachine.updateArticle(articleState)
            assertEquals(stateMachine.getSpeakerState(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState(), articleState)

            stateMachine.updateArticle(articleState.next()!!)
            assertEquals(stateMachine.getSpeakerState(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState(), articleState.next())
        }
    }

    @Test
    fun actionSpeakOne() {
        val articleState = ArticleState("Test", listOf("A", "B", "C"))
        stateMachine.attach(fakeClient, mockListener, LectorApplication.AppStore)
        runBlocking {
            stateMachine.updateArticle(articleState)
            stateMachine.actionSpeakOne()
            verify(mockListener, times(1)).onUtteranceStarted(articleState)
            verify(mockListener, times(1)).onUtteranceEnded(articleState)
            verify(mockListener, times(0)).onArticleFinished(articleState)
        }
    }

    @Test
    fun actionSpeakInLoop() {
        val articleState = ArticleState("Test", listOf("A", "B", "C"))
        stateMachine.attach(fakeClient, mockListener, LectorApplication.AppStore)
        runBlocking {
            stateMachine.updateArticle(articleState)
            stateMachine.actionSpeakInLoop {}
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

            assertEquals(stateMachine.getSpeakerState(), SpeakerState.NOT_READY)
        }
    }

    @Test
    fun transport() {
        val articleState = ArticleState("Test", listOf("A", "B"))
        stateMachine.attach(fakeClient, mockListener, LectorApplication.AppStore)
        runBlocking {
            assertEquals(stateMachine.getSpeakerState(), SpeakerState.NOT_READY)
            stateMachine.updateArticle(articleState)
            assertEquals(stateMachine.getSpeakerState(), SpeakerState.READY)
            assertEquals(stateMachine.getArticleState(), articleState)

//            // Attempting to seek past beginning of utterance list
//            stateMachine.stopReverseOneAndResume {}
//            assertEquals(stateMachine.getSpeakerState(), SpeakerState.READY)
//            assertEquals(stateMachine.getArticleState(), articleState)
//
//            // Now on B
//            stateMachine.stopAdvanceOneAndResume {}
//            assertEquals(stateMachine.getSpeakerState(), SpeakerState.READY)
//            assertEquals(stateMachine.getArticleState(), articleState.next())
//
//            // Now on A
//            stateMachine.stopReverseOneAndResume {}
//            assertEquals(stateMachine.getSpeakerState(), SpeakerState.READY)
//            assertEquals(stateMachine.getArticleState(), articleState)
//
//            // Now on B again
//            stateMachine.stopAdvanceOneAndResume {}
//            assertEquals(stateMachine.getSpeakerState(), SpeakerState.READY)
//            assertEquals(stateMachine.getArticleState(), articleState.next())
//
//            // Attempting to seek past end of utterance list
//            stateMachine.stopAdvanceOneAndResume {}
//            assertEquals(stateMachine.getSpeakerState(), SpeakerState.READY)
//            assertEquals(stateMachine.getArticleState(), articleState.next())
        }
    }

    @Test
    fun stopSpeaking() {
        val articleState = ArticleState("Test", listOf("A", "B", "C"))
        stateMachine.attach(fakeClient, mockListener, LectorApplication.AppStore)

        runBlocking {
            stateMachine.updateArticle(articleState)
            stateMachine.actionSpeakOne()
            stateMachine.actionStopSpeaking()
            assertEquals(stateMachine.getSpeakerState(), SpeakerState.READY)
        }
    }
}