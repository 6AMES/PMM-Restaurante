package com.example.pmm_restaurante

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class PedidoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Inicializar el Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Ocultar el título predeterminado del Toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Obtener el número de mesa de la intención
        val mesaNumero = intent.getStringExtra("mesaNumero") ?: "Sin número"

        // Inicializar el TextView del título
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = "Pedido Mesa ($mesaNumero)"

        // Aquí puedes agregar más lógica para manejar el pedido
    }
}