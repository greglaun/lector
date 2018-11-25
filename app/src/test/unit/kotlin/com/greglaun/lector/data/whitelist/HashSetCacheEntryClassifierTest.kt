package com.greglaun.lector.data.whitelist

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HashSetCacheEntryClassifierTest {

    val whiteList = HashSetCacheEntryClassifier<String>()
    val testString = "Potato"

    @Test
    fun notContains() {
        runBlocking {
            assertFalse(whiteList.contains(testString).await())
        }
    }

    @Test
    fun contains() {
        runBlocking {
            whiteList.add(testString)
            assertTrue(whiteList.contains(testString).await())
        }
    }

    @Test
    fun delete() {
        runBlocking {
            whiteList.add(testString).await()
            whiteList.delete(testString).await()
            assertFalse(whiteList.contains(testString).await())
        }
    }

    @Test
    operator fun iterator() {
        runBlocking {
            whiteList.add("apple").await()
            whiteList.add("sauce").await()
            whiteList.add("pancakes").await()
            val it = whiteList.iterator()
            while (it.hasNext()) {
                assertTrue(it.next() in listOf("apple", "sauce", "pancakes"))
            }
        }
    }
}