package com.example.pmm_restaurante

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class PedidoActivity : AppCompatActivity() {

    private lateinit var platoService: PlatoService
    private lateinit var pedidoService: PedidoService
    private lateinit var platosContainer: LinearLayout
    private lateinit var filtrosContainer: LinearLayout
    private lateinit var resumenPedidosContainer: LinearLayout
    private var categoriaSeleccionada: String? = null
    private var filtroActivo: Button? = null
    private lateinit var textViewArticulosSeleccionados: TextView
    private lateinit var buttonVerPedido: Button
    private lateinit var pedidoResumenLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido)

        // Configuración inicial de la interfaz
        configurarUI()
        val mesaNumero = obtenerMesaNumero()
        if (mesaNumero == null) {
            Toast.makeText(this, "Número de mesa no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializar servicios y cargar datos
        platoService = PlatoService()
        pedidoService = PedidoService(this)
        agregarFiltros(mesaNumero)
        cargarPlatos(null, mesaNumero)
        configurarBotonVerPedido(mesaNumero)
        actualizarNumArticulosSeleccionados(mesaNumero)
    }

    // Configura la interfaz de usuario inicial
    private fun configurarUI() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<ImageButton>(R.id.buttonVolver).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        platosContainer = findViewById(R.id.platosContainer)
        filtrosContainer = findViewById(R.id.filtrosContainer)
        resumenPedidosContainer = findViewById(R.id.resumenPedidosContainer)
        textViewArticulosSeleccionados = findViewById(R.id.textViewArticulosSeleccionados)
        buttonVerPedido = findViewById(R.id.buttonVerPedido)
        pedidoResumenLayout = findViewById(R.id.pedidoResumenLayout)
    }

    // Obtiene el número de mesa desde el Intent
    private fun obtenerMesaNumero(): Int? {
        val mesaNumeroStr = intent.getStringExtra("mesaNumero")
        return if (!mesaNumeroStr.isNullOrEmpty() && mesaNumeroStr.matches(Regex("\\d+"))) {
            findViewById<TextView>(R.id.toolbar_title).text = "Pedido Mesa ($mesaNumeroStr)"
            mesaNumeroStr.toInt()
        } else {
            null
        }
    }

    // Configura el botón "Ver pedido"
    private fun configurarBotonVerPedido(mesaId: Int) {
        buttonVerPedido.setOnClickListener {
            if (pedidoResumenLayout.visibility == View.GONE) {
                mostrarResumenPedido(mesaId)
                pedidoResumenLayout.visibility = View.VISIBLE
            } else {
                pedidoResumenLayout.visibility = View.GONE
            }
        }
    }

    // Muestra el resumen del pedido actual
    private fun mostrarResumenPedido(mesaId: Int) {
        resumenPedidosContainer.removeAllViews()
        val pedido = pedidoService.obtenerPedidoParaMesa(mesaId)
        textViewArticulosSeleccionados.text = "${pedido.items.sumOf { it.cantidad }} Artículos seleccionados"

        for (item in pedido.items) {
            val view = layoutInflater.inflate(R.layout.item_resumen_pedido, resumenPedidosContainer, false)
            view.findViewById<TextView>(R.id.textViewNombre).text = item.plato.nombre
            view.findViewById<TextView>(R.id.textViewCantidad).text = "x${item.cantidad}"
            view.findViewById<TextView>(R.id.textViewPrecio).text = "${item.plato.precio * item.cantidad}€"
            resumenPedidosContainer.addView(view)
        }

        findViewById<TextView>(R.id.textViewTotalPedido).text = "Total: ${pedido.calcularTotal()}€"
        findViewById<Button>(R.id.buttonEnviarPedido).setOnClickListener {
            Toast.makeText(this, "Pedido enviado", Toast.LENGTH_SHORT).show()
            pedidoResumenLayout.visibility = View.GONE
        }
    }

    // Agrega los filtros dinámicamente
    private fun agregarFiltros(mesaNumero: Int) {
        val filtros = listOf("Todos", "Entrante", "Primero", "Segundo", "Postre")
        var todosButton: Button? = null

        for (filtro in filtros) {
            val view = layoutInflater.inflate(R.layout.item_filtro, filtrosContainer, false)
            val button = view.findViewById<Button>(R.id.filtroButton).apply {
                text = filtro
                setOnClickListener { onFiltroSeleccionado(this, filtro, mesaNumero) }
            }
            if (filtro == "Todos") todosButton = button
            filtrosContainer.addView(view)
        }

        todosButton?.let { onFiltroSeleccionado(it, "Todos", mesaNumero) }
    }

    // Maneja la selección de un filtro
    private fun onFiltroSeleccionado(nuevoFiltro: Button, filtroTexto: String, mesaNumero: Int) {
        filtroActivo?.apply {
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@PedidoActivity, R.color.white))
            setTextColor(ContextCompat.getColor(this@PedidoActivity, R.color.black))
        }
        nuevoFiltro.apply {
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@PedidoActivity, R.color.black))
            setTextColor(ContextCompat.getColor(this@PedidoActivity, R.color.white))
        }
        filtroActivo = nuevoFiltro
        onFiltroClicked(filtroTexto, mesaNumero)
    }

    // Filtra los platos según la categoría seleccionada
    private fun onFiltroClicked(categoria: String, mesaNumero: Int) {
        categoriaSeleccionada = if (categoria == "Todos") null else categoria
        cargarPlatos(categoriaSeleccionada, mesaNumero)
    }

    // Carga los platos según la categoría seleccionada
    // Carga los platos según la categoría seleccionada
    private fun cargarPlatos(categoria: String?, mesaNumero: Int) {
        val platosResumen = if (categoria == null) {
            platoService.obtenerTodosLosPlatosResumen()
        } else {
            platoService.obtenerPlatosPorCategoria(categoria)
        }
        platosContainer.removeAllViews()
        for (plato in platosResumen) {
            val view = layoutInflater.inflate(R.layout.item_plato_scrollview, platosContainer, false)

            // Configurar la imagen del plato
            val imageViewPlato = view.findViewById<ImageView>(R.id.imageViewPlato)
            plato.imagen?.let { imageName ->
                val resourceId = resources.getIdentifier(imageName, "drawable", packageName)
                if (resourceId != 0) {
                    imageViewPlato.setImageResource(resourceId)
                } else {
                    imageViewPlato.setImageResource(R.drawable.imagen_predeterminada)
                }
            }

            // Configurar el nombre del plato
            view.findViewById<TextView>(R.id.textViewNombre).text = plato.nombre

            // Configurar el precio del plato
            view.findViewById<TextView>(R.id.textViewPrecio).text = "${plato.precio}€"

            // Configurar la cantidad inicial del plato
            val cantidad = pedidoService.obtenerPedidoParaMesa(mesaNumero).items.find { it.plato.id == plato.id }?.cantidad ?: 0
            view.findViewById<TextView>(R.id.textViewCantidad).text = cantidad.toString()

            // Configurar el botón "+"
            view.findViewById<Button>(R.id.buttonIncrementar).setOnClickListener {
                pedidoService.incrementarPlato(mesaNumero, platoService.obtenerPlatoPorId(plato.id)!!)
                val nuevaCantidad = pedidoService.obtenerPedidoParaMesa(mesaNumero).items.find { it.plato.id == plato.id }?.cantidad ?: 0
                view.findViewById<TextView>(R.id.textViewCantidad).text = nuevaCantidad.toString()
                actualizarNumArticulosSeleccionados(mesaNumero)
            }

            // Configurar el botón "-"
            view.findViewById<Button>(R.id.buttonDecrementar).setOnClickListener {
                if (pedidoService.decrementarPlato(mesaNumero, plato.id)) {
                    val nuevaCantidad = pedidoService.obtenerPedidoParaMesa(mesaNumero).items.find { it.plato.id == plato.id }?.cantidad ?: 0
                    view.findViewById<TextView>(R.id.textViewCantidad).text = nuevaCantidad.toString()
                    actualizarNumArticulosSeleccionados(mesaNumero)
                }
            }

            // Configurar el ImageButton para ver más detalles
            view.findViewById<ImageButton>(R.id.imageButtonInfo).setOnClickListener {
                navigateToProductoDetalleActivity(plato)
            }

            // Agregar la vista al contenedor
            platosContainer.addView(view)
        }
    }

    // Actualiza el número de artículos seleccionados
    private fun actualizarNumArticulosSeleccionados(mesaId: Int) {
        val numArticulosSeleccionados = pedidoService.obtenerPedidoParaMesa(mesaId).items.sumOf { it.cantidad }
        textViewArticulosSeleccionados.text = "$numArticulosSeleccionados Artículos seleccionados"
    }

    // Navega a la actividad de detalles del producto
    private fun navigateToProductoDetalleActivity(platoResumen: PlatoResumen) {
        val platoCompleto = platoService.obtenerPlatoPorId(platoResumen.id) ?: run {
            Toast.makeText(this, "No se encontró el plato", Toast.LENGTH_SHORT).show()
            return
        }

        val mesaNumero = intent.getStringExtra("mesaNumero") ?: "Sin número"
        val intent = Intent(this, ProductoDetalleActivity::class.java).apply {
            putExtra("plato_id", platoCompleto.id)
            putExtra("plato_nombre", platoCompleto.nombre)
            putExtra("plato_descripcion", platoCompleto.descripcion)
            putExtra("plato_alergenos", platoCompleto.alergenos)
            putExtra("plato_precio", platoCompleto.precio)
            putExtra("plato_categoria", platoCompleto.categoria)
            putExtra("plato_imagen", platoCompleto.imagen)
            putExtra("mesaNumero", mesaNumero)
        }
        startActivityForResult(intent, 100)
    }

    // Maneja el resultado de ProductoDetalleActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val mesaNumero = intent.getStringExtra("mesaNumero")?.toIntOrNull() ?: return
            actualizarNumArticulosSeleccionados(mesaNumero)
            cargarPlatos(categoriaSeleccionada, mesaNumero)
        }
    }
}