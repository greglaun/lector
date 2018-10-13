package com.greglaun.lector.data.net

import arrow.core.Either
import com.greglaun.lector.data.model.html.WikiDocument
import io.reactivex.Observable
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URI



class JsoupWikiFetcher(private val client : OkHttpClient) : WikiFetcher {
    override fun fetchTextOnly(uri: URI): Observable<Either<NetworkError, String>> {
        return Observable.fromCallable {
            try {
                val doc = jsoupGet(uri.toASCIIString())
                Either.Right(doc.toString())
            } catch (e : IOException) {
                Either.Left(NetworkException(e))
            }
        }
    }

    override fun fetchArticle(uri: URI): Observable<Either<NetworkError, WikiDocument>> {
        val myObservable = Observable.fromCallable {
            val doc = jsoupGet(uri.toASCIIString()!!)
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            val imageElements = doc.getElementsByTag("img")
//                        // todo(concurrency, measurement): Get empirical evidence vs downloading on one thread
//            Observable.fromArray(imageElements.asIterable())
//                    .flatMap { img -> img. }
////
//                    }
        } // Map over image elements and download all of them in parallel
        // Accumulate those images in an array list
        // Take that array list and the text, and place them in a WikiDocument.
        // If necessary, save the images to disk.,
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun fetchArticle(articleId: Int): Observable<Either<NetworkError, WikiDocument>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetchRelated(articleId: Int): Observable<Either<NetworkError, List<WikiDocument>>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun jsoupGet(urlString : String) : Document {
        return Jsoup.connect(urlString).get()
    }
}