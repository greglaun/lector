package com.greglaun.lector.data.net

class NoOpDownloadTool: DownloadTool {
    override fun downloadUrl(urlString: String, onDone: () -> Unit) {
        // Do nothing
    }
}