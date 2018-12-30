package com.greglaun.lector.data.net

import kotlinx.coroutines.experimental.runBlocking
import java.util.*
import kotlin.concurrent.fixedRateTimer

class DownloadCompletionScheduler(
        val downloadCompleter: DownloadCompleter,
        val unfinishedDownloadSource: UnfinishedDownloadSource) {

    var fixedRateTimer: Timer? = null

    fun startDownloads() {
        fixedRateTimer = fixedRateTimer(name = "download-finish-timer",
                initialDelay = 100, period = 15 * 1000, daemon = true) {
            runBlocking {
                downloadCompleter.addUrlsFOrDownload(unfinishedDownloadSource.getUnfinished().await())
                downloadCompleter.downloadNextUrl { it ->
                    unfinishedDownloadSource.markFinished(it)
                }
            }
        }

    }

    fun stopDownloads() {
        fixedRateTimer?.cancel()
    }
}

