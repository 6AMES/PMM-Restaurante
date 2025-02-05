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
        for (i in 0 until gridLayoutMesas.childCount) {
            val button = gridLayoutMesas.getChildAt(i) as Button
            var isGreen : Boolean = true
            button.setOnClickListener {
                val mesaNumero = button.text.toString()
                navigateToPedidoActivity(mesaNumero)
            }

            for (i in 0 until gridLayoutMesas.childCount) {
                val button = gridLayoutMesas.getChildAt(i) as Button

                button.setOnLongClickListener {
                    // Alternar entre green y yellow
                    if (isGreen) {
                        // Cambiar a yellow
                        button.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                        isGreen = false // Actualizar el estado
                    } else {
                        // Cambiar a green
                        button.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                        isGreen = true // Actualizar el estado
                    }

                    true // Indica que el evento ha sido manejado
                }
            }
        }
    }

    private fun navigateToPedidoActivity(mesaNumero: String) {
        val intent = Intent(this, PedidoActivity::class.java)
        intent.putExtra("mesaNumero", mesaNumero)
        startActivity(intent)
    }
}