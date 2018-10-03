package com.greglaun.lector.data.net

import com.greglaun.lector.data.model.html.WikiDocument
import java.net.URI

interface WikiFetcher {
    fun fetchArticle(uri : URI)
    fun fetchArticle(id : Int) : WikiDocument
    fun fetchRelated(id : Int) : List<WikiDocument>
}