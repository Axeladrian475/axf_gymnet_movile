package com.example.axf_movile.network

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Maneja la sesión del suscriptor usando SharedPreferences.
 * Guarda el JWT y los datos del usuario entre sesiones.
 */
object SessionManager {

    private const val PREF_NAME = "axf_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_ID = "id_suscriptor"
    private const val KEY_NOMBRE = "nombre"
    private const val KEY_CORREO = "correo"
    private const val KEY_SUCURSAL = "id_sucursal_registro"
    private const val KEY_PUNTOS = "puntos"
    private const val KEY_RACHA = "racha_dias"
    private const val KEY_ACTIVO = "activo"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(
        token: String,
        idSuscriptor: Int,
        nombre: String,
        correo: String,
        idSucursal: Int,
        puntos: Int,
        rachaDias: Int,
        activo: Int
    ) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            putInt(KEY_ID, idSuscriptor)
            putString(KEY_NOMBRE, nombre)
            putString(KEY_CORREO, correo)
            putInt(KEY_SUCURSAL, idSucursal)
            putInt(KEY_PUNTOS, puntos)
            putInt(KEY_RACHA, rachaDias)
            putInt(KEY_ACTIVO, activo)
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getIdSuscriptor(): Int = prefs.getInt(KEY_ID, -1)
    fun getNombre(): String = prefs.getString(KEY_NOMBRE, "") ?: ""
    fun getCorreo(): String = prefs.getString(KEY_CORREO, "") ?: ""
    fun getIdSucursal(): Int = prefs.getInt(KEY_SUCURSAL, -1)
    fun getPuntos(): Int = prefs.getInt(KEY_PUNTOS, 0)
    fun getRachaDias(): Int = prefs.getInt(KEY_RACHA, 0)
    fun isActivo(): Boolean = prefs.getInt(KEY_ACTIVO, 0) == 1
    fun isLoggedIn(): Boolean = getToken() != null && getIdSuscriptor() != -1

    fun updatePuntos(puntos: Int) {
        prefs.edit { putInt(KEY_PUNTOS, puntos) }
    }

    fun clearSession() {
        prefs.edit { clear() }
    }
}
