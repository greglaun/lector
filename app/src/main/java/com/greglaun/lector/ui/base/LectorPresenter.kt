package com.greglaun.lector.ui.base

interface LectorPresenter<V : LectorView> {
    fun onAttach(lectorView: V)
    fun onDetach()
    fun getLectorView(): V?
}