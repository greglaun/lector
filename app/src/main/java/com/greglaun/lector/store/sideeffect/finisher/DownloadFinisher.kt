package com.greglaun.lector.store.sideeffect.finisher

import com.greglaun.lector.data.net.DownloadCompletionScheduler
import com.greglaun.lector.store.Action
import com.greglaun.lector.store.ReadAction
import com.greglaun.lector.store.SideEffect
import com.greglaun.lector.store.Store

class DownloadFinisher(val store: Store,
                      val downloadCompletionScheduler: DownloadCompletionScheduler): SideEffect {
    init {
        store.sideEffects.add(this)
    }

    override suspend fun handle(action: Action) {
        when (action) {
            is ReadAction.StartDownloadAction ->
                finishDownloadsInBackground(action) { store.dispatch(it) }
            is ReadAction.StopDownloadAction -> {
                downloadCompletionScheduler?.stopDownloads()
            }

        }
    }


    fun finishDownloadsInBackground(action: ReadAction.StartDownloadAction,
                                    actionDispatcher: suspend (Action) -> Unit) {
        if (downloadCompletionScheduler.isRunning) {
            return
        }
        downloadCompletionScheduler.startDownloads()
    }
}