package com.example.appedurama.data.model

data class RankingItem(
    val posicion: Long,
    val puntajeTotal: Int,
    val puntajePromedio: Double,
    val examenesCompletados: Int,
    val usuarioId: Int,
    var nombreCompleto: String = "Usuario"
)