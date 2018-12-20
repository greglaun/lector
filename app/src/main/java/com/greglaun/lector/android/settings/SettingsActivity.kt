package com.greglaun.lector.android.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import com.greglaun.lector.R


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, SettingsActivity.SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }
    }
}

