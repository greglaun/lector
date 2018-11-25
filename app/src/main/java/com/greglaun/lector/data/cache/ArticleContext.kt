package com.greglaun.lector.data.cache

interface ArticleContext {
    val id: Long?
    val contextString: String
    val position: String
    val temporary: Boolean
}