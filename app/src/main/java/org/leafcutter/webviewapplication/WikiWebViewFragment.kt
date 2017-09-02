package org.leafcutter.webviewapplication

import android.app.Fragment
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class WikiWebViewFragment : Fragment() {

    interface URLChangedCallback {
        fun call(uri : Uri)
    }

    private lateinit var myWebView: WebView
    private val TAG : String = WikiWebViewFragment::class.java.simpleName
    private lateinit var currentURL : Uri
    lateinit var urlChangedCallback : URLChangedCallback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)
        myWebView = view.findViewById(R.id.webview) as WebView
        myWebView.setWebViewClient(WikiWebViewClient())
        val wikiURL = getString(R.string.starting_url)
        myWebView.loadUrl(wikiURL)
        return view
    }

    fun loadNewArticle(html :String) {
        myWebView.loadDataWithBaseURL(TextProvider.WIKI_BASE, html, "text/html", "utf-8", null)
    }

    fun loadURL(url : String) {
        myWebView.loadUrl(url)
    }

    inner class WikiWebViewClient : WebViewClient() {
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
}