package com.example.appedurama.data.model

data class CursoInscrito(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val imagenUrl: String?,
    val plataforma: String?,
    val fechaInscripcion: String?
)