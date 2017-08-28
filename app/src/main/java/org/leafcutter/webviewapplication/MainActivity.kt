package org.leafcutter.webviewapplication

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast






class MainActivity : AppCompatActivity() {

    lateinit var currentURL : Uri
    val TAG : String = MainActivity::class.java.simpleName
    @Volatile lateinit var textSpeaker : TextSpeaker

    // Play controls
    @Volatile var isPlaying : Boolean = false
    lateinit var playMenuItem : MenuItem
    lateinit var pauseMenuItem : MenuItem
    lateinit var currentProvider : TextProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myWebView = findViewById(R.id.webview) as WebView
        myWebView.setWebViewClient(WikiWebViewClient())
        val wikiURL = "https://en.wikipedia.org/wiki/Wikipedia:Today%27s_featured_article"
        myWebView.loadUrl(wikiURL)
        currentProvider = TextProvider { wikiURL }
        textSpeaker = TextSpeaker(this)
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
                textSpeaker.startSpeaking(currentProvider)
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
            else -> return super.onOptionsItemSelected(item)
        }
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
        Toast.makeText(this@MainActivity, url.toString(), Toast.LENGTH_LONG).show()
        prepareForSpeaking(url)
    }

    private fun prepareForSpeaking(url: Uri) {
        currentProvider = JSoupTextProvider(url.toString())
    }
}
