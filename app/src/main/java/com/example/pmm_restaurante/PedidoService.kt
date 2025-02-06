package com.example.pmm_restaurante

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Modelo para cada plato dentro de un pedido y cantidad
data class PedidoPlato(
    val plato: Plato,
    var cantidad: Int
)
// Modelo para el pedido asociado a una mesa
data class Pedido(
    val mesaId: Int,
    val items: MutableList<PedidoPlato> = mutableListOf()
) {
    // Método para calcular el precio total
    fun calcularTotal(): Double {
        return items.sumOf { it.plato.precio * it.cantidad }
    }
}

class PedidoService(private val context: Context) {

    private val PREFS_NAME = "PedidoPrefs"
    private val PEDIDOS_KEY = "Pedidos"

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private var pedidosPorMesa: MutableMap<Int, Pedido> = cargarPedidos()

    fun obtenerPedidoParaMesa(mesaId: Int): Pedido {
        return pedidosPorMesa.getOrPut(mesaId) {
            Pedido(mesaId)
        }
    }

    fun incrementarPlato(mesaId: Int, plato: Plato) {
        val pedido = obtenerPedidoParaMesa(mesaId)
        val itemExistente = pedido.items.find { it.plato.id == plato.id }

        if (itemExistente != null) {
            itemExistente.cantidad += 1
        } else {
            pedido.items.add(PedidoPlato(plato, cantidad = 1))
        }
        guardarPedidos()
    }

    fun decrementarPlato(mesaId: Int, platoId: Int): Boolean {
        val pedido = pedidosPorMesa[mesaId] ?: return false
        val item = pedido.items.find { it.plato.id == platoId } ?: return false

        if (item.cantidad > 1) {
            item.cantidad -= 1
        } else {
            pedido.items.remove(item)
        }
        guardarPedidos()
        return true
    }

    fun eliminarBlatoDelPedido(mesaId: Int, platoId: Int): Boolean {
        val pedido = pedidosPorMesa[mesaId] ?: return false
        val eliminado = pedido.items.removeIf { it.plato.id == platoId }
        if (eliminado) {
            guardarPedidos()
        }
        return eliminado
    }

    fun obtenerTodosLosPedidos(): Map<Int, Pedido> {
        return pedidosPorMesa.toMap()
    }

    fun limpiarPedido(mesaId: Int): Boolean {
        val eliminado = pedidosPorMesa.remove(mesaId) != null
        if (eliminado) {
            guardarPedidos()
        }
        return eliminado
    }

    private fun cargarPedidos(): MutableMap<Int, Pedido> {
        val gson = Gson()
        val json = sharedPreferences.getString(PEDIDOS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<Map<Int, Pedido>>() {}.type
            val map: Map<Int, Pedido> = gson.fromJson(json, type)
            map.toMutableMap()
        } else {
            mutableMapOf()
        }
    }

    private fun guardarPedidos() {
        val gson = Gson()
        val json = gson.toJson(pedidosPorMesa)
        sharedPreferences.edit().putString(PEDIDOS_KEY, json).apply()
    }
}