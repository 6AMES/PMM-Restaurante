package com.example.pmm_restaurante

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        val mesaNumero = intent.getStringExtra("mesaNumero") ?: "Sin número"

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
        pedidoService = PedidoService()

        // Referencias a los contenedores
        platosContainer = findViewById(R.id.platosContainer)
        filtrosContainer = findViewById(R.id.filtrosContainer)

        // Agregar filtros dinámicamente
        agregarFiltros(mesaNumero.toInt())

        // Cargar todos los platos al inicio
        cargarPlatos(categoriaSeleccionada, mesaNumero.toInt())
    }

    // Método para agregar filtros dinámicamente.
    private fun agregarFiltros(mesaNumero: Int) {
        val filtros = listOf("Todos", "Entrante", "Primero", "Segundo", "Postre")

        for (filtro in filtros) {
            val button = Button(this).apply {
                text = filtro
                setBackgroundResource(android.R.drawable.btn_default_small) // Estilo de botón
                setPadding(16, 8, 16, 8) // Añadir padding
                setTextSize(14f) // Tamaño del texto
                setOnClickListener { onFiltroClicked(filtro, mesaNumero) }
            }

            // Añadir margen entre los botones
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.setMargins(8, 0, 8, 0) // Margen izquierdo, superior, derecho, inferior
            button.layoutParams = params

            // Añadir el botón al contenedor
            filtrosContainer.addView(button)
        }
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
            textViewPrecio.text = "$${plato.precio}"

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

            // Agregar la vista al contenedor
            platosContainer.addView(view)
        }
    }
}