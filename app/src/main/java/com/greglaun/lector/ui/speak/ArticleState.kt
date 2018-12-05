package com.greglaun.lector.ui.speak

data class ArticleState(val title: String,
                        val paragraphs: List<String>,
                        // todo (immutability): Replace with index into paragraphs?
                        val iterator: ListIterator<String>)

