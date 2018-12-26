package com.greglaun.lector.data.download

import kotlinx.coroutines.experimental.Deferred

interface DownloadCompleter {
    fun addUrlsFOrDownload(urlStrings: List<String>)
    fun downloadUrls(): Deferred<Unit>
    fun isDone(): Boolean
}