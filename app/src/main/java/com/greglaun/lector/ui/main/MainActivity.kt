package com.greglaun.lector.ui.main

import android.app.AlertDialog
import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
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
import com.greglaun.lector.android.bound.BindableTtsService
import com.greglaun.lector.android.okHttpToWebView
import com.greglaun.lector.android.room.ArticleCacheDatabase
import com.greglaun.lector.android.room.RoomCacheEntryClassifier
import com.greglaun.lector.android.room.RoomSavedArticleCache
import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.NoOpTtsPresenter
import com.greglaun.lector.ui.speak.TtsPresenter
import kotlinx.coroutines.experimental.runBlocking

class MainActivity : AppCompatActivity(), MainContract.View {
    val TAG: String = MainActivity::class.java.simpleName
    private lateinit var webView : WebView

    lateinit var mainPresenter : MainContract.Presenter
    var playMenuItem : MenuItem? = null
    var pauseMenuItem : MenuItem? = null

    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val noisyAudioStreamReceiver = BecomingNoisyReceiver()

    private lateinit var bindableTtsService: BindableTtsService
    private var bindableTtsServiceIsBound: Boolean = false

    private var RESPONSE_SOURCE_INSTANCE: ResponseSource? = null

    private val bindableTtsConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BindableTtsService.LocalBinder
            if (RESPONSE_SOURCE_INSTANCE != null) {
                bindableTtsService = binder.getService(responseSource = RESPONSE_SOURCE_INSTANCE!!)
                bindableTtsServiceIsBound = true
                checkTts()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bindableTtsServiceIsBound = false
        }
    }



    private inner class BecomingNoisyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                mainPresenter?.stopSpeakingAndEnablePlayButton()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview) as WebView
        webView.setWebViewClient(WikiWebViewClient())

        mainPresenter = MainPresenter(this, NoOpTtsPresenter(),
                createResponseSource())
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://en.m.wikipedia.org/wiki/Main_Page")
        Intent(this, BindableTtsService::class.java).also { intent ->
            bindService(intent, bindableTtsConnection, Context.BIND_AUTO_CREATE)
        }
        registerReceiver(noisyAudioStreamReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter.stopSpeakingAndEnablePlayButton()
        unregisterReceiver(noisyAudioStreamReceiver)
        if (bindableTtsService != null) {
            unbindService(bindableTtsConnection)
        }
        bindableTtsServiceIsBound = false
    }

    private fun createResponseSource(): ResponseSource {
        if (RESPONSE_SOURCE_INSTANCE == null) {
            val db = ArticleCacheDatabase.getInstance(this)
            val cacheEntryClassifier: CacheEntryClassifier<String> = RoomCacheEntryClassifier(db!!)
            RESPONSE_SOURCE_INSTANCE = ResponseSourceImpl.createResponseSource(RoomSavedArticleCache(db), cacheEntryClassifier,
                    getCacheDir())
        }
        return RESPONSE_SOURCE_INSTANCE!!
    }

    override fun onResume() {
        super.onResume()
        mainPresenter.onAttach()
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
        val ttsStateMachine = bindableTtsService
        mainPresenter = MainPresenter(this,
                TtsPresenter(androidAudioView, ttsStateMachine),
                mainPresenter.responseSource())
        mainPresenter.onAttach()
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
                mainPresenter.saveArticle()
                return true
            }
            R.id.action_reading_list -> {
                mainPresenter.onDisplayReadingList()
                return true
            }
            R.id.action_delete -> {
                mainPresenter.deleteCurrentArticle()
                return true
            }
            R.id.action_forward -> {
                mainPresenter.onForwardOne()
                return true
            }
            R.id.action_rewind -> {
                mainPresenter.onRewindOne()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun highlightText(articleState: ArticleState, onDone: ((ArticleState, String)-> Unit)?) {
        // todo(javascript): Properly handle javascript?
        val index = articleState.current_index
        val highlightColor = "yellow"
        val lectorClass = "lector-active"
        val js = "var txt = document.getElementsByTagName('p');" +
                 "txt[$index].classList.add('$lectorClass');" +
                 "txt[$index].style.backgroundColor = '$highlightColor';" +
                "var windowHeight = window.innerHeight;" +
                 "var xoff = txt[$index].offsetLeft;" +
                "var yoff = txt[$index].offsetTop;" +
                 "window.scrollTo(xoff, yoff - windowHeight/3);"

        runOnUiThread {
            webView.evaluateJavascript(js) {
                onDone?.invoke(articleState, it)
            }
        }
    }

    override fun unhighlightAllText() {
        // todo(javascript): Properly handle javascript?
        val lectorClass = "lector-active"
        val js = "var active = document.getElementsByClassName('$lectorClass');" +
                 "if (active.length > 0) {for (let item of active) {" +
                 "item.style.background='';" +
                 "item.classList.remove('$lectorClass'); }} "
        runOnUiThread {
            webView.evaluateJavascript(js) {}
        }
    }
    override fun loadUrl(urlString: String) {
        runOnUiThread {
            webView.loadUrl(urlString)
        }
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

    override fun displayReadingList(readingList : List<ArticleContext>) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            val stringList = ArrayList<String>()
            readingList.forEach {
                stringList.add(it.contextString)
            }
            builder.setTitle(getString(R.string.dialog_reading_list_title))
            builder.setItems(stringList.toTypedArray()) { dialog, which ->
                mainPresenter.loadFromContext(readingList[which])
            }
            builder.show()
        }
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

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            if (request.url.authority.endsWith("wikipedia.org")) {
                return runBlocking {
                    okHttpToWebView(mainPresenter.onRequest(request.url.toString()).await()!!)
                }
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            // todo (javascript): Only run on appropriate urls
            // todo (javascript): Do we need "item.previousSibling.className+=' open-block';"?
            val js ="var blocks = document.querySelectorAll('[id^=mf-section-]');" +
                     "if (blocks.length > 0) {" +
                      "for (let item of blocks) {" +
                    "item.className+=' open-block';" +
                     "}}"
            webView.evaluateJavascript(js, null)
        }
    }
}