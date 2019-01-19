package com.greglaun.lector.android.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface ArticleContextDao {

    @Query("SELECT * from roomarticlecontext")
    fun getAll(): List<RoomArticleContext>

    @Query("SELECT * FROM roomarticlecontext WHERE contextString = :contextString")
    fun get(contextString: String): RoomArticleContext?

    @Query("SELECT * FROM roomarticlecontext WHERE id > :oldId ORDER BY id LIMIT 1")
    fun getNextLargest(oldId: Long): RoomArticleContext?

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

    @Query("SELECT * from RoomArticleContext WHERE `download_complete` = 0 AND `temporary` = 0")
    fun getAllUnfinished(): List<RoomArticleContext>

    @Query("UPDATE RoomArticleContext SET `download_complete` = 1 WHERE contextString = :contextString")
    fun markFinished(contextString: String)
}