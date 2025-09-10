package com.example.appedurama.data.repository


import com.example.appedurama.data.datasource.GeminiApiService
import com.example.appedurama.data.model.RutaAprendizaje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RutaAprendizajeRepository {
    suspend fun obtenerRutas(areasDeInteres: String): Result<List<RutaAprendizaje>> = withContext(Dispatchers.IO) {
        GeminiApiService.obtenerRutasDeAprendizaje(areasDeInteres)
    }
}