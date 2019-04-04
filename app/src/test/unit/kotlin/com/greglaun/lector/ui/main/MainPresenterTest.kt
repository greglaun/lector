package com.greglaun.lector.ui.main

import com.greglaun.lector.LectorApplication
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.TTSContract
import kotlinx.coroutines.runBlocking
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

    val mainPresenter = MainPresenter(mockView, LectorApplication.AppStore)

    val testDir = File("testDir")

    @After
    fun cleanup() {
        if (testDir.exists()) {
            testDir.deleteRecursively()
        }
    }

//    @Test
//    fun onAttach() {
//        mainPresenter.onAttach()
//        verify(mockTts, times(1)).deprecatedOnStart(mainPresenter)
//    }
//
//    @Test
//    fun onDetach() {
//        mainPresenter.onDetach()
//        verify(mockTts, times(1)).deprecatedOnStop()
//    }
//
//    @Test
//    fun onPlayButtonPressed() {
//        mainPresenter.onPlayButtonPressed()
//        runBlocking {
//            verify(mockTts, times(1)).startSpeaking(ArgumentMatchers.any())
//            verify(mockView, times(1)).enablePauseButton()
//        }
//    }
//
//    @Test
//    fun stopSpeakingAndEnablePlayButton() {
//        mainPresenter.onPauseButtonPressed()
//        runBlocking {
//            verify(mockTts, times(1)).stopSpeaking()
//        }
//        verify(mockView, times(1)).enablePlayButton()
//    }
//
//    @Test
//    fun onUrlChanged() {
//        runBlocking {
//            `when`(responseSource.contains(ArgumentMatchers.anyString())).thenReturn(
//                    false)
//            `when`(responseSource.add(ArgumentMatchers.anyString())).thenReturn(0L)
//            mainPresenter.onUrlChanged("test")
//            verify(mockView, times(1)).loadUrl("test")
//        }
//    }
//
//    @Test
//    fun onUtteranceStarted() {
//        val articleState = ArticleState("A name", listOf("One", "Two"))
//        mainPresenter.onUtteranceStarted(articleState)
//        verify(mockView, times(1)).highlightText(articleState)
//    }
//
//    @Test
//    fun onUtteranceEnded() {
//        val articleState = ArticleState("A name", listOf("One", "Two"))
//        mainPresenter.onUtteranceEnded(articleState)
//        verify(mockView, times(1)).unhighlightAllText()
//    }
//
//    @Test
//    fun onSpeechStopped() {
//        mainPresenter.onSpeechStopped()
//        verify(mockView, times(1)).enablePlayButton()
//    }
//
//    @Test
//    fun loadFromContext() {
//        val context = BasicArticleContext.fromString("Something")
//        runBlocking {
//            `when`(responseSource.add(ArgumentMatchers.anyString())).thenReturn(0L)
//            `when`(responseSource.contains(ArgumentMatchers.anyString())).thenReturn(
//                    false)
//            mainPresenter.loadFromContext(context)
//        }
//        verify(mockView, times(1)).loadUrl(
//                contextToUrl(context.contextString))
//        verify(mockView, times(1)).unhideWebView()
//    }
//
//    @Test
//    fun saveArticle() {
//        runBlocking {
//            mainPresenter.saveArticle()
//            verify(responseSource, times(1)).
//                    markPermanent("MAIN_PAGE")
//        }
//    }
//
//    @Test
//    fun courseDetailsRequested() {
//        val courseContext = ConcreteCourseContext(0L, "A name", 0)
//        runBlocking {
//            mainPresenter.courseDetailsRequested(courseContext)
//            verify(courseSource, times(1)).
//                    getArticlesForCourse(courseContext.id!!)
//        }
//    }
//
//    @Test
//    fun onDisplayReadingList() {
//        runBlocking {
//            mainPresenter.onDisplayReadingList()
//            verify(responseSource, times(1)).getAllPermanent()
//        }
//    }
//
//    @Test
//    fun onDisplayCourses() {
//        runBlocking {
//            mainPresenter.onDisplaySavedCourses()
//            verify(mockView, times(1)).displayCourses()
//        }
//    }
//
//    @Test
//    fun onPageDownloadFinished() {
//        runBlocking {
//            mainPresenter.onPageDownloadFinished("A String")
//            verify(responseSource, times(1)).markFinished(
//                    urlToContext("A String"))
//        }
//    }
}