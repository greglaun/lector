package com.greglaun.lector.android.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface CachedResponseDao {

    @Query("SELECT * from cachedresponse")
    fun getAll(): List<CachedResponse>

    @Query("""
        SELECT * from cachedresponse WHERE articleContextId = (SELECT id FROM
        roomarticlecontext where contextString = :contextString)
        """)
    fun getAllWithContext(contextString: String): List<CachedResponse>

    @Query("SELECT * from cachedresponse WHERE url_hash = :urlHash LIMIT 1")
    fun get(urlHash : String): CachedResponse?

    @Insert(onConflict = REPLACE)
    fun insert(cachedResponse: CachedResponse)

    @Insert(onConflict = REPLACE)
    fun insert(responseList: List<CachedResponse>)

    @Query("DELETE from cachedresponse")
    fun deleteAll()

    @Query("""
        DELETE from cachedresponse WHERE articleContextId = (SELECT id FROM
        roomarticlecontext WHERE contextString= :contextString)
        """)
    fun deleteWithContext(contextString: String)

    @Query("""
            DELETE from cachedresponse WHERE articleContextId = (SELECT id FROM
            roomarticlecontext WHERE `temporary` = 1)
            """)
    fun deleteAllTemporary()
}