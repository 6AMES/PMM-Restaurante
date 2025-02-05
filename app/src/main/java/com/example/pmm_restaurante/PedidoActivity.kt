package com.example.pmm_restaurante

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class PedidoActivity : AppCompatActivity() {

    private lateinit var platoService: PlatoService
    private lateinit var pedidoService: PedidoService
    private lateinit var platosContainer: LinearLayout
    private lateinit var filtrosContainer: LinearLayout
    private var categoriaSeleccionada: String? = null // Almacena la categoría seleccionada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido)
        // Cambiar color barra de notificaciones del móvil
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Iniciar el Toolbar principal
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Coger el número de mesa de la intención
        val mesaNumeroStr = intent.getStringExtra("mesaNumero")

        if (mesaNumeroStr.isNullOrEmpty() || !mesaNumeroStr.matches(Regex("\\d+"))) {
            // Si el número de mesa no es válido, mostrar un mensaje de error y volver a MainActivity
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
            // Crear un Intent para navegar a MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Iniciar los servicios
        platoService = PlatoService()
        pedidoService = PedidoService(this)

        // Referencias a los contenedores
        platosContainer = findViewById(R.id.platosContainer)
        filtrosContainer = findViewById(R.id.filtrosContainer)

        // Añadir filtros dinámicos
        agregarFiltros(mesaNumero)

        // Cargar todos los platos al inicio
        cargarPlatos(categoriaSeleccionada, mesaNumero)
    }

    // Método para agregar filtros dinámicamente.
    private var filtroActivo: Button? = null // Variable para rastrear el filtro activo

    private fun agregarFiltros(mesaNumero: Int) {
        val filtros = listOf("Todos", "Entrante", "Primero", "Segundo", "Postre")
        var todosButton: Button? = null

        for (filtro in filtros) {
            // Inflar el diseño personalizado
            val view = layoutInflater.inflate(R.layout.item_filtro, filtrosContainer, false)

            // Buscar el botón dentro del diseño inflado
            val button = view.findViewById<Button>(R.id.filtroButton)
            button.text = filtro // Establecer el texto del filtro

            // Configurar el listener de clic
            button.setOnClickListener {
                onFiltroSeleccionado(button, filtro, mesaNumero)
            }

            // Guardar la referencia al botón "Todos"
            if (filtro == "Todos") {
                todosButton = button
            }

            // Añadir la vista al contenedor
            filtrosContainer.addView(view)
        }

        // Activar el filtro "Todos" por defecto
        todosButton?.let {
            onFiltroSeleccionado(it, "Todos", mesaNumero)
        }
    }

    // Método para manejar la selección de un filtro.
    private fun onFiltroSeleccionado(nuevoFiltro: Button, filtroTexto: String, mesaNumero: Int) {
        // Restaurar el estilo del filtro previamente activo
        filtroActivo?.apply {
            setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this@PedidoActivity, R.color.white)))
            setTextColor(ContextCompat.getColor(this@PedidoActivity, R.color.black))
        }

        // Actualizar el estilo del nuevo filtro seleccionado
        nuevoFiltro.apply {
            setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this@PedidoActivity, R.color.black)))
            setTextColor(ContextCompat.getColor(this@PedidoActivity, R.color.white))
        }

        // Actualizar el filtro activo
        filtroActivo = nuevoFiltro

        // Llamar al método para filtrar los platos
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

    // Método para cargar los platos según la categoría seleccionada.
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
                // Obtener el ID de la imagen en drawable usando su nombre
                val resourceId = resources.getIdentifier(imageName, "drawable", packageName)
                // Comprobar si la imagen existe
                if (resourceId != 0) {
                    imageViewPlato.setImageResource(resourceId)
                } else {
                    // Si no se encuentra la imagen, cargar una imagen predeterminada
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
            val cantidad = pedidoService.obtenerPedidoParaMesa(mesaNumero.toInt()).items.find { it.plato.id == plato.id }?.cantidad ?: 0
            textViewCantidad.text = cantidad.toString()

            // Configurar el botón "+"
            val buttonIncrementar = view.findViewById<Button>(R.id.buttonIncrementar)
            buttonIncrementar.setOnClickListener {
                pedidoService.incrementarPlato(mesaNumero.toInt(), platoService.obtenerPlatoPorId(plato.id)!!)
                // Actualizar la cantidad mostrada
                val nuevaCantidad = pedidoService.obtenerPedidoParaMesa(mesaNumero.toInt()).items.find { it.plato.id == plato.id }?.cantidad ?: 0
                textViewCantidad.text = nuevaCantidad.toString()
            }

            // Configurar el botón "-"
            val buttonDecrementar = view.findViewById<Button>(R.id.buttonDecrementar)
            buttonDecrementar.setOnClickListener {
                if (pedidoService.decrementarPlato(mesaNumero.toInt(), plato.id)) {
                    // Actualizar la cantidad mostrada
                    val nuevaCantidad = pedidoService.obtenerPedidoParaMesa(mesaNumero.toInt()).items.find { it.plato.id == plato.id }?.cantidad ?: 0
                    textViewCantidad.text = nuevaCantidad.toString()
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

        // Iniciar la actividad
        startActivity(intent)
    }
}