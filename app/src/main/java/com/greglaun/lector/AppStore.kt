package com.greglaun.lector

import com.greglaun.lector.data.PersistenceSideEffect
import com.greglaun.lector.store.Store


object AppStore : Store() {
    init {
        this.sideEffects.add(PersistenceSideEffect(store = this))
    }
}