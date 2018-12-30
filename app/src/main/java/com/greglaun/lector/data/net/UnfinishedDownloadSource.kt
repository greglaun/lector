package com.greglaun.lector.data.net

import kotlinx.coroutines.experimental.Deferred

interface UnfinishedDownloadSource {
    fun getUnfinished(): Deferred<List<String>>
    fun markFinished(urlString: String): Deferred<Unit>
}