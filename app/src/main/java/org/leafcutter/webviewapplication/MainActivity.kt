package org.leafcutter.webviewapplication

import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient


class MainActivity : AppCompatActivity() {

    val OFFLINE_MODE = true
    val WIKI_BASE = "https://en.wikipedia.org/wiki/"
    lateinit var currentURL : Uri
    val TAG : String = MainActivity::class.java.simpleName
    @Volatile lateinit var textSpeaker : TextSpeaker

    // Play controls
    @Volatile var isPlaying : Boolean = false
    lateinit var playMenuItem : MenuItem
    lateinit var pauseMenuItem : MenuItem
    lateinit var textProvider: TextProvider
    lateinit var myWebView : WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myWebView = findViewById(R.id.webview) as WebView
        myWebView.setWebViewClient(WikiWebViewClient())
        val wikiURL = getString(R.string.starting_url)
        myWebView.loadUrl(wikiURL)
        textSpeaker = TextSpeaker(this)
        textSpeaker.setEndOfArticleCallback(ArticleCleanupCallback())
        prepareForSpeaking(Uri.parse(wikiURL))
    }

    override fun onPause() {
        super.onPause()
        saveCurrentPlace(textProvider)
        stopSpeaking()

    }

    override fun onResume() {
        super.onResume()
        loadCurrentPlace()
    }

    private fun loadCurrentPlace() {
        val html = ReadingListProvider.retrieveCurrentHTML(this)
        val currentPlace = ReadingListProvider.retrieveCurrentPlace(this)
        this.textProvider = JSoupTextProvider(html)
        this.textProvider.fastForwardTo(currentPlace)
    }

    private fun saveCurrentPlace(textProvider: TextProvider) {
        ReadingListProvider.saveCurrent(textProvider.html, textSpeaker.currentUtterance, this)
        if (!ReadingListProvider.retrieveArticle(textProvider.title, this).equals("")) {
            ReadingListProvider.savePlace(textProvider.title, textSpeaker.getCurrentUtterance(), this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.action_menu, menu)

        playMenuItem = menu.findItem(R.id.action_play)
        pauseMenuItem = menu.findItem(R.id.action_pause)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_play -> {
                startSpeaking()
                return true
            }
            R.id.action_pause -> {
                stopSpeaking()
                return true
            }
            R.id.action_save -> {
                saveArticle(textProvider)
                return true
            }
            R.id.action_reading_list -> {
                displayReadingList()
                return true
            }
            R.id.action_delete -> {
                deleteArticle()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun stopSpeaking() {
        isPlaying = false
        textSpeaker.stopSpeaking()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.invalidateOptionsMenu()
        }
    }

    private fun startSpeaking() {
        isPlaying = true
        textSpeaker.startSpeaking(textProvider)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.invalidateOptionsMenu()
        }
    }

    private fun deleteArticle() {
        ReadingListProvider.deleteArticle(textProvider.title, this)
    }

    private fun displayReadingList() {
        val readingList = ReadingListProvider.retrieveList(this);
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_reading_list_title))
        builder.setItems(readingList, DialogInterface.OnClickListener { dialog, which ->
            // TODO: Run on worker thread?
            if (OFFLINE_MODE) {
                val html = ReadingListProvider.retrieveArticle(readingList[which], this)
                val currentPlace = ReadingListProvider.retrievePlace(readingList[which], this)
                myWebView.loadDataWithBaseURL(WIKI_BASE, html, "text/html", "utf-8", null)
                textProvider = JSoupTextProvider(html)
                textProvider.fastForwardTo(currentPlace)
            } else {
                val url = WIKI_BASE + readingList[which]
                textProvider = JSoupTextProvider(Uri.parse(url))
                myWebView.loadUrl(url)
            }
        })
        builder.show()
    }

    private fun saveArticle(provider: TextProvider) {
        ReadingListProvider.saveArticle(provider.title, provider.html, this)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        if (isPlaying) {
            playMenuItem.setVisible(false) // hide play button
            pauseMenuItem.setVisible(true) // show the pause button
        } else if (!isPlaying) {
            playMenuItem.setVisible(true) // show play button
            pauseMenuItem.setVisible(false) // hide the pause button
        }

        return true
    }

    inner class WikiWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url.equals(WIKI_BASE)) {
                return // Ignore WebKit telling us that we loaded data using loadData.
            }
            Log.d(TAG, "Page started: " + url)
            // TODO: Sort out policy for Uri class vs String class urls
            val uri = Uri.parse(url)
            currentURL = uri
            onURLChanged(uri)
        }

        override fun shouldOverrideUrlLoading(view: WebView, request : WebResourceRequest): Boolean {
            Log.d(TAG, "Should override url loading.")
            currentURL = request.url
            onURLChanged(request.url)
            return false
        }
    }

    private fun onURLChanged(url : Uri) {
        prepareForSpeaking(url)
    }

    private fun prepareForSpeaking(url: Uri) {
        textProvider = JSoupTextProvider(url)
    }

    internal inner class ArticleCleanupCallback : TextSpeaker.Callback {

        override fun call() {
            ReadingListProvider.deletePlace(textProvider.title, this@MainActivity)
        }
    }
}
