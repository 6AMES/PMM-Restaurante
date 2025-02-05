package com.example.pmm_restaurante

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

// "Servicio" para manejar la lógica de los pedidos
class PedidoService {

    // Mapa para almacenar los pedidos por mesa
    private val pedidosPorMesa: MutableMap<Int, Pedido> = mutableMapOf()

    // Método para obtener o crear un pedido para una mesa
    fun obtenerPedidoParaMesa(mesaId: Int): Pedido {
        return pedidosPorMesa.getOrPut(mesaId) { Pedido(mesaId) }
    }

    // Método para incrementar la cantidad de un plato en el pedido
    fun incrementarPlato(mesaId: Int, plato: Plato) {
        val pedido = obtenerPedidoParaMesa(mesaId)
        val itemExistente = pedido.items.find { it.plato.id == plato.id }

        if (itemExistente != null) {
            itemExistente.cantidad += 1
        } else {
            pedido.items.add(PedidoPlato(plato, cantidad = 1))
        }
    }

    // Método para decrementar la cantidad de un plato en el pedido
    fun decrementarPlato(mesaId: Int, platoId: Int): Boolean {
        val pedido = pedidosPorMesa[mesaId] ?: return false
        val item = pedido.items.find { it.plato.id == platoId } ?: return false

        if (item.cantidad > 1) {
            item.cantidad -= 1
        } else {
            pedido.items.remove(item)
        }
        return true
    }

    // Método para eliminar un plato del pedido
    fun eliminarPlatoDelPedido(mesaId: Int, platoId: Int): Boolean {
        val pedido = pedidosPorMesa[mesaId] ?: return false
        return pedido.items.removeIf { it.plato.id == platoId }
    }

    // Método para obtener todos los pedidos
    fun obtenerTodosLosPedidos(): Map<Int, Pedido> {
        return pedidosPorMesa.toMap()
    }

    // Método para limpiar el pedido de una mesa
    fun limpiarPedido(mesaId: Int): Boolean {
        return pedidosPorMesa.remove(mesaId) != null
    }

}