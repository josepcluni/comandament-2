# Remap TV
1. Abre la carpeta en Android Studio (generará el Gradle wrapper) y compila el APK.
2. Instálalo en la TV: `adb connect IP_DE_LA_TV` y `adb install app-debug.apk`.
3. Abre la app > "Activar servicio de accesibilidad" > Remap TV > Activar.
   Si el menú de accesibilidad no lo permite, por adb:
   adb shell settings put secure enabled_accessibility_services com.example.remaptv/com.example.remaptv.RemapService
   adb shell settings put secure accessibility_enabled 1
4. "Añadir mapeo", pulsa el botón del mando y elige la acción.
