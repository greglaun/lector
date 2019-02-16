package com.greglaun.lector.android.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.POSITION_BEGINNING

@Entity(indices = arrayOf(Index(value = "contextString", unique = true)))
data class RoomArticleContext(@PrimaryKey(autoGenerate = true) override var id: Long?,
                              override var contextString: String,
                              override var position: String = POSITION_BEGINNING,  // position uses md5 hash
                              override var temporary: Boolean = true,
                              override var downloadComplete: Boolean = false): ArticleContext

