package com.greglaun.lector.android

import android.app.Activity
import android.webkit.WebView
import com.greglaun.lector.android.webview.WikiWebViewClient
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.contextToUrl
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.net.DownloadTool
import com.greglaun.lector.store.Store

class WebviewDownloadTool(private val webView: WebView, store: Store,
                          responseSource: ResponseSource,
                          private val activity: Activity):
        DownloadTool {

    private var downloadCallbacks: HashMap<String, (() -> Unit)?> = HashMap()

    init {
        webView.webViewClient = WikiWebViewClient(store, responseSource, {
            false
        }, { urlString ->
            urlString?.let {
                urlToContext(it).let { contextString ->
                    if (downloadCallbacks.containsKey(contextString)) {
                        // todo: Find better solution to allow download to finish
                        Thread.sleep(1000) // Wait for download to actually finish. A hack.
                        downloadCallbacks[contextString]?.invoke()
                        downloadCallbacks.remove(contextString)
                    }
                }
            }
        })
    }

    override fun downloadUrl(urlString: String, onDone: () -> Unit) {
        downloadCallbacks[urlString] = onDone
        activity.runOnUiThread {
            webView.loadUrl(contextToUrl(urlString))
        }
    }
}