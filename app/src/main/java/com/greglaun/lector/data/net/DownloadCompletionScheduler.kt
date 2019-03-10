package com.greglaun.lector.data.net

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.concurrent.fixedRateTimer

class DownloadCompletionScheduler(
        val downloadCompleter: DownloadCompleter,
        val unfinishedDownloadSource: UnfinishedDownloadSource) {

    var fixedRateTimer: Timer? = null
    var isRunning: Boolean = false

    fun startDownloads() {
        fixedRateTimer = fixedRateTimer(name = "download-finish-timer",
                initialDelay = 100, period = 15 * 1000, daemon = true) {
            runBlocking {
                downloadCompleter.addUrlsFOrDownload(unfinishedDownloadSource.getUnfinished())
                downloadCompleter.downloadNextUrl { it ->
                    GlobalScope.launch {
                        unfinishedDownloadSource.markFinished(it)
                    }
                }
                isRunning = true
            }
        }

    }

    fun stopDownloads() {
        fixedRateTimer?.cancel()
        isRunning = false
    }
}

