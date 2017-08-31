package org.leafcutter.webviewapplication

import android.content.DialogInterface
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
        prepareForSpeaking(Uri.parse(wikiURL))
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
                isPlaying = true
                textSpeaker.startSpeaking(textProvider)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    this.invalidateOptionsMenu()
                }
                return true
            }
            R.id.action_pause -> {
                isPlaying = false
                textSpeaker.stopSpeaking()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    this.invalidateOptionsMenu()
                }
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
                textProvider = JSoupTextProvider(html)
                myWebView.loadData(html, "text/html", "utf-8")
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
}
