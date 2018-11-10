package com.greglaun.lector.data.cache

fun urlToContext(urlString : String) : String {
    return urlString.substringBeforeLast("#").substringAfterLast("wiki/")
}