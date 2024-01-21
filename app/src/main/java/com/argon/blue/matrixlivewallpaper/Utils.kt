package com.argon.blue.matrixlivewallpaper

import android.content.Context

class Utils(private val context: Context) {
    fun getCharsetFromName(name:String):String {
        if (name.contains("Japanese") ) {
            return context.resources.getString(R.string.jp_charset)
        } else if(name.contains("Korean") ) {
            return context.resources.getString(R.string.kr_charset)
        } else if(name.contains("Thai") ) {
            return context.resources.getString(R.string.th_charset)
        } else if(name.contains("Russian") ) {
            return context.resources.getString(R.string.ru_charset)
        } else {
            return context.resources.getString(R.string.jp_charset)
        }
    }
}