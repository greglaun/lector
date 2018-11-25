package com.greglaun.lector.android.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class ArticleContext(@PrimaryKey(autoGenerate = true) var id: Long?,
                          var contextString: String,
                          var position: String = "",  // position uses md5 hash
                          var temporary: Boolean = true)