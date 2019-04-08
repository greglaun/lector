package com.greglaun.lector.data.net

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.concurrent.fixedRateTimer

class DownloadCompletionScheduler(
        private val downloadCompleter: DownloadCompleter,
        private val unfinishedDownloadSource: UnfinishedDownloadSource,
        var isRunning: Boolean = false) {

    private var fixedRateTimer: Timer? = null

    fun startDownloads() {
        fixedRateTimer = fixedRateTimer(name = "download-finish-timer",
                initialDelay = 100, period = 15 * 1000, daemon = true) {
            runBlocking {
                downloadCompleter.addUrlsFOrDownload(unfinishedDownloadSource.getUnfinished())
                downloadCompleter.downloadNextUrl {
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

