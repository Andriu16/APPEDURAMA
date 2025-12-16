package com.example.appedurama.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID


data class ApiResponseCursos(
    @SerializedName("termino_busqueda") val terminoBusqueda: String,
    @SerializedName("resultados") val resultados: Resultados
)

data class Resultados(
    @SerializedName("RESULTADOS DE UDEMY") val udemy: PlataformaResultados?,
    @SerializedName("RESULTADOS DE PLATZI") val platzi: PlataformaResultados?,
    @SerializedName("RESULTADOS DE UPC") val upc: PlataformaResultados?,
    @SerializedName("RESULTADOS DE ULIMA") val ulima: PlataformaResultados?
)

data class PlataformaResultados(
    val plataforma: String,
    val cursos: List<Curso>
)

data class Curso(
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("nombre") val nombre: String,
    @SerializedName("url_curso") val url: String,
    @SerializedName("imagen") val imagen: String,
    @SerializedName("plataforma") val plataforma: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("creadores") val creadores: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("lectores") val lectores: String? = null,
    @SerializedName("horas_curso") val horasCurso: String? = null,
    @SerializedName("fecha_publicacion") val fechaPublicacion: String? = null,
    @SerializedName("dificultad") val dificultad: String? = null,
    @SerializedName("numero_clases") val numeroClases: String? = null,
    @SerializedName("horas_contenido") val horasContenido: String? = null,
    @SerializedName("horas_practica") val horasPractica: String? = null,
    @SerializedName("precio_original") val precioOriginal: String? = null,
    @SerializedName("descuento_label") val descuentoLabel: String? = null,
    @SerializedName("precio_descuento") val precioDescuento: String? = null,
    @SerializedName("modalidad") val modalidad: String? = null,
    @SerializedName("certificado") val certificado: String? = null,
    @SerializedName("cuotas") val cuotas: String? = null,
    @SerializedName("fecha_inicio") val fechaInicio: String? = null,
    @SerializedName("horario") val horario: String? = null,
    @SerializedName("duracion") val duracion: String? = null,
    @SerializedName("precio") val precio: String? = null
)
data class SearchRequestBody(val termino: String)
