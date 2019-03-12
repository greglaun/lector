package com.greglaun.lector

import android.app.Application
import android.content.Context
import com.greglaun.lector.android.AndroidDownloadCompleter
import com.greglaun.lector.android.AndroidInternetChecker
import com.greglaun.lector.android.WebviewDownloadTool
import com.greglaun.lector.android.room.LectorDatabase
import com.greglaun.lector.android.room.RoomCacheEntryClassifier
import com.greglaun.lector.android.room.RoomCourseSource
import com.greglaun.lector.android.room.RoomSavedArticleCache
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseDownloaderImpl
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.net.DownloadCompletionScheduler
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import com.greglaun.lector.store.Store
import com.greglaun.lector.store.sideeffect.fetch.FetchSideEffect
import com.greglaun.lector.store.sideeffect.finisher.DownloadFinisher
import com.greglaun.lector.store.sideeffect.persistence.PersistenceSideEffect

class LectorApplication: Application() {
    private var RESPONSE_SOURCE_INSTANCE: ResponseSource? = null
    private var COURSE_SOURCE_INSTANCE: CourseSource? = null
    private var COURSE_DOWNLOADER_INSTANCE: CourseDownloader? = null
    private var DOWNLOAD_COMPLETION_SCHEDULER: DownloadCompletionScheduler? = null
    object AppStore: Store()

    var context: Context? = null

    override fun onCreate() {
        super.onCreate()
        context = applicationContext!!
        responseSource()
        courseSource()
        courseDownloader()
        prepareStore()
    }

    fun getAppContext(): Context {
        return context!!
    }

    fun prepareStore() {
        AppStore.sideEffects.add(PersistenceSideEffect(AppStore))
        AppStore.sideEffects.add(FetchSideEffect(AppStore,
                responseSource(),
                courseSource(),
                courseDownloader()))
    }

    fun addDownloadCompletionSideEffect(webviewDownloadTool: WebviewDownloadTool) {
        AppStore.sideEffects.add(DownloadFinisher(AppStore,
                downloadCompletionScheduler(webviewDownloadTool)))
    }

    fun responseSource(): ResponseSource {
        if (RESPONSE_SOURCE_INSTANCE == null) {
            val db = LectorDatabase.getInstance(context!!)
            val cacheEntryClassifier: CacheEntryClassifier<String> = RoomCacheEntryClassifier(db!!)
            RESPONSE_SOURCE_INSTANCE = ResponseSourceImpl.createResponseSource(
                    RoomSavedArticleCache(db),
                    cacheEntryClassifier,
                    getCacheDir())
        }
        return RESPONSE_SOURCE_INSTANCE!!
    }

    fun courseSource(): CourseSource {
        if (COURSE_SOURCE_INSTANCE == null) {
            COURSE_SOURCE_INSTANCE = RoomCourseSource(LectorDatabase.getInstance(this)!!)
        }
        return COURSE_SOURCE_INSTANCE!!
    }

    fun courseDownloader(): CourseDownloader {
        if (COURSE_DOWNLOADER_INSTANCE == null) {
            COURSE_DOWNLOADER_INSTANCE = CourseDownloaderImpl(BuildConfig.BASE_URL, cacheDir)
        }
        return COURSE_DOWNLOADER_INSTANCE!!
    }


    fun downloadCompletionScheduler(webviewDownloadTool: WebviewDownloadTool): DownloadCompletionScheduler {
        if (DOWNLOAD_COMPLETION_SCHEDULER == null) {
            DOWNLOAD_COMPLETION_SCHEDULER = DownloadCompletionScheduler(
                    AndroidDownloadCompleter(AndroidInternetChecker(this),
                            webviewDownloadTool),
                    responseSource())
        }
        return DOWNLOAD_COMPLETION_SCHEDULER!!
    }
}