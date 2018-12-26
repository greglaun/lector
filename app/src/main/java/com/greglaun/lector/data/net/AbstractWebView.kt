package com.greglaun.lector.data.net

interface AbstractWebView {
    fun downloadUrl(urlString: String, onDone: (Unit) -> Unit)
}