package com.greglaun.lector.ui.main

import com.greglaun.lector.ui.base.LectorPresenter

class MainPresenter {


    // todo(refactor): Move to presenter
    fun prepareTextSpeaker() {
        textSpeaker = TextSpeaker(this)
        textSpeaker.setEndOfArticleCallback(ArticleCleanupCallback())
        prepareForSpeaking(Uri.parse(getString(R.string.starting_url)))
    }

    // todo(refactor): Move to presenter
    private fun loadCurrentPlace() {
        val html = ReadingListProvider.retrieveCurrentHTML(this)
        val currentPlace = ReadingListProvider.retrieveCurrentPlace(this)
        this.textProvider = JSoupTextProvider(html)
        this.textProvider.fastForwardTo(currentPlace)
    }

    // todo(refactor): Move to presenter
    private fun saveCurrentPlace(textProvider: TextProvider) {
        ReadingListProvider.saveCurrent(textProvider.html, textSpeaker.currentUtterance, this)
        if (!ReadingListProvider.retrieveArticle(textProvider.title, this).equals("")) {
            ReadingListProvider.savePlace(textProvider.title, textSpeaker.getCurrentUtterance(), this)
        }
    }

    // todo(refactor): Move to presenter
    private fun stopSpeaking() {
        isPlaying = false
        textSpeaker.stopSpeaking()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.invalidateOptionsMenu()
        }
    }

    // todo(refactor): Move to presenter
    private fun startSpeaking() {
        isPlaying = true
        textSpeaker.startSpeaking(textProvider)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.invalidateOptionsMenu()
        }
    }

    // todo(refactor): Move to presenter
    private fun deleteArticle() {
        ReadingListProvider.deleteArticle(textProvider.title, this)
    }

    // todo(refactor): Move to presenter
    fun getReadingList(): Array<out String>? {
        return ReadingListProvider.retrieveList(this)
    }

    // todo(refactor): Move to presenter
    private fun saveArticle(provider: TextProvider) {
        ReadingListProvider.saveArticle(provider.title, provider.html, this)
    }

    // todo(refactor): Move to presenter
    fun loadURL(url : String) {
        webView.loadUrl(url)
    }

    // todo(refactor): Move to presenter
    fun loadNewArticle(html :String) {
        webView.loadDataWithBaseURL(TextProvider.WIKI_BASE, html, "text/html", "utf-8", null)
    }

    // todo(refactor): Move to presenter
    private fun onURLChanged(url: Uri) {
        if (isWikiUrl(url)) {
            prepareForSpeaking(url)
        }
    }

    // todo(refactor): Move to presenter
    private val WIKI_LANGUAGE = "en"

    // todo(refactor): Move to presenter
    private fun isWikiUrl(url: Uri): Boolean {
        return (url.toString().startsWith("https://" + WIKI_LANGUAGE + ".wikipedia.org/wiki")
                || url.toString().startsWith(
                "https://" + WIKI_LANGUAGE + ".m.wikipedia.org/wiki")
                && !url.toString().contains("File:")
                )
    }

    // todo(refactor): Move to presenter
    private fun prepareForSpeaking(url: Uri) {
        textSpeaker.flush()
        textProvider = JSoupTextProvider(url)
    }

    // todo(refactor): Move to presenter
    internal inner class ArticleCleanupCallback : TextSpeaker.Callback {

        override fun call() {
            ReadingListProvider.deletePlace(textProvider.title, this@MainActivity)
        }
    }

    // todo(refactor): Move to presenter
    internal inner class NewURLCallback : WikiWebViewClient.URLChangedCallback {
        override fun call(uri: Uri) {
            onURLChanged(uri)
        }
    }
}