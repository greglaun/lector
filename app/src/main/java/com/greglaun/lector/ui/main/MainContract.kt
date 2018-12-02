package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.ui.base.LectorPresenter
import com.greglaun.lector.ui.base.LectorView
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Response

interface MainContract {
    interface View : LectorView {
        fun loadUrl(urlString : String)
        fun enablePlayButton()
        fun enablePauseButton()
        fun displayReadingList(readingList: List<ArticleContext>)
        fun highlightText(textToHighlight: String, onDone: ((String) -> Unit)? = null)
    }

    interface Presenter : LectorPresenter<View> {
        fun onPlayButtonPressed()
        fun stopSpeakingAndEnablePlayButton()
        fun saveArticle()
        fun deleteArticle(url : String)
        fun onUrlChanged(url : String)
        fun onRequest(url : String) : Deferred<Response?>
        fun onDisplayReadingList()
        fun onArticleOver()
        fun responseSource(): ResponseSource
        fun loadFromContext(articleContext: ArticleContext)
    }
}