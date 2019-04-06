package com.greglaun.lector.store.sideeffect.persistence

import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.store.Action
import com.greglaun.lector.store.ReadAction
import com.greglaun.lector.store.State
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.ArticleStateSource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.util.*

class PersistenceHandlersKtTest {

    val responseSource = mock(ResponseSourceImpl::class.java)
    val courseSource = mock(CourseSource::class.java)
    val articleStateSource = mock(ArticleStateSource::class.java)
    val articleState = ArticleState("A name", listOf("One", "Two"))
    val actionDispatcher: suspend (Action) -> Unit = {}


    @Test
    fun testHandleFetchCourseDetails() {

    }

    @Test
    fun testLoadNewUrlNoMatch() {
        runBlocking {
            val history: Stack<String> = Stack()
            var state = State()
            `when`(articleStateSource.getArticle(ArgumentMatchers.anyString())).thenReturn(
                    articleState)
            `when`(responseSource.contains(ArgumentMatchers.anyString())).thenReturn(
                    false)

            loadNewUrl(ReadAction.LoadNewUrlAction("Some string", false),
                    responseSource,
                    articleStateSource,
                    history,
                    actionDispatcher)
            verify(responseSource, times(1)).
                    add(articleState.title)
            assertTrue(history.empty())

            loadNewUrl(ReadAction.LoadNewUrlAction("Some string", true),
                    responseSource,
                    articleStateSource,
                    history,
                    actionDispatcher)
            verify(responseSource, times(2)).
                    add(articleState.title)
            assertEquals(history.size, 1)

        }

    }

    @Test
    fun testLoadNewUrlMatch() {
        runBlocking {
            val history: Stack<String> = Stack()
            var state = State()
            `when`(articleStateSource.getArticle(ArgumentMatchers.anyString())).thenReturn(
                    articleState)
            `when`(responseSource.contains(ArgumentMatchers.anyString())).thenReturn(
                    true)
            loadNewUrl(ReadAction.LoadNewUrlAction("Some string", false),
                    responseSource,
                    articleStateSource,
                    history,
                    actionDispatcher)
            verify(responseSource, times(0)).
                    add(articleState.title)
            assertTrue(history.empty())

            loadNewUrl(ReadAction.LoadNewUrlAction("Some string", true),
                    responseSource,
                    articleStateSource,
                    history,
                    actionDispatcher)
            verify(responseSource, times(0)).
                    add(articleState.title)
            assertEquals(history.size, 1)
        }
    }


    @Test
    fun testHandleArticleOver() {
    }

    @Test
    fun testHandleFetchAllPermanentAndDisplay() {
    }

    @Test
    fun testHandleFetchAlCoursesAndDisplay() {
    }

    @Test
    fun testHandleFetchArticlesForCourseAndDisplay() {
    }

    @Test
    fun testHandleSaveArticle() {
    }

    @Test
    fun testHandleDeleteArticle() {
    }

    @Test
    fun testHandleDeleteCourse() {
    }

    @Test
    fun handeMarkDownloadFinished() {
    }

    @Test
    fun testHandleMaybeGoBack() {
    }
}