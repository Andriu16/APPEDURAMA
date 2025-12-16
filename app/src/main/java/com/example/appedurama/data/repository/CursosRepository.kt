package com.example.appedurama.data.repository

import android.util.Log
import com.example.appedurama.data.datasource.ApiClient // Tu cliente Retrofit
import com.example.appedurama.data.model.Curso
import com.example.appedurama.data.model.SearchRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CursosRepository {

    private val apiService = ApiClient.instance


    suspend fun obtenerCursos(termino: String): Result<List<Curso>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("CursosRepository", "Iniciando búsqueda de cursos para el término: $termino")
            val requestBody = SearchRequestBody(termino = termino)

            val response = apiService.buscarCursos(requestBody)

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d("CursosRepository", "Respuesta exitosa de la API. Parseando resultados...")


                val listaCompletaDeCursos = mutableListOf<Curso>()

                apiResponse.resultados.udemy?.cursos?.let {
                    Log.d("CursosRepository", "Encontrados ${it.size} cursos en Udemy.")
                    listaCompletaDeCursos.addAll(it)
                }
                apiResponse.resultados.platzi?.cursos?.let {
                    Log.d("CursosRepository", "Encontrados ${it.size} cursos en Platzi.")
                    listaCompletaDeCursos.addAll(it)
                }
                apiResponse.resultados.upc?.cursos?.let {
                    Log.d("CursosRepository", "Encontrados ${it.size} cursos en UPC.")
                    listaCompletaDeCursos.addAll(it)
                }
                apiResponse.resultados.ulima?.cursos?.let {
                    Log.d("CursosRepository", "Encontrados ${it.size} cursos en ULIMA.")
                    listaCompletaDeCursos.addAll(it)
                }

                Log.i("CursosRepository", "Búsqueda finalizada. Total de cursos encontrados: ${listaCompletaDeCursos.size}")

                Result.success(listaCompletaDeCursos)

            } else {

                val errorMsg = "Error en la respuesta de la API: ${response.code()} - ${response.message()}"
                Log.e("CursosRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {

            Log.e("CursosRepository", "Excepción durante la llamada a la API", e)
            Result.failure(e)
        }
    }
}