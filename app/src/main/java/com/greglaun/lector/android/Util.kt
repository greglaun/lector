package com.greglaun.lector.android

import android.webkit.WebResourceResponse
import okhttp3.Response
import java.nio.charset.Charset

fun OkHttpToWebView(response : Response) : WebResourceResponse? {
    if (response.code() == 200) {
        return WebResourceResponse(response.body()!!.contentType()!!.type() + "/" + response.body()!!.contentType()!!.subtype(),
                response.body()!!.contentType()!!.charset(Charset.defaultCharset())!!.name(),
                response.body()!!.byteStream())
    }
    return null
}