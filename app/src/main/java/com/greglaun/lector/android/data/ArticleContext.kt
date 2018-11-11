package com.greglaun.lector.android.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class ArticleContext(@PrimaryKey() var context: String)