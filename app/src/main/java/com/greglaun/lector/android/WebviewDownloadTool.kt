package com.greglaun.lector.android

import android.webkit.WebView
import com.greglaun.lector.android.webview.WikiWebViewClient
import com.greglaun.lector.data.net.DownloadTool
import com.greglaun.lector.ui.main.MainContract

class WebviewDownloadTool(val webView: WebView, mainPresenter: MainContract.Presenter):
        DownloadTool {

    var downloadCallbacks: HashMap<String, (() -> Unit)?> = HashMap()

    init {
        webView.webViewClient = WikiWebViewClient(mainPresenter, {
            false
        },{

        })
    }

    override fun downloadUrl(urlString: String, onDone: () -> Unit) {
        urlString?.also {
            downloadCallbacks.put(urlString, onDone)
        }
    }

    fun finishedDownload(urlString: String) {
        if (downloadCallbacks.containsKey(urlString)) {
            downloadCallbacks.get(urlString)?.invoke()
        }
    }
}