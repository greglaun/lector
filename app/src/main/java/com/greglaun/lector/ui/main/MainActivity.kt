package com.greglaun.lector.ui.main

import android.annotation.SuppressLint
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
import com.greglaun.lector.LectorApplication
import com.greglaun.lector.R
import com.greglaun.lector.android.*
import com.greglaun.lector.android.bound.BindableTtsService
import com.greglaun.lector.android.webview.WikiWebViewClient
import com.greglaun.lector.data.LruCallbackList
import com.greglaun.lector.store.LECTOR_UNIVERSE
import com.greglaun.lector.store.Navigation
import com.greglaun.lector.store.UpdateAction
import com.greglaun.lector.ui.course.CourseBrowserActivity
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.TTSContract
import com.greglaun.lector.ui.speak.currentIndex
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), MainContract.View {
    private val tag: String = MainActivity::class.java.simpleName
    private lateinit var webView: WebView
    private lateinit var downloaderWebView: WebView

    private lateinit var readingListRecyclerView: RecyclerView
    private lateinit var readingListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var readingListViewManager: RecyclerView.LayoutManager

    private lateinit var readingListView: LinearLayout

    private lateinit var courseListRecyclerView: RecyclerView
    private lateinit var courseListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var courseListViewManager: RecyclerView.LayoutManager

    private val onLoadedCallbacks = LruCallbackList<String>()
    private lateinit var mainPresenter: MainContract.Presenter
    var ttsPresenter: TTSContract.Presenter? = null

    private lateinit var playMenuItem : MenuItem
    private lateinit var pauseMenuItem : MenuItem

    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val noisyAudioStreamReceiver = BecomingNoisyReceiver()

    private var bindableTtsService: BindableTtsService? = null
    private var bindableTtsServiceIsBound: Boolean = false

    private var sharedPreferenceListener: LectorPreferenceChangeListener? = null

    /*
     * Lifecycle
     *
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainPresenter = MainPresenter(this, LectorApplication.AppStore)
        readingListView = findViewById(R.id.ll_reading_list)

        webView = findViewById(R.id.webview)
        webView.webViewClient = WikiWebViewClient(LectorApplication.AppStore,
                (application as LectorApplication).responseSource(), {
            val intent = Intent(Intent.ACTION_VIEW, it.url)
            startActivity(intent)
            false
        },{
            handleOnLoadCallbacks(it)
            expandCollapsableElements()
            // todo(javascript): How to avoid having to do this for slow-loading pages?
            GlobalScope.launch {
                Thread.sleep(1000)
                runOnUiThread {
                    expandCollapsableElements()
                }
            }
        })

        downloaderWebView = findViewById(R.id.downloader_webview)
        (application as LectorApplication).addDownloadCompletionSideEffect(
                WebviewDownloadTool(downloaderWebView, LectorApplication.AppStore,
                        (application as LectorApplication).responseSource(), this))

        readingListViewManager = LinearLayoutManager(this)
        courseListViewManager = LinearLayoutManager(this)

        setUpReadingListRecycler(mainPresenter as MainPresenter)
        setUpCourseListRecycler(mainPresenter as MainPresenter)

        onCreateSetupWebview()

        registerReceiver(noisyAudioStreamReceiver, intentFilter)

        mainPresenter.onAttach()
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(sharedPreferenceListener)

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun onCreateSetupWebview() {
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://en.m.wikipedia.org/wiki/Main_Page")
        Intent(this, BindableTtsService::class.java).also { intent ->
            bindService(intent, bindableTtsConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsPresenter?.onPauseButtonPressed()
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(sharedPreferenceListener)
        unregisterReceiver(noisyAudioStreamReceiver)
        if (bindableTtsService != null) {
            unbindService(bindableTtsConnection)
        }
        bindableTtsServiceIsBound = false
        mainPresenter.onDetach()
    }

    override fun onBackPressed() {
        // todo(unidirectional)
        val navigation = LectorApplication.AppStore.state.navigation
        when (navigation) {
            Navigation.CURRENT_ARTICLE -> {
                GlobalScope.launch {
                    mainPresenter.maybeGoToPreviousArticle()
                }
            }
            else -> GlobalScope.launch {
                LectorApplication.AppStore.dispatch(UpdateAction.UpdateNavigationAction(
                        Navigation.CURRENT_ARTICLE))
            }
        }
    }

    /*
     * Service
     *
     */

    private val bindableTtsConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BindableTtsService.LocalBinder
            bindableTtsService = binder.getService()
            bindableTtsServiceIsBound = true
            checkTts()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bindableTtsServiceIsBound = false
            bindableTtsService?.detach()
        }
    }

    private inner class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                ttsPresenter?.onPauseButtonPressed()
            }
        }
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

    private fun onBadTts() {
        Log.d(tag, "Null Tts")
    }

    /*
     * Presenter setup
     */

    private fun onSuccessfulTts(androidTts: TextToSpeech) {
        // todo(concurrency): This should be called on the UI thread. Should we lock?
        val androidAudioView = AndroidAudioView(androidTts)
        androidTts.setOnUtteranceProgressListener(androidAudioView)

        ttsPresenter = bindableTtsService
        ttsPresenter?.attach(androidAudioView, LectorApplication.AppStore)

        sharedPreferenceListener = LectorPreferenceChangeListener(mainPresenter, ttsPresenter!!)
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(sharedPreferenceListener)
        sharedPreferenceListener?.setFromPreferences(this)
    }

    /*
     * Recycler setup
     */

    private fun setUpReadingListRecycler(mainPresenter: MainPresenter) {
        readingListViewAdapter = ReadingListAdapter(mainPresenter.readingList, {
            GlobalScope.launch {
                mainPresenter.loadFromContext(it)
            }
        }, {
            mainPresenter.deleteRequested(it)
        }
        )

        readingListRecyclerView = findViewById<RecyclerView>(R.id.rv_reading_list).apply {
            setHasFixedSize(true)
            layoutManager = readingListViewManager
            adapter = readingListViewAdapter
        }
    }

    private fun setUpCourseListRecycler(mainPresenter: MainPresenter) {
        courseListViewAdapter = CourseListAdapter(mainPresenter.courseList, {
            GlobalScope.launch {
                mainPresenter.courseDetailsRequested(it)
            }
        }, {
            mainPresenter.deleteRequested(it)
        }
        )

        courseListRecyclerView = findViewById<RecyclerView>(R.id.rv_course_list).apply {
            setHasFixedSize(true)
            layoutManager = courseListViewManager
            adapter = courseListViewAdapter
        }
    }

    /*
     * View hiding/unhiding
     */

    override fun unHideReadingListView() {
        runOnUiThread {
            webView.visibility = GONE
            courseListRecyclerView.visibility = GONE
            readingListView.visibility = VISIBLE
        }
    }

    override fun unHideCourseListView() {
        runOnUiThread {
            webView.visibility = GONE
            readingListView.visibility = GONE
            courseListRecyclerView.visibility = VISIBLE
        }
    }

    override fun unhideWebView() {
        runOnUiThread {
            readingListView.visibility = GONE
            courseListRecyclerView.visibility = GONE
            webView.visibility = VISIBLE
        }
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

    override fun displayCourses() {
        runOnUiThread {
            unHideCourseListView()
        }
    }

    /*
     * Navigation
     */

    override fun navigateBrowseCourses() {
        startActivity(Intent(this, CourseBrowserActivity::class.java))
    }

    /*
     *  Options menu
     */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.action_menu, menu)
        this.playMenuItem = menu.findItem(R.id.action_play)
        pauseMenuItem = menu.findItem(R.id.action_pause)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_play -> {
                ttsPresenter?.onPlayButtonPressed()
                return true
            }
            R.id.action_pause -> {
                ttsPresenter?.onPauseButtonPressed()
                return true
            }
            R.id.action_save -> {
                GlobalScope.launch {
                    mainPresenter.saveArticle()
                }
                return true
            }
            R.id.action_reading_list -> {
                GlobalScope.launch {
                    mainPresenter.onDisplayReadingList()
                }
                return true
            }
            R.id.action_browse_courses -> {
                GlobalScope.launch {
                    mainPresenter.onBrowseCourses()
                }
                return true
            }
            R.id.action_my_courses -> {
                GlobalScope.launch {
                    mainPresenter.onDisplaySavedCourses()
                }
                return true
            }
            R.id.action_forward -> {
                GlobalScope.launch {
                    ttsPresenter?.onForwardOne()
                }
                return true
            }
            R.id.action_rewind -> {
                GlobalScope.launch {
                    ttsPresenter?.onRewindOne()
                }
                return true
            }
            R.id.action_settings -> {
                // todo(unidirectional)
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /*
     * Javascript
     */

    override fun highlightText(articleState: ArticleState, onDone: ((ArticleState, String)-> Unit)?) {
        // todo(javascript): Properly handle javascript?
        val index = articleState.currentIndex()
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

    /*
     * Webview
     */

    override fun loadUrl(urlString: String, onLoaded: (suspend (String)-> Unit)?) {
        onLoaded?.also {
            onLoadedCallbacks.push(Pair(urlString, onLoaded))
        }
        runOnUiThread {
            webView.loadUrl(urlString)
        }
    }

    private fun handleOnLoadCallbacks(urlString: String?) {
        urlString?.also {
            val callback = onLoadedCallbacks.get(urlString)
            GlobalScope.launch {
                callback?.invoke(it)
            }
        }
    }

    /*
     * Controlling playback
     */

    override fun enablePlayButton() {
        runOnUiThread {
            playMenuItem.isVisible = true // show play button
            pauseMenuItem.isVisible = false // hide the pause button
        }
    }

    override fun enablePauseButton() {
        runOnUiThread {
            playMenuItem.isVisible = false // hide play button
            pauseMenuItem.isVisible = true // show the pause button
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

    fun onPlayAllPressed(@Suppress("UNUSED_PARAMETER") view: View) {
        runOnUiThread {
            var title = LECTOR_UNIVERSE
            val viewText = findViewById<TextView>(R.id.reading_list_title).text
            if (viewText != null) {
                title = viewText.toString()
            }
            unhideWebView()
            mainPresenter.playAllPressed(title)
        }
    }

    /*
     * LectorView functions
     */

    override fun confirmMessage(message: String, yesButton: String,
                                noButton: String, onConfirmed: (Boolean) -> Unit) =
            AlertDialog.Builder(this)
                    .setMessage(message)
                    .setNegativeButton(android.R.string.no) { _: DialogInterface, _: Int ->
                        onConfirmed(false)
                    }
                    .setPositiveButton(android.R.string.yes) { _: DialogInterface, _: Int ->
                        onConfirmed(true)
                    }.create().show()

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
}