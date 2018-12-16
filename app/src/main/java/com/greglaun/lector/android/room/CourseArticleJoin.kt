package com.greglaun.lector.android.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index

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
        @ColumnInfo(name = "course_id") val placeId: Long,
        @ColumnInfo(name = "article_id") val categoryId: Long
)