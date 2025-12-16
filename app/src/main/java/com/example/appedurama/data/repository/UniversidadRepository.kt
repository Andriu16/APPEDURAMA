package com.example.appedurama.data.repository

import com.example.appedurama.data.datasource.GeminiApiService
import com.example.appedurama.data.model.UniversidadRecomendada
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UniversidadRepository {
    suspend fun obtenerRecomendaciones(respuestas: String): Result<List<UniversidadRecomendada>> = withContext(Dispatchers.IO) {
        GeminiApiService.obtenerUniversidadesRecomendadas(respuestas)
    }
}