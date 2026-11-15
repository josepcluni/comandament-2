package com.example.remaptv

import android.content.Context

/** Guarda por botón una acción corta ("s<code>") y una larga opcional ("l<code>"). */
object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("mappings", Context.MODE_PRIVATE)

    fun getShort(c: Context, code: Int): String? = sp(c).getString("s$code", null)
    fun getLong(c: Context, code: Int): String? = sp(c).getString("l$code", null)

    /** keyCode -> (corta, larga?) */
    fun all(c: Context): Map<Int, Pair<String, String?>> =
        sp(c).all.keys.filter { it.startsWith("s") }.mapNotNull { it.drop(1).toIntOrNull() }
            .associateWith { code -> Pair(getShort(c, code)!!, getLong(c, code)) }

    fun set(c: Context, code: Int, short: String, long: String?) {
        val e = sp(c).edit().putString("s$code", short)
        if (long == null) e.remove("l$code") else e.putString("l$code", long)
        e.apply()
    }

    fun remove(c: Context, code: Int) = sp(c).edit().remove("s$code").remove("l$code").apply()
}
