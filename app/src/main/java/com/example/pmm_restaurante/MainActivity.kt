package com.example.pmm_restaurante

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
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
            val mesaId = i + 1 // Suponemos que los IDs de las mesas son secuenciales

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

            // Configurar el long clic para alternar el estado
            button.setOnLongClickListener {
                val nuevoEstado = mesaReservaService.toggleEstadoMesa(mesaId)
                button.backgroundTintList = if (nuevoEstado) {
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                } else {
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
                }
                true
            }
        }
    }

    private fun navigateToPedidoActivity(mesaNumero: String) {
        val intent = Intent(this, PedidoActivity::class.java)
        intent.putExtra("mesaNumero", mesaNumero)
        startActivity(intent)
    }
}