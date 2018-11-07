package com.greglaun.lector.ui.main

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.greglaun.lector.R
import com.greglaun.lector.android.AndroidAudioView
import com.greglaun.lector.android.OkHttpToWebView
import com.greglaun.lector.ui.speak.NoOpTtsView
import kotlinx.coroutines.experimental.runBlocking


class MainActivity : AppCompatActivity(), MainContract.View {
    val TAG: String = MainActivity::class.java.simpleName
    private lateinit var webView : WebView

    lateinit var mainPresenter : MainContract.Presenter
    lateinit var playMenuItem: MenuItem
    lateinit var pauseMenuItem: MenuItem
    lateinit var cacheDir : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview) as WebView
        webView.setWebViewClient(WikiWebViewClient())
        mainPresenter = MainPresenter(this, NoOpTtsView(), getCacheDir())
        checkTts()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://en.m.wikipedia.org/wiki/Main_Page")
    }

    override fun onResume() {
        super.onResume()
        checkTts()
    }

    fun checkTts() {
        // todo(android): Clean this up, it is horribly messy.
        var androidTts : TextToSpeech? = null
         androidTts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (androidTts == null) onBadTts()
             when(it) {
                 TextToSpeech.SUCCESS -> onSuccessfulTts(androidTts!!)
                 TextToSpeech.ERROR -> onBadTts()
             }
        })
    }

    private fun onSuccessfulTts(androidTts: TextToSpeech) {
        // todo(concurrency): This should be called on the UI thread. Should we lock?
        mainPresenter = MainPresenter(this, AndroidAudioView(androidTts), getCacheDir())
    }

    private fun onBadTts() {
        Log.d(TAG, "Null Tts")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
        onPause()
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
                mainPresenter.onPlayButtonPressed()
                return true
            }
            R.id.action_pause -> {
                mainPresenter.onPauseBottonPressed()
                return true
            }
            R.id.action_save -> {
                mainPresenter.saveArticle(webView.url)
                return true
            }
            R.id.action_reading_list -> {
                mainPresenter.onDisplayReadingList()
                return true
            }
            R.id.action_delete -> {
                mainPresenter.deleteArticle(webView.url)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val isPlaying = false
        if (isPlaying) {
            playMenuItem.setVisible(false) // hide play button
            pauseMenuItem.setVisible(true) // show the pause button
        } else if (!isPlaying) {
            playMenuItem.setVisible(true) // show play button
            pauseMenuItem.setVisible(false) // hide the pause button
        }
        return true
    }

    override fun loadUrl(urlString: String) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enablePlayButton() {

    }

    override fun enablePauseButton() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(resId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(resId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

   inner class WikiWebViewClient : WebViewClient() {
        var currentURL : Uri = Uri.parse("https://en.m.wikipedia.org/wiki/Main_Page")

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url != null) {
                mainPresenter.onUrlChanged(url)
            }
        }

       override fun onPageFinished(view: WebView?, url: String?) {
           super.onPageFinished(view, url)
           var result = webView.evaluateJavascript(
                   "(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();"
           ) { html ->
               Log.d("HTML", html)
               // code here
           }
           println("Happy")
       }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        if (request.url.authority.substringAfter('.') == "wikipedia.org") {
            return runBlocking {
                OkHttpToWebView(mainPresenter.onRequest(request.url.toString()).await()!!)
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

   }
}