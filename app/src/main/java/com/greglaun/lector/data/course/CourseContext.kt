package com.greglaun.lector.data.course

interface CourseContext {
    val id: Long?
    val courseName: String
    val position: Int
}

data class EmptyCourseContext(override val id: Long? = null,
                              override  val courseName: String = "All Articles",
                              override val position: Int = 0): CourseContext