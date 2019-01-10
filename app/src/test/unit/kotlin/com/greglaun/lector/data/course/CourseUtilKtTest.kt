package com.greglaun.lector.data.course

import org.junit.Assert.assertTrue
import org.junit.Test

class CourseUtilKtTest {

    @Test
    fun extractCourseNames() {
        val json = "[{\"name\":\"Ice Cream\",\"articles\":\"https://en.wikipedia.org/wiki/Ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Cold-stimulus_headache\\r\\nhttps://en.wikipedia.org/wiki/Ice_cream_social\\r\\nhttps://en.wikipedia.org/wiki/Soft_serve\\r\\nhttps://en.wikipedia.org/wiki/Strawberry_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Chocolate_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Neapolitan_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Vanilla_ice_cream\"},{\"name\":\"Furry Friends\",\"articles\":\"https://en.wikipedia.org/wiki/Dog\\r\\nhttps://en.wikipedia.org/wiki/Cat\\r\\nhttps://en.wikipedia.org/wiki/Domestic_rabbit\\r\\nhttps://en.wikipedia.org/wiki/Gerbil\\r\\nhttps://en.wikipedia.org/wiki/Hedgehog\"}]"
        val result = extractCourseMetadata(json)
        assertTrue(result.contains("Ice Cream"))
        assertTrue(result.contains("Furry Friends"))
    }

    @Test
    fun toCourseDetailsMap() {
        val json = "[{\"name\":\"Ice Cream\",\"articles\":\"https://en.wikipedia.org/wiki/Ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Cold-stimulus_headache\\r\\nhttps://en.wikipedia.org/wiki/Ice_cream_social\\r\\nhttps://en.wikipedia.org/wiki/Soft_serve\\r\\nhttps://en.wikipedia.org/wiki/Strawberry_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Chocolate_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Neapolitan_ice_cream\\r\\nhttps://en.wikipedia.org/wiki/Vanilla_ice_cream\"},{\"name\":\"Furry Friends\",\"articles\":\"https://en.wikipedia.org/wiki/Dog\\r\\nhttps://en.wikipedia.org/wiki/Cat\\r\\nhttps://en.wikipedia.org/wiki/Domestic_rabbit\\r\\nhttps://en.wikipedia.org/wiki/Gerbil\\r\\nhttps://en.wikipedia.org/wiki/Hedgehog\"}]"
        val detailsMap = toCourseDetailsMap(extractCourseMap(json))
        assertTrue(detailsMap.keys.size > 0)
        assertTrue(detailsMap.containsKey("Ice Cream"))
        assertTrue(detailsMap.containsKey("Furry Friends"))
        assertTrue(detailsMap.get("Ice Cream")!!.
                articleNames.contains("https://en.wikipedia.org/wiki/Cold-stimulus_headache"))
    }
}