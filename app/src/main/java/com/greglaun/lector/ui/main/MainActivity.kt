package com.greglaun.lector.ui.main

import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import com.greglaun.lector.*
import com.greglaun.lector.ui.WikiWebViewClient


class MainActivity : AppCompatActivity() {

    val OFFLINE_MODE = true
    val TAG: String = MainActivity::class.java.simpleName
    private lateinit var webView : WebView
    private val wikiWebViewClient = WikiWebViewClient

    lateinit var mainPresenter : MainContract.Presenter
    @Volatile var isPlaying: Boolean = false
    lateinit var playMenuItem: MenuItem
    lateinit var pauseMenuItem: MenuItem
    lateinit var textProvider: TextProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview) as WebView
        webView.setWebViewClient(wikiWebViewClient)
        mainPresenter = MainPresenter(webView, tts)
    }

    fun prepareWebView() {
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
}