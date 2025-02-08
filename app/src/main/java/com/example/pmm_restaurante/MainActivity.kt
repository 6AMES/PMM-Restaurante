package com.example.pmm_restaurante

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var pedidoActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Cambiar color barra de notificaciones del móvil
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Mapa para rastrear el estado de cada botón (true = green, false = yellow)
        val gridLayoutMesas = findViewById<GridLayout>(R.id.gridLayoutMesas)
        val mesaReservaService = MesaReservaService(this)

        for (i in 0 until gridLayoutMesas.childCount) {
            val button = gridLayoutMesas.getChildAt(i) as Button
            val mesaId = i + 1

            // Restaurar el estado del botón
            val isGreen = mesaReservaService.getEstadoMesa(mesaId)
            button.backgroundTintList = if (isGreen) {
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
            } else {
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
            }

            // Configurar el clic para navegar a PedidoActivity
            button.setOnClickListener {
                val mesaNumero = button.text.toString()
                navigateToPedidoActivity(mesaNumero)
            }

            // Configurar el long clic para alternar el estado o pagar la cuenta
            button.setOnLongClickListener {
                val sharedPreferences = getSharedPreferences("MesaEstados", MODE_PRIVATE)
                val claveRoja = "mesa_${mesaId}_roja"

                // Verificar si la mesa está marcada como roja
                val isRed = sharedPreferences.getBoolean(claveRoja, false)

                if (isRed) {
                    // La mesa está roja, marcarla como pagada
                    Toast.makeText(this, "Cuenta pagada para la mesa $mesaId", Toast.LENGTH_SHORT).show()

                    // Cambiar el color de la mesa a verde
                    button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))

                    // Actualizar el estado en SharedPreferences
                    val editor = sharedPreferences.edit()
                    editor.putBoolean(claveRoja, false) // Marcar como no roja
                    editor.apply()

                    // Vaciar la lista de pedidos para esta mesa
                    val pedidoService = PedidoService(this)
                    pedidoService.limpiarPedido(mesaId)

                    println("Cuenta pagada y lista de pedidos vaciada para la mesa $mesaId")
                } else {
                    // Si la mesa no está roja, alternar entre verde y amarillo
                    val nuevoEstado = mesaReservaService.toggleEstadoMesa(mesaId)
                    button.backgroundTintList = if (nuevoEstado) {
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                    } else {
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
                    }
                }

                true // Indicar que el evento ha sido manejado
            }
        }

        // Restaurar el estado de las mesas
        restaurarEstadosMesas(gridLayoutMesas)

        // Registrar el ActivityResultLauncher
        pedidoActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val mesaId = data?.getIntExtra("mesaId", -1) ?: -1
                println("Resultado recibido de PedidoActivity: mesaId = $mesaId") // Log para verificar el resultado
                if (mesaId != -1) {
                    println("Cambiando el color de la mesa $mesaId a rojo")
                    cambiarColorMesaARojo(mesaId)
                }
            }
        }
    }

    // Método para cambiar de página
    private fun navigateToPedidoActivity(mesaNumero: String) {
        val intent = Intent(this, PedidoActivity::class.java)
        intent.putExtra("mesaNumero", mesaNumero)
        pedidoActivityResultLauncher.launch(intent)
    }

    // Método para cambiar el color de una mesa específica a rojo
    fun cambiarColorMesaARojo(mesaId: Int) {
        val gridLayoutMesas = findViewById<GridLayout>(R.id.gridLayoutMesas)
        if (mesaId in 1..gridLayoutMesas.childCount) {
            val button = gridLayoutMesas.getChildAt(mesaId - 1) as Button
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))

            // Guardar el estado de la mesa en SharedPreferences
            val sharedPreferences = getSharedPreferences("MesaEstados", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val clave = "mesa_${mesaId}_roja"
            editor.putBoolean(clave, true)
            editor.apply()

            println("Color cambiado para la mesa $mesaId a rojo")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Log para verificar que el método se está ejecutando
        // Verificar si el resultado proviene de PedidoActivity
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Obtener el ID de la mesa desde el Intent
            val mesaId = data?.getIntExtra("mesaId", -1) ?: -1
            if (mesaId != -1) {
                // Cambiar el color de la mesa a rojo
                cambiarColorMesaARojo(mesaId)
            }
        }
    }

    // Método para restaurar el estado de las mesas al iniciar la actividad
    private fun restaurarEstadosMesas(gridLayoutMesas: GridLayout) {
        val sharedPreferences = getSharedPreferences("MesaEstados", MODE_PRIVATE)

        for (i in 0 until gridLayoutMesas.childCount) {
            val mesaId = i + 1
            val button = gridLayoutMesas.getChildAt(i) as Button

            // Leer el estado de la mesa desde SharedPreferences
            val clave = "mesa_${mesaId}_roja"
            val isRed = sharedPreferences.getBoolean(clave, false)
            if (isRed) {
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))
            } else {
                // Restaurar el estado predeterminado (amarillo o verde)
                val mesaReservaService = MesaReservaService(this)
                val isGreen = mesaReservaService.getEstadoMesa(mesaId)
                button.backgroundTintList = if (isGreen) {
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                } else {
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
                }
            }
        }
    }

    // Método para manejar el long click en las mesas
    private fun configurarLongClickEnMesas(gridLayoutMesas: GridLayout) {
        for (i in 0 until gridLayoutMesas.childCount) {
            val mesaId = i + 1
            val button = gridLayoutMesas.getChildAt(i) as Button

            button.setOnLongClickListener {
                val sharedPreferences = getSharedPreferences("MesaEstados", MODE_PRIVATE)
                val claveRoja = "mesa_${mesaId}_roja"

                // Verificar si la mesa está marcada como roja
                val isRed = sharedPreferences.getBoolean(claveRoja, false)

                if (isRed) {
                    // La mesa está roja, marcarla como pagada
                    Toast.makeText(this, "Cuenta pagada para la mesa $mesaId", Toast.LENGTH_SHORT).show()

                    // Cambiar el color de la mesa a verde
                    button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))

                    // Actualizar el estado en SharedPreferences
                    val editor = sharedPreferences.edit()
                    editor.putBoolean(claveRoja, false) // Marcar como no roja
                    editor.apply()

                    // Vaciar la lista de pedidos para esta mesa
                    val pedidoService = PedidoService(this)
                    pedidoService.limpiarPedido(mesaId)

                    println("Cuenta pagada y lista de pedidos vaciada para la mesa $mesaId")
                } else {
                    // Si la mesa no está roja, alternar entre verde y amarillo
                    val mesaReservaService = MesaReservaService(this)
                    val nuevoEstado = mesaReservaService.toggleEstadoMesa(mesaId)
                    button.backgroundTintList = if (nuevoEstado) {
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                    } else {
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
                    }
                }

                true
            }
        }
    }
}