package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId

const val DEFAULT_ARTICLE = "MAIN_PAGE"

// todo(cleanup): Remove storage of utteranceId? It was originally here to guard against changes to
// todo(continued): the ordering of the paragraphs. But that is probably not a concern anymore.
data class ArticlePosition(val index: Int = 0,
                           val utteranceId: String = "")

interface AbstractArticleState {
    val title: String
    val paragraphs: List<String>
    val currentPosition: ArticlePosition
}

data class ArticleState(override val title: String,
                        override val paragraphs: List<String> = emptyList(),
                        override val currentPosition: ArticlePosition = ArticlePosition()): AbstractArticleState

val EmptyArticleState = ArticleState(DEFAULT_ARTICLE,
        emptyList(),
        ArticlePosition(0, ""))


fun AbstractArticleState.currentIndex(): Int {
    return currentPosition.index
}

fun AbstractArticleState.hasNext(): Boolean {
    return currentIndex() >= 0 && currentIndex() < paragraphs.size - 1
}

fun AbstractArticleState.next(): ArticleState? {
    if (!hasNext()) {
        return null
    }
    val nextIndex = currentPosition.index + 1
    val nextUtteranceId = utteranceId(paragraphs[nextIndex])
    return ArticleState(title, paragraphs, ArticlePosition(nextIndex, nextUtteranceId))
}

fun AbstractArticleState.hasPrevious(): Boolean {
    return currentIndex() > 0
}

fun AbstractArticleState.previous(): ArticleState? {
    if (!hasPrevious()) {
        return null
    }
    val previousPosition = currentIndex() - 1
    val previousUtteranceId = utteranceId(paragraphs[previousPosition])
    return ArticleState(title, paragraphs, ArticlePosition(previousPosition, previousUtteranceId))
}

fun AbstractArticleState.current(): String? {
    if (currentIndex() < 0 || currentIndex() >= paragraphs.size) {
        return null
    }
    return paragraphs[currentIndex()]
}

fun AbstractArticleState.scrubTo(index: Int): ArticleState {
    if (index < paragraphs.size) {
        return ArticleState(title, paragraphs,
                ArticlePosition(index, utteranceId(paragraphs[index])))
    }
    return this as ArticleState
}

fun AbstractArticleState.scrubTo(positionHash: String): ArticleState {
    if (positionHash == "") {
        return this as ArticleState
    }
    val matches = paragraphs.withIndex().filter{
        utteranceId(it.value) == positionHash
    }.map {it.index}
    if (matches.isEmpty()) {
        return this as ArticleState
    }
    return this.scrubTo(matches[0])
}