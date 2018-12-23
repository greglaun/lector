package com.greglaun.lector.android

import android.content.Context

interface LectorPreferenceManager {
    fun setFromPreferences(context: Context): Unit
}
