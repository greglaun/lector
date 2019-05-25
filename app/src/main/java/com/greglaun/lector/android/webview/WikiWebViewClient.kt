package com.greglaun.lector.android.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.greglaun.lector.android.okHttpToWebView
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.store.ReadAction
import com.greglaun.lector.store.Store
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response

class WikiWebViewClient(val store: Store,
                        val responseSource: ResponseSource,
                        val onNonWikiUrl: (WebResourceRequest) -> Boolean,
                        val onPageDone: (urlString: String?) -> Unit):
        WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (request.url.authority!!.endsWith("wikipedia.org")) {
            if (request.url.path.endsWith("index.php")) {
                return false
            }
            GlobalScope.launch {
                store.dispatch(ReadAction.LoadNewUrlAction(request.url.toString()))
            }
            return true
        }
        if (request.url.authority!!.endsWith("wikimedia.org")) {
            return false
        }
        return onNonWikiUrl(request)
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest):
            WebResourceResponse? {
        if (request.url.authority!!.endsWith("wikipedia.org")) {
            return runBlocking {
                val response = onRequest(request.url.toString())
                if (response == null) {
                    null
                } else {
                    okHttpToWebView(response)
                }
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageDone(url)
    }

    private suspend fun onRequest(url: String): Response? {
        val currentContext = store.state.currentArticleScreen.articleState.title
        return responseSource.getWithContext(Request.Builder()
                .url(url)
                .build(), currentContext)
    }
}
