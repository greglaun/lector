package com.greglaun.lector.android

import com.greglaun.lector.data.net.DownloadCompleter
import com.greglaun.lector.data.net.DownloadTool
import com.greglaun.lector.data.net.InternetChecker

class AndroidDownloadCompleter(private val internetChecker: InternetChecker,
                               private val downloadTool: DownloadTool)
    : DownloadCompleter {
    private val articlesToDownload = mutableSetOf<String>()

    override fun addUrlsFOrDownload(urlStrings: List<String>) {
        urlStrings.forEach {
            if (!articlesToDownload.contains(it))
                articlesToDownload.add(it)
        }
    }

    override fun downloadNextUrl(onArticleDownloaded: (String) -> Unit) {
        if (internetChecker.internetIsAvailable() && !articlesToDownload.isEmpty()) {
            val current = articlesToDownload.iterator().next()
            downloadTool.downloadUrl(current) {
                onArticleDownloaded(current)
                articlesToDownload.remove(current)
            }
        }
    }
}