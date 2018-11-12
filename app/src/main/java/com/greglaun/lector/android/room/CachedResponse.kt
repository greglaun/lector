package com.greglaun.lector.android.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(foreignKeys = arrayOf(ForeignKey(
        entity = ArticleContext::class,
        parentColumns = arrayOf("articleContext"),
        childColumns = arrayOf("articleContext"))))

data class CachedResponse(@PrimaryKey(autoGenerate = true) var id: Long?,
                          @ColumnInfo(name = "url_hash") var urlHash: String, // Using md5
                          @ColumnInfo(name = "serial_response") var serialResponse : String,
                          var articleContext: String)