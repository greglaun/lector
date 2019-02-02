package com.greglaun.lector.ui.main

import com.greglaun.lector.android.room.RoomCourseSource
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.ui.speak.TTSContract
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.io.File

class MainPresenterTest {
    lateinit var mainPresenter: MainContract.Presenter
    lateinit var mockView: MainContract.View
    lateinit var mockTts: TTSContract.Presenter
    lateinit var responseSource: ResponseSourceImpl
    lateinit var courseSource: RoomCourseSource
    val testDir = File("testDir")

    @Before
    fun setUp() {
        mockView = mock(MainContract.View::class.java)
        mockTts = mock(TTSContract.Presenter::class.java)

        responseSource = mock(ResponseSourceImpl::class.java)
        courseSource = mock(RoomCourseSource::class.java)

        mainPresenter = MainPresenter(mockView, mockTts, responseSource, courseSource)
    }

    @After
    fun cleanup() {
        if (testDir.exists()) {
            testDir.deleteRecursively()
        }
    }

    @Test
    fun onPlayButtonPressed() {
        mainPresenter.onPlayButtonPressed()
        verify(mockTts, times(1)).speakInLoop(ArgumentMatchers.any())
        verify(mockView, times(1)).enablePauseButton()
    }

    @Test
    fun stopSpeakingAndEnablePlayButton() {
        mainPresenter.stopSpeakingAndEnablePlayButton()
        verify(mockTts, times(1)).stopSpeaking()
        verify(mockView, times(1)).enablePlayButton()
    }

    @Test
    fun onUrlChanged() {
        runBlocking {
            `when`(responseSource.contains(ArgumentMatchers.anyString())).thenReturn(
                    CompletableDeferred(false))
            `when`(responseSource.add(ArgumentMatchers.anyString())).thenReturn(
                    CompletableDeferred())
            mainPresenter.onUrlChanged("test").await()
            verify(mockView, times(1)).loadUrl("test")
        }
    }

    @Test
    fun onForwardOne() {
        assertTrue(false)
    }

    @Test
    fun getReadingList() {
        assertTrue(false)
    }

    @Test
    fun onAttach() {
        assertTrue(false)
    }

    @Test
    fun onDetach() {
        assertTrue(false)
    }

    @Test
    fun onUtteranceStarted() {
        assertTrue(false)
    }

    @Test
    fun onUtteranceEnded() {
        assertTrue(false)
    }

    @Test
    fun onArticleFinished() {
        assertTrue(false)
    }

    @Test
    fun onSpeechStopped() {
        assertTrue(false)
    }

    @Test
    fun loadFromContext() {
        assertTrue(false)
    }

    @Test
    fun onRequest() {
        assertTrue(false)
    }

    @Test
    fun saveArticle() {
        assertTrue(false)
    }

    @Test
    fun courseDetailsRequested() {
        assertTrue(false)
    }

    @Test
    fun deleteRequested() {
        assertTrue(false)
    }

    @Test
    fun deleteRequested1() {
        assertTrue(false)
    }

    @Test
    fun onDisplayReadingList() {
        assertTrue(false)
    }

    @Test
    fun onDisplayCourses() {
        assertTrue(false)
    }

    @Test
    fun onRewindOne() {
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

    @Test
    fun evaluateJavascript() {
        assertTrue(false)
    }

    @Test
    fun onPageDownloadFinished() {
        assertTrue(false)
    }

    @Test
    fun playAllPressed() {
        assertTrue(false)
    }

    @Test
    fun setAutoPlay() {
        assertTrue(false)
    }

    @Test
    fun setAutoDelete() {
        assertTrue(false)
    }
}