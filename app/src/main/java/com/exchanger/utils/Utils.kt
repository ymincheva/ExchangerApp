package com.exchanger.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.exchanger.R

class Utils(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    fun putPreferences( key: String, value: Int) {
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getPreferences( key: String): Int {
        return prefs.getInt(key,0)
    }
}