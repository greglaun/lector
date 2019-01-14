package com.greglaun.lector.android.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomWarnings

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
    fun insert(join: CourseArticleJoin): Long

    @Query("""
           SELECT * from coursearticlejoin WHERE course_id = :courseId
           AND article_id = :articleId""")
    fun get(courseId: Long, articleId: Long): CourseArticleJoin

}