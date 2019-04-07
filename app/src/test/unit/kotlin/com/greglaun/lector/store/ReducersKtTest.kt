package com.greglaun.lector.store

import com.greglaun.lector.data.cache.BasicArticleContext
import com.greglaun.lector.data.course.ConcreteCourseContext
import com.greglaun.lector.data.course.EmptyCourseContext
import com.greglaun.lector.data.course.extractCourseMap
import com.greglaun.lector.data.course.toCourseDetailsMap
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.next
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

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
                newState)

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
                newState)
        newState = reduceRewindOne(
                newState)


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
        val courseContext = ConcreteCourseContext(0L, "A name", 0)

        var newState = reduceFetchCourseDetailsAction(
                ReadAction.FetchCourseDetailsAction(courseContext),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen.currentReadingList,
                courseContext.courseName)
        assertEquals(newState.readingListScreen.articles,
                Lce.Loading)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceUpdateSpeakerState() {
        var newState = reduceUpdateSpeakerState(
                UpdateAction.UpdateSpeakerStateAction(SpeakerState.SPEAKING),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, SpeakerState.SPEAKING)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceSpeakAction() {
        var newState = reduceSpeakAction(
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, SpeakerState.SPEAKING_NEW_UTTERANCE)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceStopSpeakingAction() {

        var newState = reduceStopSpeakingAction(
                State())
        assertEquals(newState.speakerState, SpeakerState.NOT_READY)

        newState = reduceSpeakAction(
                State())

        assertEquals(newState.speakerState, SpeakerState.SPEAKING_NEW_UTTERANCE)

        newState = reduceStopSpeakingAction(
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, SpeakerState.NOT_READY)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceArticleOverAction() {
        var newState = reduceSpeakAction(
                State())

        newState = reduceArticleOverAction(
                newState)

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
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
    fun testReduceUpdateArticleFreshnessState() {
        var newState = reduceUpdateArticleFreshnessState(
                UpdateAction.UpdateArticleFreshnessAction(
                        State().currentArticleScreen.articleState as ArticleState),
                State())

        assertEquals(newState.currentArticleScreen.articleState,
                State().currentArticleScreen.articleState)
        assertEquals(newState.currentArticleScreen.currentCourse,
                State().currentArticleScreen.currentCourse)
        assertEquals(newState.currentArticleScreen.newArticle, false)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceFetchAllPermanentAndDisplay() {
        var newState = reduceFetchAllPermanentAndDisplay(
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen.currentReadingList,
                State().readingListScreen.currentReadingList)
        assertEquals(newState.readingListScreen.articles, Lce.Loading)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, Navigation.MY_READING_LIST)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceUpdateReadingList() {
        val readingListLce = Lce.Success(listOf(
                BasicArticleContext(1L, "Blah", "", true)))

        var newState = reduceUpdateReadingList(
                UpdateAction.UpdateReadingListAction(readingListLce = readingListLce),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen.currentReadingList,
                State().readingListScreen.currentReadingList)
        assertEquals(newState.readingListScreen.articles, readingListLce)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceUpdateCourseBrowseList() {
        val courseListLce = Lce.Success(listOf(EmptyCourseContext()))

        var newState = reduceUpdateCourseBrowseList(
                UpdateAction.UpdateCourseBrowseList(courseListLce),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen.availableCourses,
                courseListLce)
        assertEquals(newState.navigation, Navigation.BROWSE_COURSES)
        assertEquals(newState.preferences, State().preferences)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, State().preferenceChanged)
    }

    @Test
    fun testReduceSetHandsomeBritish() {
        var newState = reduceSetHandsomeBritish(
                PreferenceAction.SetHandsomeBritish(true),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences.autoDelete, State().preferences.autoDelete)
        assertEquals(newState.preferences.speechRate, State().preferences.speechRate)
        assertEquals(newState.preferences.autoPlay, State().preferences.autoPlay)
        assertEquals(newState.preferences.isBritish, true)
        assertEquals(newState.preferences.isSlow, State().preferences.isSlow)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, true)
    }

    @Test
    fun testReduceSetSpeechRate() {
        var newState = reduceSetSpeechRate(
                PreferenceAction.SetSpeechRate(123.1f),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences.autoDelete, State().preferences.autoDelete)
        assertEquals(newState.preferences.speechRate, 123.1f)
        assertEquals(newState.preferences.autoPlay, State().preferences.autoPlay)
        assertEquals(newState.preferences.isBritish, State().preferences.isBritish)
        assertEquals(newState.preferences.isSlow, State().preferences.isSlow)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, true)
    }

    @Test
    fun testReduceSetAutoPlay() {
        var newState = reduceSetAutoPlay(
                PreferenceAction.SetAutoPlay(false),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences.autoDelete, State().preferences.autoDelete)
        assertEquals(newState.preferences.speechRate, State().preferences.speechRate)
        assertEquals(newState.preferences.autoPlay, false)
        assertEquals(newState.preferences.isBritish, State().preferences.isBritish)
        assertEquals(newState.preferences.isSlow, State().preferences.isSlow)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, true)
    }

    @Test
    fun testReduceSetAutoDelete() {
        var newState = reduceSetAutoDelete(
                PreferenceAction.SetAutoDelete(false),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences.autoPlay, State().preferences.autoPlay)
        assertEquals(newState.preferences.autoDelete, false)
        assertEquals(newState.preferences.speechRate, State().preferences.speechRate)
        assertEquals(newState.preferences.isBritish, State().preferences.isBritish)
        assertEquals(newState.preferences.isSlow, State().preferences.isSlow)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, true)
    }

    @Test
    fun testReduceSetIsSlow() {
        var newState = reduceSetIsSlow(
                PreferenceAction.SetIsSlow(true),
                State())

        assertEquals(newState.currentArticleScreen, State().currentArticleScreen)
        assertEquals(newState.readingListScreen, State().readingListScreen)
        assertEquals(newState.courseBrowserScreen, State().courseBrowserScreen)
        assertEquals(newState.navigation, State().navigation)
        assertEquals(newState.preferences.autoDelete, State().preferences.autoDelete)
        assertEquals(newState.preferences.speechRate, State().preferences.speechRate)
        assertEquals(newState.preferences.autoPlay, State().preferences.autoPlay)
        assertEquals(newState.preferences.isBritish, State().preferences.isBritish)
        assertEquals(newState.preferences.isSlow, true)
        assertEquals(newState.background, State().background)
        assertEquals(newState.changed, State().changed)
        assertEquals(newState.speakerState, State().speakerState)
        assertEquals(newState.preferenceChanged, true)
    }
}