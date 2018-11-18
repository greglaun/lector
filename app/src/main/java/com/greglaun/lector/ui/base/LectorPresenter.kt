package com.greglaun.lector.ui.base

interface LectorPresenter<V : LectorView> {
    fun onAttach()
    fun onDetach()
    fun getLectorView(): V?
}