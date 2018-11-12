package com.greglaun.lector.android.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(ArticleContext::class, CachedResponse::class), version = 1)
abstract class ArticleCacheDatabase : RoomDatabase() {

    abstract fun articleContextDao(): ArticleContextDao
    abstract fun cachedResponseDao(): CachedResponseDao

    companion object {
        private var INSTANCE: ArticleCacheDatabase? = null

        fun getInstance(context: Context): ArticleCacheDatabase? {
            if (INSTANCE == null) {
                synchronized(ArticleCacheDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ArticleCacheDatabase::class.java, "saved_articles.db")
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}