package com.pps.parapente.sync

import android.content.Context
import android.content.SharedPreferences

/**
 * Paramètres de connexion à Supabase, enregistrés dans les préférences de l'application
 * (stockage privé à l'application, propre à cet appareil).
 */
class SyncConfig(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("pps_sync", Context.MODE_PRIVATE)

    var url: String
        get() = prefs.getString("url", "") ?: ""
        set(value) { prefs.edit().putString("url", value.trim()).apply() }

    var cleAnon: String
        get() = prefs.getString("cleAnon", "") ?: ""
        set(value) { prefs.edit().putString("cleAnon", value.trim()).apply() }

    var syncAuto: Boolean
        get() = prefs.getBoolean("syncAuto", false)
        set(value) { prefs.edit().putBoolean("syncAuto", value).apply() }

    var derniereSync: String
        get() = prefs.getString("derniereSync", "") ?: ""
        set(value) { prefs.edit().putString("derniereSync", value).apply() }

    fun estConfigure(): Boolean = url.isNotBlank() && cleAnon.isNotBlank()
}
