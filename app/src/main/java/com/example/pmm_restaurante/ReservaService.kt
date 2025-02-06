package com.example.pmm_restaurante

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MesaReservaService(private val context: Context) {

    private val PREFS_NAME = "MesaReservaPrefs"
    private val RESERVAS_KEY = "MesasReservadas"

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Mapa para almacenar el estado de las mesas
    private var estadosMesas: MutableMap<Int, Boolean> = cargarEstadosMesas()

    // Método para obtener el estado de una mesa
    fun getEstadoMesa(mesaId: Int): Boolean {
        return estadosMesas.getOrDefault(mesaId, true) // Por defecto, las mesas están disponibles (verde = true)
    }

    // Método para alternar el estado de una mesa
    fun toggleEstadoMesa(mesaId: Int): Boolean {
        val estadoActual = getEstadoMesa(mesaId)
        estadosMesas[mesaId] = !estadoActual
        guardarEstadosMesas()
        return !estadoActual
    }

    // Método para cargar los estados de las mesas desde SharedPreferences
    private fun cargarEstadosMesas(): MutableMap<Int, Boolean> {
        val gson = Gson()
        val json = sharedPreferences.getString(RESERVAS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<Map<Int, Boolean>>() {}.type
            val map: Map<Int, Boolean> = gson.fromJson(json, type)
            map.toMutableMap()
        } else {
            mutableMapOf()
        }
    }

    // Método para guardar los estados de las mesas en SharedPreferences
    private fun guardarEstadosMesas() {
        val gson = Gson()
        val json = gson.toJson(estadosMesas)
        sharedPreferences.edit().putString(RESERVAS_KEY, json).apply()
    }
}