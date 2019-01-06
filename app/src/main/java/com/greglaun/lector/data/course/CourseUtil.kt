package com.greglaun.lector.data.course

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

private val mapper: ObjectMapper = ObjectMapper()

fun extractCourseMap(jsonString: String): List<Map<String, Object>> {
    val courseMap: List<Map<String, Object>> = mapper.readValue(jsonString,
            object : TypeReference<ArrayList<Map<String, Any>>>() {})
    return courseMap
}

fun extractCourseNames(jsonString: String): List<String> {
    val courseMap = extractCourseMap(jsonString)
    val courseNames = mutableListOf<String>()
    courseMap.forEach{
        if (it.containsKey("name")) {
            courseNames.add(it.get("name") as String)
        }
    }
    return courseNames
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
