package com.greglaun.lector.android.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface ArticleContextDao {

    @Query("SELECT * from articlecontext")
    fun getAll(): List<ArticleContext>

    @Query("SELECT * FROM articlecontext WHERE context = :context")
    fun get(context: String): ArticleContext

    @Query("DELETE from ArticleContext WHERE context = :context" )
    fun delete(context: String)

    @Insert(onConflict = REPLACE)
    fun insert(articleContext: ArticleContext)

    @Query("DELETE from ArticleContext")
    fun deleteAll()
}