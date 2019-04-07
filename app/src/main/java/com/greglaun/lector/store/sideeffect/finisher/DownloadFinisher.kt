package com.greglaun.lector.store.sideeffect.finisher

import com.greglaun.lector.data.net.DownloadCompletionScheduler
import com.greglaun.lector.store.Action
import com.greglaun.lector.store.ReadAction
import com.greglaun.lector.store.SideEffect
import com.greglaun.lector.store.Store

class DownloadFinisher(val store: Store,
                       private val downloadCompletionScheduler: DownloadCompletionScheduler):
        SideEffect {

    override suspend fun handle(action: Action) {
        when (action) {
            is ReadAction.StartDownloadAction ->
                finishDownloadsInBackground()
            is ReadAction.StopDownloadAction -> {
                downloadCompletionScheduler.stopDownloads()
            }

        }
    }


    private fun finishDownloadsInBackground() {
        if (downloadCompletionScheduler.isRunning) {
            return
        }
        downloadCompletionScheduler.startDownloads()
    }
}