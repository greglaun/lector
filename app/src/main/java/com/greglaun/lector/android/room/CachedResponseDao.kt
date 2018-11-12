package com.greglaun.lector.android.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface CachedResponseDao {

    @Query("SELECT * from cachedresponse")
    fun getAll(): List<CachedResponse>

    @Query("SELECT * from cachedresponse WHERE articleContext = :articleContext")
    fun getWithContext(articleContext: String): List<CachedResponse>

    @Query("SELECT * from cachedresponse WHERE url_hash = :urlHash LIMIT 1")
    fun get(urlHash : String): CachedResponse

    @Insert(onConflict = REPLACE)
    fun insert(cachedResponse: CachedResponse)

    @Query("DELETE from cachedresponse")
    fun deleteAll()

    @Query("DELETE from cachedresponse WHERE articleContext=:articleContext")
    fun deleteWithContext(articleContext: String)

}