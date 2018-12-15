package com.greglaun.lector.ui.base

interface LectorView {
    fun onError(resourceId: Int)
    fun onError(message: String)
    fun showMessage(message: String)
    fun showMessage(resourceId: Int)
    fun confirmMessage(message: String, yesButton: String = "Yes", noButton: String = "No",
                       onConfirmed: (Boolean) -> Unit)
    fun confirmMessage(resourceId: Int, yesButton: Int, noButton: String,
                       onConfirmed: (Boolean) -> Unit)
}