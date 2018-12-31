package com.greglaun.lector.android

import android.app.Activity
import android.webkit.WebView
import com.greglaun.lector.android.webview.WikiWebViewClient
import com.greglaun.lector.data.cache.contextToUrl
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.net.DownloadTool
import com.greglaun.lector.ui.main.MainContract

class WebviewDownloadTool(val webView: WebView, mainPresenter: MainContract.Presenter,
                          val activity: Activity):
        DownloadTool {

    var downloadCallbacks: HashMap<String, (() -> Unit)?> = HashMap()

    init {
        webView.webViewClient = WikiWebViewClient(mainPresenter, {
            false
        }, { it ->
            it?.let {
                urlToContext(it).let {
                    if (downloadCallbacks.containsKey(it)) {
                        // todo: Find better solution to allow download to finish
                        Thread.sleep(1000) // Wait for download to actually finish. A hack.
                        downloadCallbacks.get(it)?.invoke()
                        downloadCallbacks.remove(it)
                    }
                }
            }
        })
    }

    override fun downloadUrl(urlString: String, onDone: () -> Unit) {
        downloadCallbacks.put(urlString, onDone)
        activity.runOnUiThread {
            webView.loadUrl(contextToUrl(urlString))
        }
    }
}