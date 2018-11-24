package com.greglaun.lector.android.room

import android.arch.persistence.room.*

@Entity(foreignKeys = arrayOf(ForeignKey(
        entity = ArticleContext::class,
        parentColumns = arrayOf("articleContext"),
        childColumns = arrayOf("articleContext"))),
        indices = arrayOf(Index(value = "articleContext")))


data class CachedResponse(@PrimaryKey(autoGenerate = true) var id: Long?,
                          @ColumnInfo(name = "url_hash") var urlHash: String, // Using md5
                          @ColumnInfo(name = "serialized_response") var response : String,
                          var articleContext: String)