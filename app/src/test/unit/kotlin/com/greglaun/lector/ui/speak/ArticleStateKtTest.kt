package com.greglaun.lector.ui.speak

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArticleStateKtTest {
    var articleState: ArticleState? = null

    @Before
    fun setUp() {
        articleState = ArticleState("a title", listOf("one", "two", "three"))
    }

    @Test
    fun hasNext() {
        assertTrue(articleState!!.hasNext())
    }

    @Test
    operator fun next() {
        assertTrue(articleState!!.hasNext())
        assertNull(ArticleState(articleState!!.title, listOf("0")).next())
    }

    @Test
    fun hasPrevious() {
        assertFalse(articleState!!.hasPrevious())
        assertTrue(articleState!!.next()!!.hasPrevious())
    }

    @Test
    fun previous() {
        assertFalse(articleState!!.hasPrevious())
        assertTrue(articleState!!.next()!!.hasPrevious())
    }

    @Test
    fun current() {
        assertTrue(false)
    }
}