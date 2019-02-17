package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.BasicArticleContext
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.data.cache.contextToUrl
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.course.ConcreteCourseContext
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.TTSContract
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.io.File

class MainPresenterTest {
    val mockView = mock(MainContract.View::class.java)
    val mockTts = mock(TTSContract.Presenter::class.java)

    val responseSource = mock(ResponseSourceImpl::class.java)
    val courseSource = mock(CourseSource::class.java)

    val mainPresenter = MainPresenter(mockView, mockTts, responseSource, courseSource)

    val testDir = File("testDir")

    @After
    fun cleanup() {
        if (testDir.exists()) {
            testDir.deleteRecursively()
        }
    }

    @Test
    fun onAttach() {
        mainPresenter.onAttach()
        verify(mockTts, times(1)).onStart(mainPresenter)
    }

    @Test
    fun onDetach() {
        mainPresenter.onDetach()
        verify(mockTts, times(1)).onStop()
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
            mainPresenter.onUrlChanged("test")
            verify(mockView, times(1)).loadUrl("test")
        }
    }

    @Test
    fun onUtteranceStarted() {
        val articleState = ArticleState("A name", listOf("One", "Two"))
        mainPresenter.onUtteranceStarted(articleState)
        verify(mockView, times(1)).highlightText(articleState)
    }

    @Test
    fun onUtteranceEnded() {
        val articleState = ArticleState("A name", listOf("One", "Two"))
        mainPresenter.onUtteranceEnded(articleState)
        verify(mockView, times(1)).unhighlightAllText()
    }

    @Test
    fun onSpeechStopped() {
        mainPresenter.onSpeechStopped()
        verify(mockView, times(1)).enablePlayButton()
    }

    @Test
    fun loadFromContext() {
        `when`(responseSource.contains(ArgumentMatchers.anyString())).thenReturn(
                CompletableDeferred(false))
        `when`(responseSource.add(ArgumentMatchers.anyString())).thenReturn(
                CompletableDeferred(0L))
        val context = BasicArticleContext.fromString("Something")
        runBlocking {
            mainPresenter.loadFromContext(context)
        }
        verify(mockView, times(1)).loadUrl(
                contextToUrl(context.contextString))
        verify(mockView, times(1)).unhideWebView()
    }

    @Test
    fun saveArticle() {
        runBlocking {
            mainPresenter.saveArticle()
            verify(responseSource, times(1)).
                    markPermanent("MAIN_PAGE")
        }
    }

    @Test
    fun courseDetailsRequested() {
        val courseContext = ConcreteCourseContext(0L, "A name", 0)
        runBlocking {
            mainPresenter.courseDetailsRequested(courseContext)
            verify(courseSource, times(1)).
                    getArticlesForCourse(courseContext.id!!)
        }
    }

    @Test
    fun onDisplayReadingList() {
        runBlocking {
            mainPresenter.onDisplayReadingList()
            verify(responseSource, times(1)).getAllPermanent()
        }
    }

    @Test
    fun onDisplayCourses() {
        runBlocking {
            mainPresenter.onDisplayCourses()
            verify(mockView, times(1)).displayCourses()
        }
    }

    @Test
    fun onPageDownloadFinished() {
        mainPresenter.onPageDownloadFinished("A String")
        verify(responseSource, times(1)).markFinished(
                urlToContext("A String"))
    }
}