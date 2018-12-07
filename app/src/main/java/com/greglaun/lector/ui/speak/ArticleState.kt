package com.greglaun.lector.ui.speak

data class ArticleState(val title: String,
                        val paragraphs: List<String>,
                        val current_index: Int = 0)

fun ArticleState.hasNext(): Boolean {
    return current_index >= 0 && current_index < paragraphs.size - 1
}

fun ArticleState.next(): ArticleState? {
    if (!hasNext()) {
        return null
    }
    return ArticleState(title, paragraphs, current_index + 1)
}

fun ArticleState.hasPrevious(): Boolean {
    return current_index > 0
}

fun ArticleState.previous(): ArticleState? {
    if (!hasPrevious()) {
        return null
    }
    return ArticleState(title, paragraphs, current_index - 1)
}

fun ArticleState.current(): String? {
    if (current_index < 0 || current_index >= paragraphs.size) {
        return null
    }
    return paragraphs.get(current_index)
}