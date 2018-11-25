package com.greglaun.lector.android.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

@Dao
interface ArticleContextDao {

    @Query("SELECT * from articlecontext")
    fun getAll(): List<ArticleContext>

    @Query("SELECT * FROM articlecontext WHERE contextString = :contextString")
    fun get(contextString: String): ArticleContext

    @Query("DELETE from ArticleContext WHERE contextString = :contextString")
    fun delete(contextString: String)

    @Query("UPDATE ArticleContext SET `temporary` = 0 WHERE contextString = :contextString")
    fun markPermanent(contextString: String)

    @Query("UPDATE ArticleContext SET `temporary` = 1 WHERE contextString = :contextString")
    fun markTemporary(contextString: String)

    @Query("SELECT contextString from ArticleContext WHERE `temporary` = 1")
    fun getAllTemporary(): List<String>

    @Insert(onConflict = REPLACE)
    fun insert(articleContext: ArticleContext)

    @Update
    fun updateArticleContext(articleContext: ArticleContext)

    @Query("DELETE from ArticleContext")
    fun deleteAll()

    @Query("DELETE from articlecontext WHERE `temporary` = 1")
    fun deleteAllTemporary()

    @Query("SELECT `temporary` from articlecontext WHERE contextString = :contextString")
    fun isTemporary(contextString: String): Boolean
}