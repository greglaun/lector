package com.greglaun.lector.data.model.html

import com.greglaun.lector.data.model.speakable.LinearSpeakable

interface HtmlSpeakable : LinearSpeakable {
    val htmlDocument : HtmlDocument
}