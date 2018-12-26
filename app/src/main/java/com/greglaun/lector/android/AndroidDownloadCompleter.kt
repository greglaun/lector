package com.greglaun.lector.android

import com.greglaun.lector.data.download.DownloadCompleter
import com.greglaun.lector.data.net.AbstractWebView
import com.greglaun.lector.data.net.InternetChecker
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

class AndroidDownloadCompleter(val internetChecker: InternetChecker, val webView: AbstractWebView)
    : DownloadCompleter {
    val articlesToDownload = hashMapOf<String, Boolean>()

    override fun addUrlsFOrDownload(urlStrings: List<String>) {
        urlStrings.forEach{
            if (!articlesToDownload.containsKey(it))
            articlesToDownload.put(it, false)
        }
    }

    override fun downloadUrls(): Deferred<Unit> {
        if (internetChecker.internetIsAvailable()) {
            return GlobalScope.async {
                articlesToDownload.forEach {
                    downloadOneArticle(it.key)
                }
            }
        } else {
            return CompletableDeferred(Unit)
        }
    }

    override fun isDone(): Boolean {
        var isDone = true
        articlesToDownload.forEach {
            isDone = isDone && it.value
        }
        return isDone
    }

    fun downloadOneArticle(urlString: String) {
        if (internetChecker.internetIsAvailable()) {
            webView.downloadUrl(urlString) {
                articlesToDownload.put(urlString, true)
            }
        }
    }
}