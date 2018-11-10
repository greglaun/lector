package com.greglaun.lector.data.cache

fun urlToContext(urlString : String) : String {
    return urlString.substringBeforeLast("#").substringAfterLast("wiki/")
}

fun contextToTitle(requestContext : String) : String {
    return requestContext.replace("_", " ")
}

fun titleToContext(title : String) : String {
    return title.replace(" ", "_")
}