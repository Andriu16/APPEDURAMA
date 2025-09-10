package com.example.appedurama.data.model


import com.google.gson.annotations.SerializedName


data class RutasResponse(
    @SerializedName("rutas_aprendizaje")
    val rutas: List<RutaAprendizaje>
)

data class RutaAprendizaje(
    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("cursos")
    val cursos: List<String>,

    @SerializedName("habilidades")
    val habilidades: List<String>,

    @SerializedName("oportunidades")
    val oportunidades: List<String>,

    // Propiedades para controlar la UI (expandido/colapsado)
    @Transient var cursosExpandido: Boolean = false,
    @Transient var habilidadesExpandido: Boolean = false,
    @Transient var oportunidadesExpandido: Boolean = false
)