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
        coursearticlejoin.course_id = :courseId AND `temporary` = 0
        ORDER BY position
        """)
    fun getArticlesWithCourseId(courseId: Long): List<RoomArticleContext>

    @Query("""
           SELECT course_position from coursearticlejoin WHERE course_id = :courseId ORDER BY
           course_position DESC LIMIT 1""")
    fun getMaxOccupiedPosition(courseId: Long): Long?

    @Query("""
           SELECT course_position from coursearticlejoin WHERE course_id = :courseId ORDER BY
           course_position LIMIT 1""")
    fun getLeastOccupiedPosition(courseId: Long): Long?

    @Insert
    fun insert(join: CourseArticleJoin): Long

    @Query("""
           SELECT * from coursearticlejoin WHERE course_id = :courseId
           AND article_id = :articleId""")
    fun get(courseId: Long, articleId: Long): CourseArticleJoin?

    @Query("SELECT * from coursearticlejoin")
    fun getAll(): List<CourseArticleJoin>?

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""
        SELECT * FROM roomarticlecontext INNER JOIN coursearticlejoin ON
        roomarticlecontext.id = coursearticlejoin.article_id WHERE
        coursearticlejoin.course_id = :courseId AND roomarticlecontext.id > :previousArticleId
        """)
    fun getNextInCourse(courseId: Long, previousArticleId: Long): RoomArticleContext

}