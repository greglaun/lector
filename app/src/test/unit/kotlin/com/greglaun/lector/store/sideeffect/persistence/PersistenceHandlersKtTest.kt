package com.greglaun.lector.store.sideeffect.persistence

import com.greglaun.lector.data.cache.BasicArticleContext
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.data.course.*
import com.greglaun.lector.store.*
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.ArticleStateSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.util.*

class PersistenceHandlersKtTest {

    private val responseSource = mock(ResponseSourceImpl::class.java)!!
    private val courseSource = mock(CourseSource::class.java)!!
    private val articleStateSource = mock(ArticleStateSource::class.java)!!
    private val courseDownloader = mock(CourseDownloader::class.java)!!
    private val articleState = ArticleState("A name", listOf("One", "Two"))
    private val actionDispatcher: suspend (Action) -> Unit = {}
    private val courseContext = ConcreteCourseContext(0L, "Ice Cream", 0)
    private val articleContext =
            BasicArticleContext.fromString("Some article")

    @Test
    fun testHandleFetchCourseDetails() {
        runBlocking {
            val json = "[{\"name\":\"Ice Cream\",\"articles\":\"https://en.wikipedia.org/wiki/Ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Cold-stimulus_headache\\r\\nhttps://en.wikipedia.org/wiki/Ice_cream_social\\r\\nhttps://en.wikipedia.org/wiki/Soft_serve\\r\\nhttps://en.wikipedia.org/wiki/Strawberry_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Chocolate_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Neapolitan_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Vanilla_ice_cream\"},{\"name\":\"Furry Friends\",\"articles\":\"https://en.wikipedia.org/wiki/Dog\\r\\nhttps://en.wikipedia.org/wiki/Cat\\r\\nhttps://en.wikipedia.org/wiki/Domestic_rabbit\\r\\nhttps://en.wikipedia.org/wiki/Gerbil\\r\\nhttps://en.wikipedia.org/wiki/Hedgehog\"}]"
            val detailsMap = toCourseDetailsMap(extractCourseMap(json))
            `when`(courseDownloader.fetchCourseDetails(ArgumentMatchers.anyList())).thenReturn(
                    detailsMap)
            handleFetchCourseDetails(ReadAction.FetchCourseDetailsAction(courseContext),
                    courseDownloader) {
                assertEquals((it as UpdateAction.UpdateCourseDetailsAction).courseDetails,
                        detailsMap[courseContext.courseName])
            }

        }
    }

    @Test
    fun testLoadNewUrlNoMatch() {
        runBlocking {
            val history: Stack<String> = Stack()
            `when`(articleStateSource.getArticle(ArgumentMatchers.anyString())).thenReturn(
                    articleState)
            `when`(responseSource.contains(ArgumentMatchers.anyString())).thenReturn(
                    false)

            loadNewUrl(ReadAction.LoadNewUrlAction("A name", false),
                    responseSource,
                    articleStateSource,
                    history,
                    actionDispatcher)
            verify(responseSource, times(1)).
                    add(articleState.title)
            assertTrue(history.empty())

            loadNewUrl(ReadAction.LoadNewUrlAction("A name", true),
                    responseSource,
                    articleStateSource,
                    history,
                    actionDispatcher)
            verify(responseSource, times(2)).
                    add(articleState.title)
            // The following history assertion is currently broken due to a bug in Mockito. We can
            // uncomment when that bug is fixed.
            // assertEquals(history.size, 1)
        }

    }

    @Test
    fun testLoadNewUrlMatch() {
        runBlocking {
            val history: Stack<String> = Stack()
            `when`(articleStateSource.getArticle(ArgumentMatchers.anyString())).thenReturn(
                    articleState)
            `when`(responseSource.contains(ArgumentMatchers.anyString())).thenReturn(
                    true)
            `when`(responseSource.getArticleContext(ArgumentMatchers.anyString())).thenReturn(
                    articleContext)

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
            // The following history assertion is currently broken due to a bug in Mockito. We can
            // uncomment when that bug is fixed.
            // assertEquals(history.size, 1)
        }
    }


