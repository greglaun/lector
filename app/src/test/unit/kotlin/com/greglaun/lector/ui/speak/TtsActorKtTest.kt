package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock


class TtsActorKtTest {
    val ttsClient = FakeTtsActorClient()
    val stateMachine = TtsActorStateMachine()
    val articleState = ArticleState("MyTitle", listOf("some", "paragraphs")
    )

    @Before
    fun setUp() {
        stateMachine.startMachine(ttsClient, mock(TtsStateListener::class.java))
    }

    @Test
    fun initialStateNotReady() {
        runBlocking {
            assertTrue(SpeakerState.NOT_READY == stateMachine.getSpeakerState().await())
        }
    }

    @Test
    fun updateArticleSuccess() {
        runBlocking {
            stateMachine.updateArticle(articleState)
            assertTrue(SpeakerState.READY == stateMachine.getSpeakerState().await())
        }
    }

    @Test
    fun speakOne() {
        runBlocking {
            val notReady = stateMachine.actionSpeakOne().await() // 1
            assertTrue(SpeakerState.NOT_READY == notReady)
        }

        runBlocking {
            stateMachine.updateArticle(articleState)
//            stateMachine.changeStateReady().await()
//            stateMachine.changeStateStartSpeaking().await()
            val state1 = stateMachine.actionSpeakOne().await() // 1
            assertTrue(SpeakerState.SPEAKING == state1)
            val state2 = stateMachine.actionSpeakOne().await() // 2
            assertTrue(SpeakerState.NOT_READY == state2)
        }
    }
    
    @Test
    fun stopSpeaking() {
        runBlocking {
            stateMachine.updateArticle(articleState)
//            stateMachine.changeStateReady().await()
//            stateMachine.changeStateStartSpeaking().await()
            stateMachine.actionStopSpeaking().await()
            val state1 = stateMachine.actionSpeakOne().await() // 1
            assertTrue(SpeakerState.READY == state1)
        }

    }
}