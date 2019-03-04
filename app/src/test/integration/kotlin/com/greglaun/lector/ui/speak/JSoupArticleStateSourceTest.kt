package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.HashMapSavedArticleCache
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.data.cache.contextToUrl
import com.greglaun.lector.data.whitelist.HashSetCacheEntryClassifier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class JSoupArticleStateSourceTest {

    @Test
    fun getArticle() {
        val responseSource = ResponseSourceImpl.createResponseSource(
                HashMapSavedArticleCache(),
                HashSetCacheEntryClassifier(),
                File("testDir"))
        val articleSource = JSoupArticleStateSource(responseSource)
        runBlocking {
            val banana = articleSource.getArticle(contextToUrl("Banana"))
            assertNotNull(banana)
            assertNotNull(banana!!.next())
        }


    }
}