package com.greglaun.lector.ui.speak

data class ArticleState(val title: String,
                        val paragraphs: List<String>,
                        val iterator: ListIterator<String>)

