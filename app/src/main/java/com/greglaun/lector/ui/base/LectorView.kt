package com.greglaun.lector.ui.base

interface LectorView {
    fun onError(resId: Int)
    fun onError(message: String)
    fun showMessage(message: String)
    fun showMessage(resId: Int)
}