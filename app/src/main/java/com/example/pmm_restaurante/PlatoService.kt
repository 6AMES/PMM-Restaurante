package com.example.pmm_restaurante

// Modelo de cada plato
data class Plato(
    var id: Int,
    var nombre: String,
    var descripcion: String,
    var alergenos: String,
    var precio: Double,
    var categoria: String, // "Entrante", "Primero", "Segundo", "Postre"
    val imagen: String,
    var disponible: Boolean = true
)

data class PlatoResumen(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val imagen: String?
)

// "Servicio" para manejar la lógica de los platos
class PlatoService {

    // Lista de platos
    private val platos: List<Plato> = listOf(
        Plato(
            id=1,
            nombre="Sformato di zucca con fonduta",
            descripcion="Delicado flan de calabaza servido con cremosa fonduta de queso.",
            alergenos="Lácteos, huevo",
            precio=4.95,
            categoria="Entrante",
            imagen="sformato_zucca"
        ),
        Plato(
            id=2,
            nombre="Insalata Russa Tradizionale",
            descripcion="Ensalada cremosa de patatas, zanahorias, guisantes y mayonesa.",
            alergenos="Huevo, mostaza",
            precio=4.95,
            categoria="Entrante",
            imagen="insalata_russa"
        ),
        Plato(
            id=3,
            nombre="Antipasto Misto",
            descripcion="Surtido de embutidos italianos, quesos y aceitunas.",
            alergenos="Lácteos",
            precio=9.95,
            categoria="Entrante",
            imagen="antipasto_misto"
        ),
        Plato(
            id=4,
            nombre="Vitello Tonnato",
            descripcion="Finas láminas de ternera con una suave salsa de atún y alcaparras.",
            alergenos="Pescado, huevo, mostaza",
            precio=5.95,
            categoria="Entrante",
            imagen="vitello_tonnato"
        ),
        Plato(
            id=5,
            nombre="Terrina di carni bianche, albicocche e mandorle",
            descripcion="Terrina de aves con albicocas y almendras, de textura suave y sabor delicado.",
            alergenos="Frutos de cáscara, huevo",
            precio=5.95,
            categoria="Entrante",
            imagen="terrina_carni"
        ),
        Plato(
            id=6,
            nombre="Tris classico astigiano",
            descripcion="Surtido tradicional de especialidades piamontesas, con vitello tonnato, insalata russa y carne cruda.",
            alergenos="Pescado, huevo, mostaza",
            precio=9.95,
            categoria="Entrante",
            imagen="tris_classico"
        ),
        Plato(
            id=7,
            nombre="Tortino di porri e gorgonzola",
            descripcion="Pastelito salado de puerros con cremoso queso gorgonzola.",
            alergenos="Lácteos, huevo",
            precio=4.95,
            categoria="Entrante",
            imagen="tortino_porri"
        ),
        Plato(
            id=8,
            nombre="Agnolotti gobbi monferrini",
            descripcion="Pasta rellena de carne y verduras, típica del Monferrato, con salsa tradicional.",
            alergenos="Gluten, huevo",
            precio=5.95,
            categoria="Primero",
            imagen="agnolotti_gobbi"
        ),
        Plato(
            id=9,
            nombre="Gnocchi alla fonduta di Toma",
            descripcion="Suaves ñoquis de patata con una cremosa fonduta de queso Toma.",
            alergenos="Lácteos, gluten",
            precio=5.95,
            categoria="Primero",
            imagen="gnocchi_fonduta"
        ),
        Plato(
            id=10,
            nombre="Tagliatelle al ragù di coniglio e rosmarino",
            descripcion="Tagliatelle con un sabroso ragú de conejo aromatizado con romero.",
            alergenos="Gluten, huevo",
            precio=5.95,
            categoria="Primero",
            imagen="tagliatelle_ragu_1"
        ),
        Plato(
            id=11,
            nombre="Tagliatelle al ragù di salsiccia",
            descripcion="Tagliatelle con un jugoso ragú de salchicha italiana en salsa de tomate.",
            alergenos="Gluten, huevo",
            precio=5.95,
            categoria="Primero",
            imagen="tagliatelle_ragu_2"
        ),
        Plato(
            id=12,
            nombre="Coniglio al Moscato",
            descripcion="Conejo cocinado a fuego lento con vino Moscato, aromatizado con hierbas y especias.",
            alergenos="Sulfitos",
            precio=8.95,
            categoria="Segundo",
            imagen="coniglio_moscato"
        ),
        Plato(
            id=13,
            nombre="Spezzatino di cinghiale con le prugne",
            descripcion="Estofado de jabalí con una mezcla dulce y salada de ciruelas.",
            alergenos="Sulfitos",
            precio=8.95,
            categoria="Segundo",
            imagen="spezzatino_cinghiale"
        ),
        Plato(
            id=14,
            nombre="Stracotto di manzo all’Arneis",
            descripcion="Estofado de carne de res cocinado lentamente en vino blanco Arneis, con hierbas y especias.",
            alergenos="Sulfitos",
            precio=8.95,
            categoria="Segundo",
            imagen="stracotto_manzo"
        ),
        Plato(
            id=15,
            nombre="Crostata di arance amare",
            descripcion="Tarta crujiente rellena de una mermelada de naranjas amargas, con un toque de dulzura y acidez.",
            alergenos="Gluten, huevo",
            precio=3.95,
            categoria="Postre",
            imagen="crostata_arance"
        ),
        Plato(
            id=16,
            nombre="Crostata di fichi",
            descripcion="Tarta crujiente rellena de higos frescos, con un sabor dulce y ligeramente afrutado.",
            alergenos="Gluten, huevo",
            precio=3.95,
            categoria="Postre",
            imagen="crostata_fichi"
        ),
        Plato(
            id=17,
            nombre="Tiramisù allo zabaione di Moscato",
            descripcion="Clásico tiramisú con una crema de zabaione infusionada con vino Moscato, suave y aromática.",
            alergenos="Gluten, huevo, lácteos, sulfitos",
            precio=3.95,
            categoria="Postre",
            imagen="tiramisu_zabaione"
        ),
        Plato(
            id=18,
            nombre="Torta di nocciole",
            descripcion="Bizcocho esponjoso de avellanas, con un sabor intenso y delicadamente dulce.",
            alergenos="Gluten, huevo, frutos de cáscara",
            precio=3.95,
            categoria="Postre",
            imagen="torta_nocciole"
        )
    )

    // Método para toda la info de un plato por id
    fun obtenerPlatoPorId(id: Int): Plato? {
        return platos.find { it.id == id }
    }

    // Método para obtener un resumen de los platos para mostrarlos
    fun obtenerTodosLosPlatosResumen(): List<PlatoResumen> {
        return platos.map { plato ->
            PlatoResumen(
                id = plato.id,
                nombre = plato.nombre,
                precio = plato.precio,
                imagen = plato.imagen
            )
        }
    }

    // Método para filtrar platos por categoría
    fun obtenerPlatosPorCategoria(categoria: String): List<PlatoResumen> {
        return platos.filter { it.categoria.equals(categoria, ignoreCase = true) }
            .map { plato ->
                PlatoResumen(
                    id = plato.id,
                    nombre = plato.nombre,
                    precio = plato.precio,
                    imagen = plato.imagen
                )
            }
    }

}