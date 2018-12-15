package com.greglaun.lector.data.cache

import com.greglaun.lector.data.net.deserializeResponse
import com.greglaun.lector.data.net.serializeResponse
import okhttp3.Response
import okio.Okio
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.net.URLDecoder
import java.security.MessageDigest

fun urlToContext(urlString : String) : String {
    return URLDecoder.decode(urlString.substringBeforeLast("#").substringAfterLast("wiki/")
            .replace("_", " "), "UTF-8")
}

fun utteranceId(text : String) : String{
    return text.md5()
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16)
            .padStart(32, '0')
}

fun Response.serialize(): String {
    val byteArrayStream = ByteArrayOutputStream()
    val sink = Okio.sink(byteArrayStream)
    serializeResponse(this, sink)
    return String(byteArrayStream.toByteArray())
}

fun String.toResponse(): Response {
    val byteArraySource = ByteArrayInputStream(this.toByteArray())
    val source = Okio.source(byteArraySource)
    return deserializeResponse(source)
}