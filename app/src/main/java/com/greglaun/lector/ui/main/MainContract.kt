package com.greglaun.lector.ui.main

import com.greglaun.lector.ui.base.LectorPresenter
import com.greglaun.lector.ui.base.LectorView
import java.net.URI

interface MainContract {
    interface View : LectorView {
        fun loadUrl(Url : URI)
        fun startPlaying()
        fun stopPlaying()
        fun enablePlayButton()
        fun enablePauseButton()
        // todo(feature): highlightText(String)
    }

    interface Presenter : LectorPresenter<View> {
        fun onPlayButtonPressed()
        fun onPauseBottonPressed()
        fun onUrlChanged(url : String)
        fun onPageStarted(url : String)
        fun onRequest(url : String)
    }
}