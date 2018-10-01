package com.greglaun.lector.data.model.html

interface HtmlDocument  {
    fun fromString (inputHtml : String) : HtmlDocument
    fun markCurrent(currentElement : HtmlElement) // Mark the given element as current
    fun annotateElements() // Add integer values to all elements
    fun toHtmlSpeakable() : HtmlSpeakable
}