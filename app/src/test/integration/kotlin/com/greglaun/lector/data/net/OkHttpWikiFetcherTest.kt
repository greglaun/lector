package com.greglaun.lector.data.net

import okhttp3.OkHttpClient
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

/**
 * Test of the Speakable interface
 *
 */
class OkHttpWikiFetcherTest {
    val testUrlString = "https://en.wikipedia.org/robots.txt"

    @Test
    @Throws(Exception::class)
    fun fetch_text_only() {
        val uri = URI(testUrlString)

        val client = OkHttpClient()
        val wikiFetcher = OkHttpWikiFetcher(client)
        wikiFetcher.fetchTextOnly(uri).subscribe{ result ->
            assertTrue(result.get().contains("grub-client"))
            assertTrue(result.get().contains("https://bugzilla.wikimedia.org/show_bug.cgi?id=14075"))
        }
    }
}