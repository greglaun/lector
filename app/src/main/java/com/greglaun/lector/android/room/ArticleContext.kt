package com.greglaun.lector.android.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class ArticleContext(@PrimaryKey() var articleContext: String,
                          var position: String = "") // position uses md5 hash