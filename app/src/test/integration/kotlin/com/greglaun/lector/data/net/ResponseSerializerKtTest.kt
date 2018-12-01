package com.greglaun.lector.data.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream



class ResponseSerializerKtTest {
    val testUrlString = "https://en.wikipedia.org/robots.txt"

    @Test
    fun serializeDeserialize() {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(testUrlString)
                .build()
        val networkResponse = client.newCall(request).execute()
        // todo(Okio) what is the sane way to do this with Okio?
        val byteArraySink = ByteArrayOutputStream()
        val sink = Okio.sink(byteArraySink)
        serializeResponse(networkResponse, sink)
        val byteArraySource = ByteArrayInputStream(byteArraySink.toByteArray())
        val source = Okio.source(byteArraySource)
        val deserializedResponse = deserializeResponse(source)
        assertTrue(networkResponse.code() == deserializedResponse.code())
        assertTrue(networkResponse.message() == deserializedResponse.message())
        assertTrue(networkResponse.headers().get("CONTENT-TYPE") != null)
        assertTrue(
                networkResponse.headers().get("CONTENT-TYPE") ==
                        deserializedResponse.headers().get("CONTENT-TYPE"))
        assertTrue(deserializedResponse.body() != null)
        assertTrue(deserializedResponse.body()!!.string().contains("Inktomi"))
    }
}