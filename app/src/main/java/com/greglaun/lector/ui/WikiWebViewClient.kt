package com.greglaun.lector.ui

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.greglaun.lector.TextProvider

object WikiWebViewClient : WebViewClient() {
    var currentURL : Uri = Uri.parse("https://www.wikipedia.org/wiki/Main_Page")
    lateinit var urlChangedCallback : URLChangedCallback

    interface URLChangedCallback {
        fun call(uri : Uri)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url.equals(TextProvider.WIKI_BASE)) {
            return // Ignore WebKit telling us that we loaded data using loadData.
        }
        Log.d(TAG, "Page started: " + url)
        // TODO: Sort out policy for Uri class vs String class urls
        val uri = Uri.parse(url)
        currentURL = uri
        urlChangedCallback.call(uri)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        Log.d(TAG, "Should override url loading.")
        currentURL = request.url
        urlChangedCallback.call(request.url)
        return false
    }
}