package com.greglaun.lector.data.net

interface DownloadCompleter {
    fun addUrlsFOrDownload(urlStrings: List<String>)
    fun downloadNextUrl(onArticleDownloaded: (String) -> Unit)
}