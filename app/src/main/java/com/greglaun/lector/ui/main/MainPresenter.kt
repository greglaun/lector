package com.greglaun.lector.ui.main

import com.greglaun.lector.TtsPresenter
import com.greglaun.lector.ui.speak.TTSContract

// todo(global state): Move to better place.
val STARTING_URL_STRING = "https://www.wikipedia.org/wiki/Main_Page"

class MainPresenter(val view : MainContract.View, ttsView : TTSContract.AudioView)
    : MainContract.Presenter {
    val ttsPresenter = TtsPresenter(ttsView)
    val WIKI_LANGUAGE = "en"


    override fun onAttach(lectorView: MainContract.View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDetach() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLectorView(): MainContract.View? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayButtonPressed() {
        ttsPresenter.startSpeaking()
    }

    override fun onPauseBottonPressed() {
        ttsPresenter.stopSpeaking()
        view.enablePlayButton()
    }

    override fun onUrlChanged(url: String) {
        view.loadUrl(url)
        ttsPresenter.onUrlChanged(url)
    }

    override fun onRequest(url: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}