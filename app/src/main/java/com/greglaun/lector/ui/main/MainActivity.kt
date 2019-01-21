package com.greglaun.lector.ui.main

import android.app.AlertDialog
import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.greglaun.lector.R
import com.greglaun.lector.android.*
import com.greglaun.lector.android.bound.BindableTtsService
import com.greglaun.lector.android.room.LectorDatabase
import com.greglaun.lector.android.room.RoomCacheEntryClassifier
import com.greglaun.lector.android.room.RoomCourseSource
import com.greglaun.lector.android.room.RoomSavedArticleCache
import com.greglaun.lector.android.webview.WikiWebViewClient
import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import com.greglaun.lector.ui.course.CourseBrowserActivity
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.NoOpTtsPresenter
import com.greglaun.lector.ui.speak.TtsPresenter
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity(), MainContract.View {
    val TAG: String = MainActivity::class.java.simpleName
    private lateinit var webView: WebView
    private lateinit var downloaderWebView: WebView

    private lateinit var readingListRecyclerView: RecyclerView
    private lateinit var readingListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var readingListViewManager: RecyclerView.LayoutManager

    private lateinit var readingListView: LinearLayout

    private lateinit var courseListRecyclerView: RecyclerView
    private lateinit var courseListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var courseListViewManager: RecyclerView.LayoutManager

    lateinit var mainPresenter : MainContract.Presenter
    var playMenuItem : MenuItem? = null
    var pauseMenuItem : MenuItem? = null

    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val noisyAudioStreamReceiver = BecomingNoisyReceiver()

    private lateinit var bindableTtsService: BindableTtsService
    private var bindableTtsServiceIsBound: Boolean = false

    private var RESPONSE_SOURCE_INSTANCE: ResponseSource? = null
    private var sharedPreferenceListener: LectorPreferenceChangeListener? = null

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

        mainPresenter = MainPresenter(this, NoOpTtsPresenter(),
                createResponseSource(), RoomCourseSource(LectorDatabase.getInstance(this)!!))

        readingListView = findViewById(R.id.ll_reading_list)

        webView = findViewById(R.id.webview) as WebView
        webView.webViewClient = WikiWebViewClient(mainPresenter, {
            val intent = Intent(Intent.ACTION_VIEW, it.url)
            startActivity(intent)
            false
        },{
            expandCollapsableElements()
            // todo(javascript): How to avoid having to do this for slow-loading pages?
            GlobalScope.launch {
                Thread.sleep(1000)
                runOnUiThread {
                    expandCollapsableElements()
                }
                it?.let {
                    mainPresenter.onPageDownloadFinished(it)
                }
            }
        })

        downloaderWebView = findViewById(R.id.downloader_webview) as WebView
        mainPresenter.downloadCompleter = AndroidDownloadCompleter(
                AndroidInternetChecker(this),
                WebviewDownloadTool(downloaderWebView, mainPresenter, this))

        readingListViewManager = LinearLayoutManager(this)
        courseListViewManager = LinearLayoutManager(this)

        sharedPreferenceListener = LectorPreferenceChangeListener(mainPresenter)
        sharedPreferenceListener?.setFromPreferences(this)

        renewReadingListRecycler(mainPresenter as MainPresenter)
        renewCourseListRecycler(mainPresenter as MainPresenter)

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
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(sharedPreferenceListener)
        unregisterReceiver(noisyAudioStreamReceiver)
        if (bindableTtsService != null) {
            unbindService(bindableTtsConnection)
        }
        bindableTtsServiceIsBound = false
    }


    private fun createResponseSource(): ResponseSource {
        if (RESPONSE_SOURCE_INSTANCE == null) {
            val db = LectorDatabase.getInstance(applicationContext)
            val cacheEntryClassifier: CacheEntryClassifier<String> = RoomCacheEntryClassifier(db!!)
            RESPONSE_SOURCE_INSTANCE = ResponseSourceImpl.createResponseSource(
                    RoomSavedArticleCache(db),
                    cacheEntryClassifier,
                    getCacheDir())
        }
        return RESPONSE_SOURCE_INSTANCE!!
    }

    override fun onResume() {
        super.onResume()
        mainPresenter.onAttach()
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(sharedPreferenceListener)
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
                mainPresenter.responseSource(), mainPresenter.courseSource())
        renewReadingListRecycler(mainPresenter as MainPresenter)
        renewCourseListRecycler(mainPresenter as MainPresenter)
        mainPresenter.onAttach()

        mainPresenter.downloadCompleter = mainPresenter.downloadCompleter

        webView.webViewClient = WikiWebViewClient(mainPresenter, {
            val intent = Intent(Intent.ACTION_VIEW, it.url)
            startActivity(intent)
            false
        },{
            expandCollapsableElements()
            // todo(javascript): How to avoid having to do this for slow-loading pages?
            GlobalScope.launch {
                Thread.sleep(1000)
                runOnUiThread {
                    expandCollapsableElements()
                }
            }
        })

        sharedPreferenceListener = LectorPreferenceChangeListener(mainPresenter)
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(sharedPreferenceListener)
        sharedPreferenceListener?.setFromPreferences(this)
    }

    private fun renewReadingListRecycler(mainPresenter: MainPresenter) {
        readingListViewAdapter = ReadingListAdapter(mainPresenter.readingList, { it: ArticleContext ->
            mainPresenter.loadFromContext(it)
        }, { it: ArticleContext ->
            mainPresenter.deleteRequested(it)
        }
        )

        readingListRecyclerView = findViewById<RecyclerView>(R.id.rv_reading_list).apply {
            setHasFixedSize(true)
            layoutManager = readingListViewManager
            adapter = readingListViewAdapter
        }
    }

    private fun renewCourseListRecycler(mainPresenter: MainPresenter) {
        courseListViewAdapter = CourseListAdapter(mainPresenter.courseList, { it: CourseContext ->
            mainPresenter.courseDetailsRequested(it)
        }, { it: CourseContext ->
            mainPresenter.deleteRequested(it)
        }
        )

        courseListRecyclerView = findViewById<RecyclerView>(R.id.rv_course_list).apply {
            setHasFixedSize(true)
            layoutManager = courseListViewManager
            adapter = courseListViewAdapter
        }
    }

    private fun onBadTts() {
        Log.d(TAG, "Null Tts")
    }

    override fun onBackPressed() {
        if (webView.visibility != VISIBLE) {
          unhideWebView()
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onPause()
        }
    }

    override fun unHideReadingListView() {
        webView.visibility = GONE
        courseListRecyclerView.visibility = GONE
        readingListView.visibility = VISIBLE
    }

    override fun unHideCourseListView() {
        webView.visibility = GONE
        readingListView.visibility = GONE
        courseListRecyclerView.visibility = VISIBLE
    }

    override fun unhideWebView() {
        readingListView.visibility = GONE
        courseListRecyclerView.visibility = GONE
        webView.visibility = VISIBLE
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
            R.id.action_browse_courses -> {
                startActivity(Intent(this, CourseBrowserActivity::class.java))
                return true
            }
            R.id.action_my_courses -> {
                mainPresenter.onDisplayCourses()
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
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
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

    override fun onReadingListChanged() {
        runOnUiThread {
            readingListViewAdapter.notifyDataSetChanged()
        }
    }

    override fun onCoursesChanged() {
        runOnUiThread {
            courseListViewAdapter.notifyDataSetChanged()
        }
    }

    override fun confirmMessage(message: String, yesButton: String, noButton: String, onConfirmed: (Boolean) -> Unit) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton(android.R.string.no, { dialogInterface: DialogInterface, i: Int ->
                    onConfirmed(false)
                })
                .setPositiveButton(android.R.string.yes, { dialogInterface: DialogInterface, i: Int ->
                    onConfirmed(true)
                }).create().show()
    }

    override fun confirmMessage(resourceId: Int, yesButton: Int, noButton: String, onConfirmed: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(resourceId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(resourceId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun displayReadingList(title: String?) {
        runOnUiThread {
            title?.let {
                val titleView =findViewById<TextView>(R.id.reading_list_title)
                titleView.text = title
            }
            unHideReadingListView()
        }
    }

    fun onPlayAllPressed(view: View) {
        runOnUiThread {
            var title = mainPresenter.LECTOR_UNIVERSE
            val viewText = findViewById<TextView>(R.id.reading_list_title).text
            if (viewText != null) {
                title = viewText.toString()
            }
            unhideWebView()
            mainPresenter.playAllPressed(title)
        }
    }

    override fun displayCourses() {
        runOnUiThread {
            unHideCourseListView()
        }
    }

    override fun evaluateJavascript(js: String, callback: ((String) -> Unit)?) {
        webView.evaluateJavascript(js, callback)
    }

    private fun expandCollapsableElements() {
        // todo (javascript): Only run on appropriate urls
        // todo (javascript): Do we need "item.previousSibling.className+=' open-block';"?
        val js = "var blocks = document.getElementsByTagName('h2');" +
                "if (blocks.length > 0) {" +
                "for (let item of blocks) {" +
                "item.className+=' open-block';" +
                "if (!!item.previousSibling) { item.previousSibling.className+=' open-block';}" +
                "}}"
        mainPresenter.evaluateJavascript(js, null)
    }
}