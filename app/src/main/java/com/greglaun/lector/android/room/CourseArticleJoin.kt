package com.greglaun.lector.android.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(primaryKeys = ["course_id", "article_id"],
        indices = [
            Index(value = ["course_id"]),
            Index(value = ["article_id"])
        ],
        foreignKeys = [
            ForeignKey(entity = RoomCourseContext::class,
                    parentColumns = ["id"],
                    childColumns = ["course_id"]),
            ForeignKey(entity = RoomArticleContext::class,
                    parentColumns = ["id"],
                    childColumns = ["article_id"])
        ])

data class CourseArticleJoin(
        @ColumnInfo(name = "course_id") val courseId: Long,
        @ColumnInfo(name = "article_id") val articleId: Long
)