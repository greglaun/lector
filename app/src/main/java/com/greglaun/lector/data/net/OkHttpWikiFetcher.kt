package com.greglaun.lector.data.net

import arrow.core.Either
import com.greglaun.lector.data.model.html.WikiDocument
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.HttpURLConnection
import java.net.URI

class OkHttpWikiFetcher(private val client : OkHttpClient) : WikiFetcher {
    override fun fetchTextOnly(uri: URI): Observable<Either<NetworkError, String>> {
        val request = Request.Builder()
                .url(uri.toURL())
                .build()
        return Observable.fromCallable {
            val response = client.newCall(request).execute()
            if (HttpURLConnection.HTTP_OK != response.code()) {
                Either.Left(HttpError(response.code()))
            }
            Either.Right(response.body()!!.string())
        }
    }

    override fun fetchArticle(uri: URI): Observable<Either<NetworkError, WikiDocument>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetchArticle(articleId: Int): Observable<Either<NetworkError, WikiDocument>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetchRelated(articleId: Int): Observable<Either<NetworkError, List<WikiDocument>>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}