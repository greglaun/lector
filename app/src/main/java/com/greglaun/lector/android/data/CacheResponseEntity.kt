package com.greglaun.lector.android.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(foreignKeys = arrayOf(ForeignKey(
        entity = ArticleContext::class,
        parentColumns = arrayOf("context"),
        childColumns = arrayOf("context"))))

data class WeatherData(@PrimaryKey(autoGenerate = true) var id: Long?,
                       @ColumnInfo(name = "serial_response") var serialResponse : String)