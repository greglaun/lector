package com.greglaun.lector.android.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(RoomArticleContext::class, CachedResponse::class,
        RoomCourseContext::class, CourseArticleJoin::class), version = 2)
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