package com.greglaun.lector.android.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.greglaun.lector.android.okHttpToWebView
import com.greglaun.lector.ui.main.MainContract
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

class WikiWebViewClient(val mainPresenter: MainContract.Presenter,
                        val onNonWikiUrl: (WebResourceRequest) -> Boolean,
                        val onPageDone: (urlString: String?) -> Unit):
        WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (request.url.authority.endsWith("wikipedia.org")) {
            GlobalScope.launch {
                mainPresenter.onUrlChanged(request.url.toString())
            }
            return true
        }
        if (request.url.authority.endsWith("wikimedia.org")) {
            return false
        }
        return onNonWikiUrl(request)
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        if (request.url.authority.endsWith("wikipedia.org")) {
            return runBlocking {
                val response = mainPresenter.onRequest(request.url.toString())
                if (response == null) {
                    null
                } else {
                    okHttpToWebView(response!!)
                }
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageDone(url)
    }
}
