package com.greglaun.lector.data.course

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.greglaun.lector.data.cache.urlToContext

private val mapper: ObjectMapper = ObjectMapper()

fun extractCourseMap(jsonString: String): List<Map<String, Any>> {
    return mapper.readValue(jsonString,
            object : TypeReference<ArrayList<Map<String, Any>>>() {})
}

fun extractCourseMetadata(jsonString: String): List<CourseMetadata> {
    val courseMap = extractCourseMap(jsonString)
    val metaData = mutableListOf<CourseMetadata>()
    courseMap.forEach{
        if (it.containsKey("name")) {
            metaData.add(CourseMetadata(name = it["name"] as String))
        }
    }
    return metaData
}

fun toCourseDetailsMap(jsonMap: List<Map<String, Any>>): Map<String, ThinCourseDetails> {
    val detailsMap = mutableMapOf<String, ThinCourseDetails>()
    jsonMap.forEach {
        detailsMap[it["name"] as String] = ThinCourseDetails(it["name"] as String,
                splitArticles(it["articles"] as String))
    }
    return detailsMap
}

private fun splitArticles(s: String): List<String> {
    return s.split("\r\n").map{ urlToContext(it)}
}
