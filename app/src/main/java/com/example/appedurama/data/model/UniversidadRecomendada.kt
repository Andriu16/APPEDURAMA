package com.example.appedurama.data.model

import com.google.gson.annotations.SerializedName

data class UniversidadesResponse(
    val universidades: List<UniversidadRecomendada>
)


data class UniversidadRecomendada(
    val nombre: String,
    val descripcion: String,
    @SerializedName("sitio_web")
    val sitioWeb: String
)