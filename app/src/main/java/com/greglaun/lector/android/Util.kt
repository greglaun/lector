package com.greglaun.lector.android

import android.webkit.WebResourceResponse
import okhttp3.Response

fun OkHttpToWebView(response : Response) : WebResourceResponse {
    return WebResourceResponse(
            response.header("content-type", "text/plain"),
            response.header("content-encoding", "utf-8"),
            response.body()!!.byteStream())
}