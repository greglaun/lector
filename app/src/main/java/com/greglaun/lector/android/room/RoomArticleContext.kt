package com.greglaun.lector.android.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.POSITION_BEGINNING

@Entity(indices = arrayOf(Index(value = "contextString", unique = true)))
data class RoomArticleContext(@PrimaryKey(autoGenerate = true) override var id: Long?,
                              override var contextString: String,
                              override var position: String = POSITION_BEGINNING,  // position uses md5 hash
                              override var temporary: Boolean = true): ArticleContext

