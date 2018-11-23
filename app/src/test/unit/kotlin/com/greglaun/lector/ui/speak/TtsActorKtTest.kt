package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock


class TtsActorKtTest {
    val ttsClient = FakeTtsActorClient()
    val articleStateSource = mock(ArticleStateSource::class.java)
    val stateMachine = TtsActorStateMachine(articleStateSource)

    val urlString = "a url"
    val paragraphs = listOf("some", "paragraphs")
    val articleState = ArticleState("MyTitle",  paragraphs,
            paragraphs.listIterator())


    @Before
    fun setUp() {
        stateMachine.startMachine(ttsClient)
        `when`(articleStateSource.getArticle(urlString)).thenReturn(articleState)
    }

    @Test
    fun initialStateNotReady() {
        runBlocking {
            assertTrue(SpeakerState.NOT_READY == stateMachine.getState().await())
        }
    }

    @Test
    fun MarkReadySuccessful() {
        runBlocking {
            stateMachine.changeStateReady().await()
            assertTrue(SpeakerState.READY == stateMachine.getState().await())
        }
    }

    @Test
    fun MarkNotReadySuccessful() {
        runBlocking {
            stateMachine.changeStateReady().await()
            assertTrue(SpeakerState.READY == stateMachine.getState().await())
            stateMachine.changeStateNotReady().await()
            assertTrue(SpeakerState.NOT_READY == stateMachine.getState().await())
        }
    }

    @Test
    fun updateArticleSuccess() {
        runBlocking {
            stateMachine.changeStateReady().await()
            assertTrue(SpeakerState.READY == stateMachine.getState().await())
            stateMachine.changeStateUpdateArticle(urlString).await()
            assertTrue(SpeakerState.NOT_READY == stateMachine.getState().await())
        }
    }

    @Test
    fun speakOne() {
        runBlocking {
            val notReady = stateMachine.actionSpeakOne().await() // 1
            assertTrue(SpeakerState.NOT_READY == notReady)
        }

        runBlocking {
            stateMachine.changeStateUpdateArticle(urlString).await()
            stateMachine.changeStateReady().await()
            stateMachine.changeStateStartSpeaking()
            val state1 = stateMachine.actionSpeakOne().await() // 1
            assertTrue(SpeakerState.SPEAKING == state1)
            val state2 = stateMachine.actionSpeakOne().await() // 2
            assertTrue(SpeakerState.NOT_READY == state2)
        }
    }


    @Test
    fun stopSpeaking() {
        runBlocking {
            stateMachine.changeStateUpdateArticle(urlString).await()
            stateMachine.changeStateReady().await()
            stateMachine.changeStateStartSpeaking()
            stateMachine.actionStopSpeaking()
            val state1 = stateMachine.actionSpeakOne().await() // 1
            assertTrue(SpeakerState.READY == state1)
        }

    }
}