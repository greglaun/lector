package com.greglaun.lector.android.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.greglaun.lector.data.cache.ArticleContext

@Entity
data class RoomArticleContext(@PrimaryKey(autoGenerate = true) override var id: Long?,
                              override var contextString: String,
                              override var position: String = "",  // position uses md5 hash
                              override var temporary: Boolean = true): ArticleContext