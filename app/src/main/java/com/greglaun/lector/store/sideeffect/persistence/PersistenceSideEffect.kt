package com.greglaun.lector.store.sideeffect.persistence

import com.greglaun.lector.store.Action
import com.greglaun.lector.store.SideEffect
import com.greglaun.lector.store.Store

class PersistenceSideEffect(val store: Store)
    : SideEffect {


    override suspend fun handle(action: Action) {
//        when (action) {
//            is CreationAction -> CreationHandler.handle(action) { store.dispatch(it) }
//            is UpdateAction -> UpdateHandler.handle(action) { store.dispatch(it) }
//            is ReadAction -> ReadHandler.handle(action) { store.dispatch(it) }
//            is DeleteAction -> DeleteHandler.handle(action) { store.dispatch(it) }
//        }
    }
}