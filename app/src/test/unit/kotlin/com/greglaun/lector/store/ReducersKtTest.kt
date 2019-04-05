package com.greglaun.lector.store

import com.greglaun.lector.data.course.extractCourseMap
import com.greglaun.lector.data.course.toCourseDetailsMap
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.next
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class ReducersKtTest {
    var articleState: ArticleState? = null

    @Before
    fun setUp() {
        articleState = ArticleState("a title", listOf("one", "two", "three"))
    }

    @Test
    fun testReduceUpdateArticleAction() {
        val newState = reduceUpdateArticleAction(UpdateAction.UpdateArticleAction(articleState!!),
                State())

        assertEquals(newState.currentArticleScreen.articleState, articleState)
        assertEquals(newState.currentArticleScreen.currentCourse,
                State().currentArticleScreen.currentCourse)
        assertEquals(newState.currentArticleScreen.newArticle, false)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, SpeakerState.READY)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceNewArticleAction() {
        val newState = reduceNewArticleAction(UpdateAction.NewArticleAction(articleState!!),
                State())

        assertEquals(newState.currentArticleScreen.articleState, articleState)
        assertEquals(newState.currentArticleScreen.currentCourse,
                State().currentArticleScreen.currentCourse)
        assertEquals(newState.currentArticleScreen.newArticle, true)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, SpeakerState.READY)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceUpdateNavigationAction() {
        val newState = reduceUpdateNavigationAction(
                UpdateAction.UpdateNavigationAction(Navigation.MY_READING_LIST),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, Navigation.MY_READING_LIST)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceFastForwardOne() {
        var newState = reduceUpdateArticleAction(UpdateAction.UpdateArticleAction(articleState!!),
                State())
        newState = reduceFastForwardOne(
                UpdateAction.FastForwardOne(), newState)

        assertEquals(newState.currentArticleScreen.articleState, articleState!!.next())
        assertEquals(newState.currentArticleScreen.currentCourse,
                State().currentArticleScreen.currentCourse)
        assertEquals(newState.currentArticleScreen.newArticle, false)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, SpeakerState.READY)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceRewindOne() {
        var newState = reduceUpdateArticleAction(UpdateAction.UpdateArticleAction(articleState!!),
                State())
        newState = reduceFastForwardOne(
                UpdateAction.FastForwardOne(), newState)
        newState = reduceRewindOne(
                UpdateAction.RewindOne(), newState)


        assertEquals(newState.currentArticleScreen.articleState.currentPosition.index,
                articleState!!.currentPosition.index)
        assertEquals(newState.currentArticleScreen.currentCourse,
                State().currentArticleScreen.currentCourse)
        assertEquals(newState.currentArticleScreen.newArticle, false)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, SpeakerState.READY)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceUpdateCourseDetailsAction() {
        val json = "[{\"name\":\"Ice Cream\",\"articles\":\"https://en.wikipedia.org/wiki/Ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Cold-stimulus_headache\\r\\nhttps://en.wikipedia.org/wiki/Ice_cream_social\\r\\nhttps://en.wikipedia.org/wiki/Soft_serve\\r\\nhttps://en.wikipedia.org/wiki/Strawberry_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Chocolate_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Neapolitan_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Vanilla_ice_cream\"},{\"name\":\"Furry Friends\",\"articles\":\"https://en.wikipedia.org/wiki/Dog\\r\\nhttps://en.wikipedia.org/wiki/Cat\\r\\nhttps://en.wikipedia.org/wiki/Domestic_rabbit\\r\\nhttps://en.wikipedia.org/wiki/Gerbil\\r\\nhttps://en.wikipedia.org/wiki/Hedgehog\"}]"
        val detailsMap = toCourseDetailsMap(extractCourseMap(json))
        val courseDetails = detailsMap.get("Ice Cream")

        var newState = reduceUpdateCourseDetailsAction(
                UpdateAction.UpdateCourseDetailsAction(courseDetails!!),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, Navigation.CURRENT_ARTICLE)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)

        reduceUpdateReadingList(UpdateAction.UpdateReadingListAction(
                "Ice Cream",
                        Lce.Success(emptyList())), newState)
        newState = reduceUpdateCourseDetailsAction(
                UpdateAction.UpdateCourseDetailsAction(courseDetails!!),
                newState)

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, Navigation.CURRENT_ARTICLE)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceFetchCourseDetailsAction() {
    }

    @Test
    fun testReduceUpdateSpeakerState() {
    }

    @Test
    fun testReduceStopSpeakingAction() {
    }

    @Test
    fun testReduceSpeakAction() {
    }

    @Test
    fun testReduceArticleOverAction() {
    }

    @Test
    fun testReduceUpdateArticleFreshnessState() {
    }

    @Test
    fun testReduceFetchAllPermanentAndDisplay() {
    }

    @Test
    fun testReduceUpdateReadingList() {
    }

    @Test
    fun testReduceUpdateCourseBrowseList() {
    }

    @Test
    fun testReduceSetHandsomeBritish() {
    }

    @Test
    fun testReduceSetSpeechRate() {
    }

    @Test
    fun testReduceSetAutoPlay() {
    }

    @Test
    fun testReduceSetAutoDelete() {
    }

    @Test
    fun testReduceSetIsSlow() {
    }
}