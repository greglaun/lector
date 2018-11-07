package com.greglaun.lector.ui.main

import com.greglaun.lector.ui.base.LectorPresenter
import com.greglaun.lector.ui.base.LectorView
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Response

interface MainContract {
    interface View : LectorView {
        fun loadUrl(urlString : String)
        fun startPlaying()
        fun stopPlaying()
        fun enablePlayButton()
        fun enablePauseButton()
        // todo(feature): highlightText(String)
    }

    interface Presenter : LectorPresenter<View> {
        fun onPlayButtonPressed()
        fun onPauseBottonPressed()
        fun saveArticle(url : String)
        fun deleteArticle(url : String)
        fun onUrlChanged(url : String)
        fun onRequest(url : String) : Deferred<Response?>
        fun onDisplayReadingList()
    }
}