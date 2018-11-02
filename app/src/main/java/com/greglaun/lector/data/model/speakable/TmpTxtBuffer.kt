package com.greglaun.lector.data.model.speakable

import com.greglaun.lector.TextProvider
import kotlin.math.min

// A temporary class to be deleted after refactoring properly
class TmpTxtBuffer {
    private val MAX_TEXTS_IN_BUFFER = 10
    val internalBuffer = ArrayList<String>()
    var cursor = 0

    fun size(): Int {
        return internalBuffer.size
    }

    fun advance() {
        cursor += 1
    }

    fun rewind() {
        cursor = min(0, cursor - 1)
    }

    fun getCurrent(): String {
        if (cursor < internalBuffer.size) {
            return internalBuffer[cursor]
        }
        return ""; // todo(refactor): Use proper error handling
    }

    fun isEmpty(): Boolean {
        return internalBuffer.size == 0
    }

    fun addFromProvider(provider: TextProvider) {
        if (provider != null) {
            // todo(debug): Why is the first text often (always) null
            internalBuffer.addAll(provider.provideAllTexts())
        }
    }

    fun clear() {
        cursor = 0
        internalBuffer.clear()
    }

}