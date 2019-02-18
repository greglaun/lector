package com.greglaun.lector.data.net

interface UnfinishedDownloadSource {
    suspend fun getUnfinished(): List<String>
    suspend fun markFinished(urlString: String)
}