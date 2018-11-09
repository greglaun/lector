package com.greglaun.lector

interface TextProvider {

    fun provideText(n: Int): List<String>

    fun fastForwardTo(place: String): Boolean

    fun provideAllTexts(): List<String>

    companion object {
        val title: String? = null
        val html: String? = null
    }
}
