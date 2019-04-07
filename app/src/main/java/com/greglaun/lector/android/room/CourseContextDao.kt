package com.greglaun.lector.android.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface CourseContextDao {

    @Query("SELECT * from roomcoursecontext")
    fun getAll(): List<RoomCourseContext>?

    @Query("SELECT * FROM roomcoursecontext WHERE courseName = :courseName")
    fun get(courseName: String): RoomCourseContext?

    @Query("DELETE from roomcoursecontext WHERE courseName = :courseName")
    fun delete(courseName: String)

    @Insert(onConflict = REPLACE)
    fun insert(courseContext: RoomCourseContext): Long

    @Update
    fun updatecourseContext(courseContext: RoomCourseContext)

    @Query("DELETE from roomcoursecontext")
    fun deleteAll()

    @Query("UPDATE roomcoursecontext SET position = :position WHERE courseName = :courseName")
    fun updatePosition(courseName: String, position: Int)
}