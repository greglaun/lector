package com.greglaun.lector.data.net

import arrow.core.Either
import com.greglaun.lector.data.model.html.WikiDocument
import io.reactivex.Observable
import java.net.URI

interface WikiFetcher {
    // Fetch the HTML document only
    fun fetchTextOnly(uri : URI) : Observable<Either<NetworkError, String>>

    // Fetch the HTMl document, the CSS document, and images, and populate a WikiDocument
    fun fetchArticle(uri : URI) : Observable<Either<NetworkError, WikiDocument>>

    // Fetch the HTMl document, the CSS document, and images, and populate a WikiDocument given
    // Wikipedia's article id.
    fun fetchArticle(articleId : Int) : Observable<Either<NetworkError, WikiDocument>>

    // Fetch the HTMl document, the CSS document, and images, and populate a WikiDocument given
    // Wikipedia's article id.
    fun fetchRelated(articleId : Int) : Observable<Either<NetworkError, List<WikiDocument>>>
}