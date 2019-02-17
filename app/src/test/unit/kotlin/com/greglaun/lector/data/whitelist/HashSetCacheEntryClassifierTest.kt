package com.greglaun.lector.data.whitelist

import com.greglaun.lector.data.cache.BasicArticleContext
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HashSetCacheEntryClassifierTest {
    val classifier = HashSetCacheEntryClassifier()
    val testString = "Potato"

    @Test
    fun notContains() {
        runBlocking {
            assertFalse(classifier.contains(testString))
        }
    }

    @Test
    fun contains() {
        runBlocking {
            classifier.add(testString)
            assertTrue(classifier.contains(testString))
        }
    }

    @Test
    fun delete() {
        runBlocking {
            classifier.add(testString).await()
            classifier.delete(testString).await()
            assertFalse(classifier.contains(testString))
        }
    }

    @Test
    fun temporaryAndPermanent() {
        runBlocking {
            classifier.add("apple").await()
            classifier.add("sauce").await()
            classifier.add("pancakes").await()
            classifier.markPermanent("apple")
            classifier.markPermanent("sauce")
            classifier.markTemporary("sauce")
            val temporary = classifier.getAllTemporary().await()
            val permanent = classifier.getAllPermanent().await()
            assertTrue(permanent.size == 1)
            assertTrue(permanent.contains(
                    BasicArticleContext(1L, "apple", "", false)))
            assertTrue(temporary.size == 2)
            assertTrue(temporary.contains(BasicArticleContext.fromString("sauce")))
            assertTrue(temporary.contains(BasicArticleContext.fromString("pancakes")))
        }
    }
}