package com.greglaun.lector.android.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface CachedResponseDao {

    @Query("SELECT * from cachedresponse")
    fun getAll(): List<CachedResponse>

    @Query("SELECT * from cachedresponse WHERE context = :context")
    fun getWithContext(context: String): List<CachedResponse>

    @Insert(onConflict = REPLACE)
    fun insert(cachedResponse: CachedResponse)

    @Query("DELETE from cachedresponse")
    fun deleteAll()
}