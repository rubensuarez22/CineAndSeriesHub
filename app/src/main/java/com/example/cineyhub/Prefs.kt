package com.example.cineyhub

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val SHARED = "UserSession"
    private val storage: SharedPreferences = context.getSharedPreferences(SHARED, 0)

    private val SHARED_NOMBRE = "nombre"
    private val SHARED_APELLIDO = "apellido"
    private val SHARED_MATRICULA = "matricula"
    private val SHARED_ROL = "rol" // Nueva clave para el rol

    fun saveNombre(nombre: String) {
        storage.edit().putString(SHARED_NOMBRE, nombre).apply()
    }

    fun saveApellido(apellido: String) {
        storage.edit().putString(SHARED_APELLIDO, apellido).apply()
    }

    fun saveMatricula(matricula: String) {
        storage.edit().putString(SHARED_MATRICULA, matricula).apply()
    }

    fun saveRol(rol: String) {
        storage.edit().putString(SHARED_ROL, rol).apply()
    }

    fun getNombre(): String? {
        return storage.getString(SHARED_NOMBRE, "")
    }

    fun getApellido(): String? {
        return storage.getString(SHARED_APELLIDO, "")
    }

    fun getMatricula(): String? {
        return storage.getString(SHARED_MATRICULA, "")
    }

    fun getRol(): String? {
        return storage.getString(SHARED_ROL, "")
    }

    fun wipe() {
        storage.edit().clear().apply()
    }
}