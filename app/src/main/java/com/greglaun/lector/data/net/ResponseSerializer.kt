package com.greglaun.lector.data.net

import okhttp3.*
import okhttp3.internal.http.HttpHeaders
import okhttp3.internal.http.StatusLine
import okio.*
import java.io.IOException
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*

val NEWLINE = '\n'.toInt()

// Initial iteration of code borrows heavily from OkHttp.
fun serializeResponse(response : Response, sink : Sink) {
    val sink = Okio.buffer(sink)

    val url = response.request().url().toString()
    val varyHeaders = HttpHeaders.varyHeaders(response)
    val requestMethod = response.request().method()
    val protocol = response.protocol()
    val code = response.code()
    val message = response.message()
    val responseHeaders = response.headers()
    val handshake = response.handshake()

    sink.writeUtf8(url)
            .writeByte(NEWLINE)
    sink.writeUtf8(requestMethod)
            .writeByte(NEWLINE)
    sink.writeDecimalLong(varyHeaders.size().toLong())
            .writeByte(NEWLINE)
    run {
        var i = 0
        val size = varyHeaders.size()
        while (i < size) {
            sink.writeUtf8(varyHeaders.name(i))
                    .writeUtf8(": ")
                    .writeUtf8(varyHeaders.value(i))
                    .writeByte(NEWLINE)
            i++
        }
    }

    sink.writeUtf8(StatusLine(protocol, code, message).toString())
            .writeByte(NEWLINE)
    sink.writeDecimalLong((responseHeaders.size()).toLong())
            .writeByte(NEWLINE)
    var i = 0
    val size = responseHeaders.size()
    while (i < size) {
        sink.writeUtf8(responseHeaders.name(i))
                .writeUtf8(": ")
                .writeUtf8(responseHeaders.value(i))
                .writeByte(NEWLINE)
        i++
    }

    if (isHttps(url)) {
        sink.writeByte(NEWLINE)
        sink.writeUtf8(handshake.cipherSuite().javaName())
                .writeByte(NEWLINE)
        writeCertList(sink, handshake.peerCertificates())
        writeCertList(sink, handshake.localCertificates())
        sink.writeUtf8(handshake.tlsVersion().javaName()).writeByte(NEWLINE)
    }

    val writtenContentLength = sink.writeAll(response.body()!!.source())
    sink.close()
}


fun deserializeResponse(input: Source) : Response {
    val response : Response
    // todo(beginner): Replace try-catch with try-with-resources
    try {
        val source = Okio.buffer(input)
        val url = source.readUtf8LineStrict()
        val requestMethod = source.readUtf8LineStrict()
        val varyHeadersBuilder = Headers.Builder()
        val varyRequestHeaderLineCount = readInt(source)
        for (i in 0 until varyRequestHeaderLineCount) {
            varyHeadersBuilder.add(source.readUtf8LineStrict())
        }
        val varyHeaders = varyHeadersBuilder.build()

        val statusLine = StatusLine.parse(source.readUtf8LineStrict())
        val protocol = statusLine.protocol
        val code = statusLine.code
        val message = statusLine.message
        val responseHeadersBuilder = Headers.Builder()
        val responseHeaderLineCount = readInt(source)
        for (i in 0 until responseHeaderLineCount) {
            responseHeadersBuilder.add(source.readUtf8LineStrict())
        }
        val responseHeaders = responseHeadersBuilder.build()
        var handshake  : Handshake? = null
        if (isHttps(url)) {
            val blank = source.readUtf8LineStrict()
            if (blank.length > 0) {
                throw IOException("expected \"\" but was \"$blank\"")
            }
            val cipherSuiteString = source.readUtf8LineStrict()
            val cipherSuite = CipherSuite.forJavaName(cipherSuiteString)
            val peerCertificates = readCertificateList(source)
            val localCertificates = readCertificateList(source)
            val tlsVersion = if (!source.exhausted())
                TlsVersion.forJavaName(source.readUtf8LineStrict())
            else
                TlsVersion.SSL_3_0
            handshake = Handshake.get(tlsVersion, cipherSuite, peerCertificates, localCertificates)
        }
        val contentType = responseHeaders.get("Content-Type")
        var contentLength = responseHeaders.get("Content-Length")?.toInt()
        if (null == contentLength) {
            contentLength = -1
        }
        val cacheRequest = Request.Builder()
                .url(url)
                .method(requestMethod, null)
                .headers(varyHeaders)
                .build()
        // Assume we can fit everything into memory. This should be okay for Wikipedia content
        return Response.Builder()
                .request(cacheRequest)
                .protocol(protocol)
                .code(code)
                .message(message)
                .headers(responseHeaders)
                .body(ResponseBody.create(MediaType.parse(contentType), source.readByteArray()))
                .handshake(handshake)
                .build()
    } finally {
        input.close()
    }
}


private fun writeCertList(sink: BufferedSink, certificates: List<Certificate>) {
    try {
        sink.writeDecimalLong(certificates.size.toLong())
                .writeByte(NEWLINE)
        var i = 0
        val size = certificates.size
        while (i < size) {
            val bytes = certificates[i].getEncoded()
            val line = ByteString.of(*bytes).base64()
            sink.writeUtf8(line)
                    .writeByte(NEWLINE)
            i++
        }
    } catch (e: CertificateEncodingException) {
        throw IOException(e.message)
    }

}

private fun readCertificateList(source: BufferedSource): List<Certificate> {
    val length = readInt(source)
    if (length == -1) return Collections.emptyList() // OkHttp v1.2 used -1 to indicate null.

    try {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val result = ArrayList<Certificate>(length)
        for (i in 0 until length) {
            val line = source.readUtf8LineStrict()
            val bytes = Buffer()
            bytes.write(ByteString.decodeBase64(line))
            result.add(certificateFactory.generateCertificate(bytes.inputStream()))
        }
        return result
    } catch (e: CertificateException) {
        throw IOException(e.message)
    }
}

fun readInt(source: BufferedSource): Int {
    try {
        val result = source.readDecimalLong()
        val line = source.readUtf8LineStrict()
        if (result < 0 || result > Integer.MAX_VALUE || !line.isEmpty()) {
            throw IOException("expected an int but was \"$result$line\"")
        }
        return result.toInt()
    } catch (e: NumberFormatException) {
        throw IOException(e.message)
    }
}

private fun isHttps(url: String) = url.startsWith("https://")
