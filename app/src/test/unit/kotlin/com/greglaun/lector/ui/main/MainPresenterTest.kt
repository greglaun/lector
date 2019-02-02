package com.greglaun.lector.ui.main

import com.greglaun.lector.android.room.RoomCourseSource
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.ui.speak.TTSContract
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
}