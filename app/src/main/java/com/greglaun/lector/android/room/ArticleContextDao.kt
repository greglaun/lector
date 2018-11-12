package com.greglaun.lector.android.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface ArticleContextDao {

    @Query("SELECT * from articlecontext")
    fun getAll(): List<ArticleContext>

    @Query("SELECT * FROM articlecontext WHERE articleContext = :articleContext")
    fun get(articleContext: String): ArticleContext

    @Query("DELETE from ArticleContext WHERE articleContext = :articleContext" )
    fun delete(articleContext: String)

    @Insert(onConflict = REPLACE)
    fun insert(articleContext: ArticleContext)

    @Query("DELETE from ArticleContext")
    fun deleteAll()
}