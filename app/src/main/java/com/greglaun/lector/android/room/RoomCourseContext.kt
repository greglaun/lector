package com.greglaun.lector.android.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.greglaun.lector.data.course.CourseContext

@Entity(indices = arrayOf(Index(value = "courseName", unique = true)))
data class RoomCourseContext(@PrimaryKey(autoGenerate = true) override var id: Long?,
                             override var courseName: String,
                             override var position: Int = 0): CourseContext

