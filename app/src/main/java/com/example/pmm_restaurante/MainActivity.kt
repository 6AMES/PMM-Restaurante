package com.example.pmm_restaurante

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Cambiar color barra de notificaciones del móvil
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        val gridLayoutMesas = findViewById<GridLayout>(R.id.gridLayoutMesas)

        // Mapa para rastrear el estado de cada botón (true = green, false = yellow)
        val buttonStates = mutableMapOf<Button, Boolean>()

        for (i in 0 until gridLayoutMesas.childCount) {
            val button = gridLayoutMesas.getChildAt(i) as Button

            // Inicializar el estado del botón como green
            buttonStates[button] = true

            // Configurar el clic para navegar a PedidoActivity
            button.setOnClickListener {
                val mesaNumero = button.text.toString()
                navigateToPedidoActivity(mesaNumero)
            }

            // Configurar el long clic para alternar entre verde y amarillo
            button.setOnLongClickListener {
                val isGreen = buttonStates[button] ?: true

                // Cambiar el color según el estado
                if (isGreen) {
                    // Cambiar a amarillo
                    button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow))
                    buttonStates[button] = false
                } else {
                    // Cambiar a verde
                    button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                    buttonStates[button] = true
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