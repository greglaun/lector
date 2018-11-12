package com.greglaun.lector.android.room

import com.greglaun.lector.data.whitelist.Whitelist
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

class RoomWhitelist(val db: ArticleCacheDatabase): Whitelist<String> {
    override fun contains(element: String): Deferred<Boolean> {
        return GlobalScope.async{
            db.articleContextDao().get(element) != null
        }
    }

    override fun add(element: String): Deferred<Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(element: String): Deferred<Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}