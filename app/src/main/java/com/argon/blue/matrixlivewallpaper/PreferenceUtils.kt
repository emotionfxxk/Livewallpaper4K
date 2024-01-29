package com.argon.blue.matrixlivewallpaper

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color

class PreferenceUtils(private val context: Context) {
    private val sp:SharedPreferences = context.getSharedPreferences("sp_matrix_settings",Context.MODE_PRIVATE)
    companion object {
        private const val KEY_TEXT_COLOR = "MATRIX_TEXT_COLOR"
        private const val KEY_CHARSET = "MATRIX_TEXT_CHARSET"
    }
    fun putMatrixTextColor(color:Int) {
        sp.edit().putInt(KEY_TEXT_COLOR, color).apply()
    }
    fun getMatrixTextColor() :Int {
        return sp.getInt(KEY_TEXT_COLOR, Color.GREEN)
    }
    fun putMatrixCharset(charset:String) {
        sp.edit().putString(KEY_CHARSET, charset).apply()
    }
    fun getMatrixCharset() : String? {
        return sp.getString(KEY_CHARSET, "Japanese(にほんご)")
    }
    fun registerListener(listener: OnSharedPreferenceChangeListener) {
        sp.registerOnSharedPreferenceChangeListener(listener)
    }
}