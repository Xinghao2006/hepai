package com.example.hepai.data

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("hepai_device", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) {
            prefs.edit().apply {
                if (value.isNullOrBlank()) remove("token") else putString("token", value)
            }.apply()
        }

    var snapshot: String?
        get() = prefs.getString("snapshot", null)
        set(value) {
            prefs.edit().apply {
                if (value.isNullOrBlank()) remove("snapshot") else putString("snapshot", value)
            }.apply()
        }
}
