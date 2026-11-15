package com.example.remaptv

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView

class MainActivity : Activity() {

    private var learning = false
    private lateinit var status: TextView
    private lateinit var list: ListView
    private var keys: List<Int> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = 48
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        status = TextView(this).apply { textSize = 20f }
        val add = Button(this).apply {
            text = "Añadir mapeo (luego pulsa un botón del mando)"
            setOnClickListener {
                learning = true
                status.text = "Pulsa ahora el botón del mando que quieres reasignar…"
            }
        }
        val access = Button(this).apply {
            text = "Activar servicio de accesibilidad"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        list = ListView(this)
        list.setOnItemClickListener { _, _, pos, _ ->
            val code = keys[pos]
            AlertDialog.Builder(this)
                .setTitle("Eliminar mapeo de ${KeyEvent.keyCodeToString(code)}?")
                .setPositiveButton("Eliminar") { _, _ -> Prefs.remove(this, code); refresh() }
                .setNegativeButton("Cancelar", null).show()
        }
        root.addView(status); root.addView(add); root.addView(access); root.addView(list)
        setContentView(root)
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        val all = Prefs.all(this)
        keys = all.keys.toList()
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            all.map { (k, v) -> "${KeyEvent.keyCodeToString(k)}  →  corta: ${Actions.label(v.first)}" +
                (v.second?.let { l -> "  |  larga: ${Actions.label(l)}" } ?: "") })
        status.text = "Mapeos activos: ${all.size}. Selecciona uno para borrarlo."
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (learning) {
            learning = false
            chooseAction(keyCode)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun chooseAction(keyCode: Int) {
        val name = KeyEvent.keyCodeToString(keyCode)
        pickAction("$name · PULSACIÓN CORTA hará…", false) { short ->
            pickAction("$name · PULSACIÓN LARGA hará…", true) { long ->
                Prefs.set(this, keyCode, short!!, long)
                refresh()
            }
        }
    }

    /** Muestra la lista de acciones. Si allowNone, el primer elemento devuelve null (sin acción). */
    private fun pickAction(title: String, allowNone: Boolean, onPick: (String?) -> Unit) {
        val ids = (if (allowNone) listOf("") else emptyList()) + Actions.simple.keys + "APP"
        val names = (if (allowNone) listOf("Ninguna (solo pulsación corta)") else emptyList()) +
            Actions.simple.values + "Abrir una app…"
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(names.toTypedArray()) { _, i ->
                when (ids[i]) {
                    "" -> onPick(null)
                    "APP" -> chooseApp { pkg -> onPick("APP:$pkg") }
                    else -> onPick(ids[i])
                }
            }
            .setOnCancelListener { refresh() }
            .show()
    }

    private fun chooseApp(onPick: (String) -> Unit) {
        val pm = packageManager
        val apps: List<ResolveInfo> = (
            pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0) +
            pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        ).distinctBy { it.activityInfo.packageName }
         .sortedBy { it.loadLabel(pm).toString().lowercase() }
        AlertDialog.Builder(this)
            .setTitle("Elige la app")
            .setItems(apps.map { it.loadLabel(pm).toString() }.toTypedArray()) { _, i ->
                onPick(apps[i].activityInfo.packageName)
            }
            .setOnCancelListener { refresh() }
            .show()
    }
}
