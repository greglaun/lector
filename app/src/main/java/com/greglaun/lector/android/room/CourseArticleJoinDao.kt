package com.greglaun.lector.android.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.RoomWarnings

@Dao
interface CourseArticleJoinDao {

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""
        SELECT * FROM roomarticlecontext INNER JOIN coursearticlejoin ON
        roomarticlecontext.id = coursearticlejoin.article_id WHERE
        coursearticlejoin.course_id = :courseId
        """)
    fun getArticlesWithCourseId(courseId: Long): List<RoomArticleContext>

    @Insert
    fun insert(join: CourseArticleJoin)
}