package com.greglaun.lector.ui.main

import android.os.Build
import com.greglaun.lector.TextSpeaker
import com.greglaun.lector.data.model.speakable.TTSContract

class MainPresenter(val view : MainContract.View, val ttsView : TTSContract.AudioView)
    : MainContract.Presenter {
    val textSpeaker = TextSpeaker(ttsView)

    fun prepareTextSpeaker() {
        textSpeaker.setEndOfArticleCallback(ArticleCleanupCallback())
        prepareForSpeaking(Uri.parse(getString(R.string.starting_url)))
    }

    private fun stopSpeaking() {
        isPlaying = false
    }

    private fun startSpeaking() {
        isPlaying = true
        textSpeaker.startSpeaking(textProvider)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.invalidateOptionsMenu()
        }
    }

    fun loadURL(url : String) {
        webView.loadUrl(url)
    }

    fun loadNewArticle(html :String) {
        webView.loadDataWithBaseURL(TextProvider.WIKI_BASE, html, "text/html", "utf-8", null)
    }

    private fun onURLChanged(url: Uri) {
        if (isWikiUrl(url)) {
            prepareForSpeaking(url)
        }
    }

    private val WIKI_LANGUAGE = "en"

    private fun isWikiUrl(url: Uri): Boolean {
        return (url.toString().startsWith("https://" + WIKI_LANGUAGE + ".wikipedia.org/wiki")
                || url.toString().startsWith(
                "https://" + WIKI_LANGUAGE + ".m.wikipedia.org/wiki")
                && !url.toString().contains("File:")
                )
    }

    private fun prepareForSpeaking(url: Uri) {
        textSpeaker.flush()
        textProvider = JSoupTextProvider(url)
    }

    internal inner class ArticleCleanupCallback : TextSpeaker.Callback {
        override fun call() {
            ReadingListProvider.deletePlace(textProvider.title, this@MainActivity)
        }
    }

    internal inner class NewURLCallback : WikiWebViewClient.URLChangedCallback {
        override fun call(uri: Uri) {
            onURLChanged(uri)
        }
    }

    override fun onPlayButtonPressed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPauseBottonPressed() {
        textSpeaker.stopSpeaking()
        view.enablePlayButton()
    }

    override fun onUrlChanged(url: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPageStarted(url: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRequest(url: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAttach(lectorView: MainContract.View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDetach() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLectorView(): MainContract.View? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}