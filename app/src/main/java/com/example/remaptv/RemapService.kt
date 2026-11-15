package com.example.remaptv

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent

class RemapService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null
    private var pendingKey = -1
    private var longFired = false

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val code = event.keyCode
        val short = Prefs.getShort(this, code) ?: return false // sin mapeo: pasa tal cual
        val long = Prefs.getLong(this, code)

        // Solo pulsación corta: se ejecuta al pulsar, sin retraso.
        if (long == null) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) Actions.run(this, short)
            return true
        }

        // Corta + larga: la corta se decide al soltar; la larga al superar el tiempo de pulsación larga.
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (event.repeatCount == 0) {
                pending?.let { handler.removeCallbacks(it) }
                longFired = false
                pendingKey = code
                pending = Runnable { longFired = true; Actions.run(this, long) }
                handler.postDelayed(pending!!, ViewConfiguration.getLongPressTimeout().toLong())
            }
        } else if (event.action == KeyEvent.ACTION_UP) {
            pending?.let { handler.removeCallbacks(it) }
            if (!longFired && pendingKey == code) Actions.run(this, short)
            pendingKey = -1
        }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
