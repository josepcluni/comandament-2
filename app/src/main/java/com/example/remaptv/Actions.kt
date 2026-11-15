package com.example.remaptv

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.media.AudioManager

/** Acciones disponibles. Formato guardado: "BACK", "HOME", ..., "APP:com.paquete", "NONE". */
object Actions {
    val simple = linkedMapOf(
        "BACK" to "Atrás",
        "HOME" to "Inicio",
        "RECENTS" to "Apps recientes",
        "NOTIFICATIONS" to "Notificaciones",
        "QUICK_SETTINGS" to "Ajustes rápidos",
        "VOL_UP" to "Subir volumen",
        "VOL_DOWN" to "Bajar volumen",
        "MUTE" to "Silenciar / activar sonido",
        "NONE" to "No hacer nada (bloquear botón)"
    )

    fun label(action: String): String =
        if (action.startsWith("APP:")) "Abrir app ${action.removePrefix("APP:")}"
        else simple[action] ?: action

    fun run(s: AccessibilityService, action: String) {
        val audio = s.getSystemService(AudioManager::class.java)
        when {
            action == "BACK" -> s.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            action == "HOME" -> s.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            action == "RECENTS" -> s.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            action == "NOTIFICATIONS" -> s.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            action == "QUICK_SETTINGS" -> s.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
            action == "VOL_UP" -> audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            action == "VOL_DOWN" -> audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            action == "MUTE" -> audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI)
            action.startsWith("APP:") -> {
                val pkg = action.removePrefix("APP:")
                val i: Intent? = s.packageManager.getLeanbackLaunchIntentForPackage(pkg)
                    ?: s.packageManager.getLaunchIntentForPackage(pkg)
                i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let { s.startActivity(it) }
            }
        }
    }
}
