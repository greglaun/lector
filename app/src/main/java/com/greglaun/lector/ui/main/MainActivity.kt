package com.greglaun.lector.ui.main

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import com.greglaun.lector.R
import com.greglaun.lector.TextProvider
import com.greglaun.lector.android.AndroidAudioView
import com.greglaun.lector.ui.WikiWebViewClient
import com.greglaun.lector.ui.speak.TTSContract


class MainActivity : AppCompatActivity(), MainContract.View {
    val OFFLINE_MODE = true
    val TAG: String = MainActivity::class.java.simpleName
    private lateinit var webView : WebView
    private val wikiWebViewClient = WikiWebViewClient

    lateinit var ttsAudioView : AndroidAudioView
    lateinit var mainPresenter : MainContract.Presenter
    lateinit var ttsPresenter : TTSContract.Presenter
    @Volatile var isPlaying: Boolean = false
    lateinit var playMenuItem: MenuItem
    lateinit var pauseMenuItem: MenuItem
    lateinit var textProvider: TextProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview) as WebView
        webView.setWebViewClient(wikiWebViewClient)
        ttsAudioView.androidTts = TextToSpeech(this, ttsAudioView)
        mainPresenter = MainPresenter(this, ttsAudioView)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startPlaying() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stopPlaying() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enablePlayButton() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
}