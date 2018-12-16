package com.greglaun.lector.data.course

data class ConcreteCourseContext(override val id: Long?, override val courseName: String,
                                 override val position: Int) : CourseContext
