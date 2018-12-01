package com.greglaun.lector.android.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

@Dao
interface ArticleContextDao {

    @Query("SELECT * from roomarticlecontext")
    fun getAll(): List<RoomArticleContext>

    @Query("SELECT * FROM roomarticlecontext WHERE contextString = :contextString")
    fun get(contextString: String): RoomArticleContext

    @Query("DELETE from RoomArticleContext WHERE contextString = :contextString")
    fun delete(contextString: String)

    @Query("UPDATE RoomArticleContext SET `temporary` = 0 WHERE contextString = :contextString")
    fun markPermanent(contextString: String)

    @Query("UPDATE RoomArticleContext SET `temporary` = 1 WHERE contextString = :contextString")
    fun markTemporary(contextString: String)

    @Query("SELECT * from RoomArticleContext WHERE `temporary` = 1")
    fun getAllTemporary(): List<RoomArticleContext>

    @Insert(onConflict = REPLACE)
    fun insert(articleContext: RoomArticleContext): Long

    @Update
    fun updateArticleContext(articleContext: RoomArticleContext)

    @Query("DELETE from RoomArticleContext")
    fun deleteAll()

    @Query("DELETE from roomarticlecontext WHERE `temporary` = 1")
    fun deleteAllTemporary()

    @Query("SELECT `temporary` from roomarticlecontext WHERE contextString = :contextString")
    fun isTemporary(contextString: String): Boolean

    @Query("SELECT * from RoomArticleContext WHERE `temporary` = 0")
    fun getAllPermanent(): List<RoomArticleContext>

    @Query("UPDATE RoomArticleContext SET position = :position WHERE contextString = :contextString")
    fun updatePosition(contextString: String, position: String)

}