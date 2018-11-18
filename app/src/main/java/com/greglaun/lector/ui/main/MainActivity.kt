package com.greglaun.lector.ui.main

import android.app.AlertDialog
import android.content.Intent
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
import com.greglaun.lector.android.okHttpToWebView
import com.greglaun.lector.data.cache.HashMapSavedArticleCache
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.titleToContext
import com.greglaun.lector.data.whitelist.HashSetWhitelist
import com.greglaun.lector.ui.speak.NoOpTtsPresenter
import com.greglaun.lector.ui.speak.TtsPresenter
import kotlinx.coroutines.experimental.runBlocking

class MainActivity : AppCompatActivity(), MainContract.View {
    val TAG: String = MainActivity::class.java.simpleName
    private lateinit var webView : WebView

    lateinit var mainPresenter : MainContract.Presenter
    var playMenuItem : MenuItem? = null
    var pauseMenuItem : MenuItem? = null
    lateinit var cacheDir : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview) as WebView
        webView.setWebViewClient(WikiWebViewClient())

        mainPresenter = MainPresenter(this, NoOpTtsPresenter(),
                createResponseSource())
        checkTts()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://en.m.wikipedia.org/wiki/Main_Page")
    }

    private fun createResponseSource(): ResponseSource {
        val whitelist: HashSetWhitelist<String> = HashSetWhitelist()
        return ResponseSource.createResponseSource(HashMapSavedArticleCache(), whitelist,
                getCacheDir())
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
        val androidAudioView = AndroidAudioView(androidTts)
        androidTts.setOnUtteranceProgressListener(androidAudioView)
        mainPresenter = MainPresenter(this, TtsPresenter(androidAudioView),
                mainPresenter.responseSource())
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
                mainPresenter.stopSpeakingAndEnablePlayButton()
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

    override fun loadUrl(urlString: String) {
        webView.loadUrl(urlString)
    }

    override fun enablePlayButton() {
        runOnUiThread {
            playMenuItem?.setVisible(true) // show play button
            pauseMenuItem?.setVisible(false) // hide the pause button
        }
    }

    override fun enablePauseButton() {
        runOnUiThread {
            playMenuItem?.setVisible(false) // hide play button
            pauseMenuItem?.setVisible(true) // show the pause button
        }
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

    override fun displayReadingList(readingList : List<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_reading_list_title))
        builder.setItems(readingList.toTypedArray()) { dialog, which ->
            mainPresenter.onUrlChanged("https://en.m.wikipedia.org/wiki/"
                    + titleToContext(readingList[which]))
        }
        builder.show()
    }

    inner class WikiWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
           if (request.url.authority.endsWith("wikipedia.org")) {
               mainPresenter.onUrlChanged(request.url.toString())
               return true
           }
           if (request.url.authority.endsWith("wikimedia.org")) {
               return false
           }
           val intent = Intent(Intent.ACTION_VIEW, request.url)
           startActivity(intent)
           return false
       }

       override fun onPageFinished(view: WebView?, url: String?) {
           super.onPageFinished(view, url)
           var result = webView.evaluateJavascript(
                   "(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();"
           ) { html ->
               Log.d("HTML", html)
               // code here
           }
       }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        if (request.url.authority.endsWith("wikipedia.org")) {
            return runBlocking {
                okHttpToWebView(mainPresenter.onRequest(request.url.toString()).await()!!)
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

   }
}