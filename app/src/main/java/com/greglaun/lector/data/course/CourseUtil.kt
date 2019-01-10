package com.greglaun.lector.data.course

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

private val mapper: ObjectMapper = ObjectMapper()

fun extractCourseMap(jsonString: String): List<Map<String, Object>> {
    val courseMap: List<Map<String, Object>> = mapper.readValue(jsonString,
            object : TypeReference<ArrayList<Map<String, Any>>>() {})
    return courseMap
}

fun extractCourseMetadata(jsonString: String): List<CourseMetadata> {
    val courseMap = extractCourseMap(jsonString)
    val metaData = mutableListOf<CourseMetadata>()
    courseMap.forEach{
        if (it.containsKey("name")) {
            metaData.add(CourseMetadata(name = it.get("name") as String))
        }
    }
    return metaData
}

fun toCourseDetailsMap(jsonMap: List<Map<String, Object>>): Map<String, CourseDetails> {
    val detailsMap = mutableMapOf<String, CourseDetails>()
    jsonMap.forEach {
        detailsMap.put(it.get("name") as String, CourseDetails(it.get("name")  as String,
                splitArticles(it.get("articles") as String)))
    }
    return detailsMap
}

private fun splitArticles(s: String): List<String> {
    return s.split("\r\n")
}