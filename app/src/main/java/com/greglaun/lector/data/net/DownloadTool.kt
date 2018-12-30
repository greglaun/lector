package com.greglaun.lector.data.net

interface DownloadTool {
    fun downloadUrl(urlString: String, onDone: () -> Unit)
}