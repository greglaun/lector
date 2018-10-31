package com.greglaun.lector.data.model.speakable

import com.greglaun.lector.TextProvider

// A temporary class to be deleted after refactoring properly
class TmpTxtBuffer {
    private val MAX_TEXTS_IN_BUFFER = 10
    val internalBuffer = ArrayList<String>()
    var cursor = 0

    fun size(): Int {
        return internalBuffer.size
    }

    fun getCurrentAndAdvance(): String {
        val current = getCurrent()
        cursor += 1
        return current
    }

    private fun getCurrent(): String {
        return internalBuffer[cursor]
    }

    fun isEmpty(): Boolean {
        return internalBuffer.size == 0
    }

    fun addFromProvider(provider: TextProvider) {
        internalBuffer.addAll(provider.provideAllTexts())
    }

}