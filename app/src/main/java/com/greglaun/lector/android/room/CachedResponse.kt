package com.greglaun.lector.android.room

import android.arch.persistence.room.*

@Entity(foreignKeys = arrayOf(ForeignKey(
        entity = RoomArticleContext::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("articleContextId"))),
        indices = arrayOf(Index(value = "articleContextId")))


data class CachedResponse(@PrimaryKey(autoGenerate = true) var id: Long?,
                          @ColumnInfo(name = "url_hash") var urlHash: String, // Using md5
                          @ColumnInfo(name = "serialized_response") var response : String,
                          var articleContextId: Long)