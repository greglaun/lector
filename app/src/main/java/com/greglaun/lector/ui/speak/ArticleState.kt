package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId

val DEFAULT_ARTICLE = "MAIN_PAGE"

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


//data class EmptyArticleState(override val title: String = DEFAULT_ARTICLE,
//                             override val paragraphs: List<String> = emptyList(),
//                             override val currentPosition: ArticlePosition =
//                                     ArticlePosition(0, "")): AbstractArticleState

fun articleStatefromTitle(title: String): ArticleState {
    return ArticleState(title, emptyList(), ArticlePosition())
}

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
    val nextUtteranceId = utteranceId(paragraphs.get(nextIndex))
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
    return paragraphs.get(currentIndex())
}