package com.greglaun.lector

interface TextProvider {

    fun provideOneText(): String
    fun provideText(n: Int): List<String>

    fun fastForwardTo(place: String): Boolean

    fun provideAllTexts(): List<String>

    companion object {
        val END_OF_STREAM = "com.greglaun.eof"
        val WIKI_BASE = "https://en.wikipedia.org/wiki/"
        val title: String? = null
        val html: String? = null
    }
}
