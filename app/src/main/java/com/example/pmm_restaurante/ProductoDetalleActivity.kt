package com.example.pmm_restaurante

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class ProductoDetalleActivity : AppCompatActivity() {

    private lateinit var platoService: PlatoService
    private lateinit var pedidoService: PedidoService
    private var mesaNumero: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_detalle)
        // Cambiar color barra de notificaciones del móvil
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Iniciar el Toolbar principal
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Recuperar el número de mesa enviado desde PedidoActivity
        mesaNumero = intent.getStringExtra("mesaNumero")

        if (mesaNumero.isNullOrEmpty()) {
            // Si no hay número de mesa, mostrar un mensaje de error y volver a MainActivity
            Toast.makeText(this, "Error: Número de mesa no disponible", Toast.LENGTH_SHORT).show()
            finish() // Cerrar esta actividad
            return
        }

        // Iniciar el título del Toolbar
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = "Pedido Mesa ($mesaNumero)"

        // Referencia al botón "Volver"
        val buttonVolver = findViewById<ImageButton>(R.id.buttonVolver)
        buttonVolver.setOnClickListener {
            val intent = Intent(this, PedidoActivity::class.java)
            intent.putExtra("mesaNumero", mesaNumero)
            startActivity(intent)
        }

        // Iniciar los servicios
        platoService = PlatoService()
        pedidoService = PedidoService(this)

        // Recuperar los datos del plato enviados desde PedidoActivity
        val platoId = intent.getIntExtra("plato_id", -1)
        val platoNombre = intent.getStringExtra("plato_nombre")
        val platoDescripcion = intent.getStringExtra("plato_descripcion")
        val platoAlergenos = intent.getStringExtra("plato_alergenos")
        val platoPrecio = intent.getDoubleExtra("plato_precio", 0.0)
        val platoCategoria = intent.getStringExtra("plato_categoria")
        val platoImagen = intent.getStringExtra("plato_imagen")

        // Mostrar los detalles del plato
        findViewById<TextView>(R.id.textViewNombre).text = platoNombre
        findViewById<TextView>(R.id.textViewDescripcion1).text = platoDescripcion
        findViewById<TextView>(R.id.textViewPrecio).text = "${platoPrecio}€"
        findViewById<TextView>(R.id.textViewCategoria1).text = platoCategoria

        // Configurar la imagen del plato
        val imageViewPlato = findViewById<ImageView>(R.id.imageViewPlato)
        platoImagen?.let { imageName ->
            val resourceId = resources.getIdentifier(imageName, "drawable", packageName)
            if (resourceId != 0) {
                imageViewPlato.setImageResource(resourceId)
            } else {
                imageViewPlato.setImageResource(R.drawable.imagen_predeterminada)
            }
        }
    }
}