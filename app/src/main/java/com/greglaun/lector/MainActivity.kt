package com.greglaun.lector

import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem


class MainActivity : AppCompatActivity() {

    val OFFLINE_MODE = true
    val TAG: String = MainActivity::class.java.simpleName
    @Volatile lateinit var textSpeaker: TextSpeaker
    private lateinit var webFragment: WikiWebViewFragment

    // Play controls
    @Volatile var isPlaying: Boolean = false
    lateinit var playMenuItem: MenuItem
    lateinit var pauseMenuItem: MenuItem
    lateinit var textProvider: TextProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        switchToWebView()
        textSpeaker = TextSpeaker(this)
        textSpeaker.setEndOfArticleCallback(ArticleCleanupCallback())
        prepareForSpeaking(Uri.parse(getString(R.string.starting_url)))
    }

    private fun switchToWebView() {
        webFragment = WikiWebViewFragment()
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.viewFragment, webFragment as Fragment)
        ft.commit()
        webFragment.urlChangedCallback = NewURLCallback()
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
                webFragment.loadNewArticle(html)
                textProvider = JSoupTextProvider(html)
                textProvider.fastForwardTo(currentPlace)
            } else {
                val url = TextProvider.WIKI_BASE + readingList[which]
                textProvider = JSoupTextProvider(Uri.parse(url))
                webFragment.loadURL(url)
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


    private fun onURLChanged(url: Uri) {
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

    internal inner class NewURLCallback : WikiWebViewFragment.URLChangedCallback {
        override fun call(uri: Uri) {
            onURLChanged(uri)
        }
    }
}