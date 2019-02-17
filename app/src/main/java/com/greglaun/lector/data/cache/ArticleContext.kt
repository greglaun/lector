package com.greglaun.lector.data.cache

val POSITION_BEGINNING: String = ""
interface ArticleContext {
    val id: Long?
    val contextString: String
    val position: String
    val temporary: Boolean
    val downloadComplete: Boolean
}