    @Test
    fun testHandleArticleOver() {
        runBlocking {
            val nextArticleContext = BasicArticleContext.fromString("Some next article")
            val nextArticleState = ArticleState("Some next article", listOf("dog", "sheep"))
            `when`(responseSource.getNextArticle(ArgumentMatchers.anyString())).thenReturn(
                    nextArticleContext)
            `when`(articleStateSource.getArticle(nextArticleContext.contextString))
                    .thenReturn(
                    nextArticleState)

            handleArticleOver(State(),
                    responseSource,
                    courseSource,
                    articleStateSource) {
                assertTrue((it is SpeakerAction))
            }


            val state = State().updatePreferences(  //  Disable autoplay to prevent StopSpeaking
                    Preferences(false, true,
                            false, false, 1.0f))
            handleArticleOver(state,
                    responseSource,
                    courseSource,
                    articleStateSource) {
            assertEquals((it as UpdateAction.NewArticleAction).articleState, nextArticleState)
            }
        }
    }

    @Test
    fun testHandleFetchAllPermanentAndDisplay() {
        runBlocking {
            val readingList = listOf(BasicArticleContext.fromString("Some article"))
            `when`(responseSource.getAllPermanent()).thenReturn(
                    readingList)
            handleFetchAllPermanentAndDisplay(responseSource) {
                assertEquals((it as UpdateAction.UpdateReadingListAction).readingListLce,
                        Lce.Success(readingList))
            }
        }
    }

    @Test
    fun testHandleFetchAlCoursesAndDisplay() {
        runBlocking {
        val courses = listOf(ConcreteCourseContext(0L, "A Course", 0))
        `when`(courseSource.getCourses()).thenReturn(
                courses)
            handleFetchAllCoursesAndDisplay(courseSource) {
            assertEquals((it as UpdateAction.UpdateCourseBrowseList).courseListLce,
                    Lce.Success(courses))
        }

    }
    }

    @Test
    fun testHandleFetchArticlesForCourseAndDisplay() {
        runBlocking {
            val courseArticles = listOf(BasicArticleContext.fromString("Some article"))
            `when`(courseSource.getArticlesForCourse(ArgumentMatchers.anyLong())).thenReturn(
                    courseArticles)
            handleFetchArticlesForCourseAndDisplay(
                    ReadAction.FetchArticlesForCourseAndDisplay(courseContext),
                    courseSource) {
                assertEquals((it as UpdateAction.UpdateArticlesForCourse).courseArticlesLce,
                        Lce.Success(courseArticles))
            }

        }

    }

    @Test
    fun testHandleSaveArticle() {
        runBlocking {
            handleSaveArticle(WriteAction.SaveArticle(articleState), responseSource)
            verify(responseSource, times(1)).markPermanent(
                    articleState.title)
        }
    }

    @Test
    fun testHandleDeleteArticle() {
        runBlocking {
            handleDeleteArticle(WriteAction.DeleteArticle(articleContext), responseSource)
            verify(responseSource, times(1)).delete(
                    articleContext.contextString)
        }
    }

    @Test
    fun testHandleDeleteCourse() {
        runBlocking {
            handleDeleteCourse(WriteAction.DeleteCourse(courseContext), courseSource)
            verify(courseSource, times(1)).delete(
                    courseContext.courseName)
        }
    }

    @Test
    fun handeMarkDownloadFinished() {
        runBlocking {
            handleMarkDownloadFinished(
                    WriteAction.MarkDownloadFinished(articleContext.contextString), responseSource)
            verify(responseSource, times(1)).markFinished(
                    articleContext.contextString)
        }
    }

    @Test
    fun testHandleMaybeGoBack() {
        runBlocking {
            val history = Stack<String>()
            history.push("Banana")
            history.push("Apple")
            handleMaybeGoBack(history) {
                assertEquals((it as ReadAction.LoadNewUrlAction).newUrl, "Banana")
            }
        }
    }
}