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
    private lateinit var textViewArticulosSeleccionados: TextView
    private lateinit var buttonVerPedido: Button
    private lateinit var pedidoResumenLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido)

        // Cambiar color barra de notificaciones del móvil
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Iniciar el Toolbar principal
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Obtener el número de mesa de la intención
        val mesaNumeroStr = intent.getStringExtra("mesaNumero")
        if (mesaNumeroStr.isNullOrEmpty() || !mesaNumeroStr.matches(Regex("\\d+"))) {
            Toast.makeText(this, "Número de mesa no válido", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        val mesaNumero = mesaNumeroStr.toInt()

        // Iniciar el título del Toolbar
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = "Pedido Mesa ($mesaNumero)"

        // Referencia al botón "Volver"
        val buttonVolver = findViewById<ImageButton>(R.id.buttonVolver)
        buttonVolver.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Iniciar servicios
        platoService = PlatoService()
        pedidoService = PedidoService(this)

        // Referencias a los contenedores
        platosContainer = findViewById(R.id.platosContainer)
        filtrosContainer = findViewById(R.id.filtrosContainer)
        resumenPedidosContainer = findViewById(R.id.resumenPedidosContainer)
        textViewArticulosSeleccionados = findViewById(R.id.textViewArticulosSeleccionados)
        buttonVerPedido = findViewById(R.id.buttonVerPedido)
        pedidoResumenLayout = findViewById(R.id.pedidoResumenLayout)

        // Añadir filtros dinámicamente
        agregarFiltros(mesaNumero)

        // Cargar todos los platos al inicio
        cargarPlatos(null, mesaNumero)

        // Configurar el botón "Ver pedido"
        configurarBotonVerPedido(mesaNumero)
    }

    /**
     * Método para configurar el botón "Ver pedido".
     */
    private fun configurarBotonVerPedido(mesaId: Int) {
        buttonVerPedido.setOnClickListener {
            // Alternar la visibilidad del menú desplegable
            if (pedidoResumenLayout.visibility == View.GONE) {
                mostrarResumenPedido(mesaId)
                pedidoResumenLayout.visibility = View.VISIBLE
            } else {
                pedidoResumenLayout.visibility = View.GONE
            }
        }
    }

    /**
     * Método para mostrar el resumen del pedido.
     */
    private fun mostrarResumenPedido(mesaId: Int) {
        // Limpiar el contenedor de resumen
        resumenPedidosContainer.removeAllViews()

        // Obtener el pedido actual
        val pedido = pedidoService.obtenerPedidoParaMesa(mesaId)

        // Calcular el número de artículos seleccionados
        val numArticulosSeleccionados = pedido.items.sumOf { it.cantidad }
        textViewArticulosSeleccionados.text = "$numArticulosSeleccionados Artículos seleccionados"

        // Mostrar cada plato seleccionado
        for (item in pedido.items) {
            val view = layoutInflater.inflate(R.layout.item_resumen_pedido, resumenPedidosContainer, false)

            // Configurar el nombre del plato
            val textViewNombre = view.findViewById<TextView>(R.id.textViewNombre)
            textViewNombre.text = item.plato.nombre

            // Configurar la cantidad
            val textViewCantidad = view.findViewById<TextView>(R.id.textViewCantidad)
            textViewCantidad.text = "x${item.cantidad}"

            // Configurar el precio total del plato
            val textViewPrecio = view.findViewById<TextView>(R.id.textViewPrecio)
            textViewPrecio.text = "${item.plato.precio * item.cantidad}€"

            // Agregar la vista al contenedor
            resumenPedidosContainer.addView(view)
        }

        // Mostrar el precio total del pedido
        val totalPedido = pedido.calcularTotal()

        // Configurar el TextView del total
        val textViewTotalPedido = findViewById<TextView>(R.id.textViewTotalPedido)
        textViewTotalPedido.text = "Total: $totalPedido€"

        // Configurar el botón "Enviar pedido"
        findViewById<Button>(R.id.buttonEnviarPedido).setOnClickListener {
            Toast.makeText(this, "Pedido enviado", Toast.LENGTH_SHORT).show()
            pedidoResumenLayout.visibility = View.GONE
        }
    }

    private var filtroActivo: Button? = null

    /**
     * Método para agregar filtros dinámicamente.
     */
    private fun agregarFiltros(mesaNumero: Int) {
        val filtros = listOf("Todos", "Entrante", "Primero", "Segundo", "Postre")
        var todosButton: Button? = null

        for (filtro in filtros) {
            val view = layoutInflater.inflate(R.layout.item_filtro, filtrosContainer, false)
            val button = view.findViewById<Button>(R.id.filtroButton)
            button.text = filtro
            button.setOnClickListener { onFiltroSeleccionado(button, filtro, mesaNumero) }
            if (filtro == "Todos") {
                todosButton = button
            }
            filtrosContainer.addView(view)
        }

        // Activar el filtro "Todos" por defecto
        todosButton?.let {
            onFiltroSeleccionado(it, "Todos", mesaNumero)
        }
    }

    /**
     * Método para manejar la selección de un filtro.
     */
    private fun onFiltroSeleccionado(nuevoFiltro: Button, filtroTexto: String, mesaNumero: Int) {
        filtroActivo?.apply {
            setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this@PedidoActivity, R.color.white)))
            setTextColor(ContextCompat.getColor(this@PedidoActivity, R.color.black))
        }

        nuevoFiltro.apply {
            setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this@PedidoActivity, R.color.black)))
            setTextColor(ContextCompat.getColor(this@PedidoActivity, R.color.white))
        }

        filtroActivo = nuevoFiltro
        onFiltroClicked(filtroTexto, mesaNumero)
    }

    // Método para manejar el clic en un filtro.
    private fun onFiltroClicked(categoria: String, mesaNumero: Int) {
        // Restablecer la categoría seleccionada si es "Todos"
        this.categoriaSeleccionada = if (categoria == "Todos") null else categoria

        // Limpiar el contenedor de platos
        platosContainer.removeAllViews()

        // Cargar los platos filtrados
        cargarPlatos(categoriaSeleccionada, mesaNumero)
    }

    /**
     * Método para cargar los platos según la categoría seleccionada.
     */
    private fun cargarPlatos(categoria: String?, mesaNumero: Int) {
        val platosResumen = if (categoria == null) {
            platoService.obtenerTodosLosPlatosResumen()
        } else {
            platoService.obtenerPlatosPorCategoria(categoria)
        }

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
            val textViewNombre = view.findViewById<TextView>(R.id.textViewNombre)
            textViewNombre.text = plato.nombre

            // Configurar el precio del plato
            val textViewPrecio = view.findViewById<TextView>(R.id.textViewPrecio)
            textViewPrecio.text = "${plato.precio}€"

            // Configurar la cantidad inicial del plato
            val textViewCantidad = view.findViewById<TextView>(R.id.textViewCantidad)
            val cantidad = pedidoService.obtenerPedidoParaMesa(mesaNumero).items.find { it.plato.id == plato.id }?.cantidad ?: 0
            textViewCantidad.text = cantidad.toString()

            // Configurar el botón "+"
            val buttonIncrementar = view.findViewById<Button>(R.id.buttonIncrementar)
            buttonIncrementar.setOnClickListener {
                pedidoService.incrementarPlato(mesaNumero, platoService.obtenerPlatoPorId(plato.id)!!)
                val nuevaCantidad = pedidoService.obtenerPedidoParaMesa(mesaNumero).items.find { it.plato.id == plato.id }?.cantidad ?: 0
                textViewCantidad.text = nuevaCantidad.toString()
                actualizarNumArticulosSeleccionados(mesaNumero)
            }

            // Configurar el botón "-"
            val buttonDecrementar = view.findViewById<Button>(R.id.buttonDecrementar)
            buttonDecrementar.setOnClickListener {
                if (pedidoService.decrementarPlato(mesaNumero, plato.id)) {
                    val nuevaCantidad = pedidoService.obtenerPedidoParaMesa(mesaNumero).items.find { it.plato.id == plato.id }?.cantidad ?: 0
                    textViewCantidad.text = nuevaCantidad.toString()
                    actualizarNumArticulosSeleccionados(mesaNumero)
                }
            }

            // Configurar el ImageButton para ver más detalles
            val imageButtonInfo = view.findViewById<ImageButton>(R.id.imageButtonInfo)
            imageButtonInfo.setOnClickListener {
                navigateToProductoDetalleActivity(plato)
            }

            // Agregar la vista al contenedor
            platosContainer.addView(view)
        }
    }

    /**
     * Método para actualizar el número de artículos seleccionados.
     */
    private fun actualizarNumArticulosSeleccionados(mesaId: Int) {
        val numArticulosSeleccionados = pedidoService.obtenerPedidoParaMesa(mesaId).items.sumOf { it.cantidad }
        findViewById<TextView>(R.id.textViewArticulosSeleccionados).text = "$numArticulosSeleccionados Artículos seleccionados"
    }

    // Declarar una constante para identificar el resultado
    private val REQUEST_CODE_DETALLE = 100

    // Método para navegar a ProductoDetalleActivity
    private fun navigateToProductoDetalleActivity(platoResumen: PlatoResumen) {
        // Obtener el plato completo desde PlatoService
        val platoCompleto = platoService.obtenerPlatoPorId(platoResumen.id) ?: run {
            Toast.makeText(this, "No se encontró el plato", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el número de mesa actual
        val mesaNumero = intent.getStringExtra("mesaNumero") ?: "Sin número"

        // Crear un Intent para navegar a ProductoDetalleActivity
        val intent = Intent(this, ProductoDetalleActivity::class.java)
        // Enviar los campos del plato como extras
        intent.putExtra("plato_id", platoCompleto.id)
        intent.putExtra("plato_nombre", platoCompleto.nombre)
        intent.putExtra("plato_descripcion", platoCompleto.descripcion)
        intent.putExtra("plato_alergenos", platoCompleto.alergenos)
        intent.putExtra("plato_precio", platoCompleto.precio)
        intent.putExtra("plato_categoria", platoCompleto.categoria)
        intent.putExtra("plato_imagen", platoCompleto.imagen)
        intent.putExtra("mesaNumero", mesaNumero)

        // Iniciar la actividad esperando un resultado
        startActivityForResult(intent, REQUEST_CODE_DETALLE)
    }

    // Sobrescribir onActivityResult para manejar el resultado
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Verificar si el resultado proviene de ProductoDetalleActivity
        if (requestCode == REQUEST_CODE_DETALLE && resultCode == RESULT_OK) {
            // Obtener el número de mesa
            val mesaNumeroStr = intent.getStringExtra("mesaNumero")
            if (!mesaNumeroStr.isNullOrEmpty() && mesaNumeroStr.matches(Regex("\\d+"))) {
                val mesaNumero = mesaNumeroStr.toInt()

                // Actualizar la interfaz de usuario
                actualizarNumArticulosSeleccionados(mesaNumero)
                cargarPlatos(categoriaSeleccionada, mesaNumero)
            }
        }
    }
}