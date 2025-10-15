package com.example.appedurama.data.repository

import android.util.Log
import com.example.appedurama.data.datasource.ApiClient // Tu cliente Retrofit
import com.example.appedurama.data.model.Curso
import com.example.appedurama.data.model.SearchRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para obtener cursos desde la API de scraping.
 * Se encarga de llamar al endpoint, procesar la respuesta anidada
 * y devolver una lista simple de cursos.
 */
class CursosRepository {

    // Instancia del servicio de la API obtenida desde nuestro cliente Retrofit.
    private val apiService = ApiClient.instance

    /**
     * Realiza una búsqueda de cursos en la API usando un término de búsqueda.
     *
     * @param termino El string que se usará para buscar cursos (ej. "Python", "Diseño Gráfico").
     * @return Un objeto Result que contiene una lista de Cursos si tiene éxito, o una Excepción si falla.
     */
    suspend fun obtenerCursos(termino: String): Result<List<Curso>> = withContext(Dispatchers.IO) {
        // Ejecutamos la llamada en el hilo de IO (Input/Output) para no bloquear la UI.
        return@withContext try {
            Log.d("CursosRepository", "Iniciando búsqueda de cursos para el término: $termino")
            // Creamos el cuerpo de la petición POST que espera la API.
            val requestBody = SearchRequestBody(termino = termino)

            // Realizamos la llamada a la API. Esta es una llamada suspendida.
            val response = apiService.buscarCursos(requestBody)

            // Verificamos si la respuesta fue exitosa (código 2xx) y si el cuerpo no es nulo.
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d("CursosRepository", "Respuesta exitosa de la API. Parseando resultados...")

                // ---- LÓGICA CLAVE: APLANAR LA LISTA ----
                // La API devuelve los cursos anidados por plataforma.
                // Creamos una única lista para juntarlos todos.
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
                // Devolvemos el resultado exitoso con la lista aplanada.
                Result.success(listaCompletaDeCursos)

            } else {
                // Si la respuesta no fue exitosa (ej. error 400 o 500), creamos un error.
                val errorMsg = "Error en la respuesta de la API: ${response.code()} - ${response.message()}"
                Log.e("CursosRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            // Si ocurre una excepción durante la llamada (ej. no hay internet), la capturamos.
            Log.e("CursosRepository", "Excepción durante la llamada a la API", e)
            Result.failure(e)
        }
    }
}