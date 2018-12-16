package com.greglaun.lector.android.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(RoomArticleContext::class, CachedResponse::class,
        RoomCourseContext::class, CourseArticleJoin::class), version = 1)
abstract class LectorDatabase : RoomDatabase() {

    abstract fun articleContextDao(): ArticleContextDao
    abstract fun cachedResponseDao(): CachedResponseDao
    abstract fun courseContextDao():  CourseContextDao
    abstract fun courseArticleJoinDao(): CourseArticleJoinDao

    companion object {
        private var INSTANCE: LectorDatabase? = null

        fun getInstance(context: Context): LectorDatabase? {
            if (INSTANCE == null) {
                synchronized(LectorDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            LectorDatabase::class.java, "saved_articles.db")
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