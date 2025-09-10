package com.example.appedurama.data.datasource


import android.util.Log
import com.example.appedurama.BuildConfig
import com.example.appedurama.data.model.RutaAprendizaje
import com.example.appedurama.data.model.RutasResponse
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.ai.client.generativeai.GenerativeModel

object GeminiApiService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val jsonConfig = generationConfig {
        responseMimeType = "application/json"
    }
    private val gson = Gson()


    suspend fun obtenerRecomendaciones(prompt: String): Result<String> {
        return try {
            val response = generativeModel.generateContent(prompt)
            Result.success(response.text.orEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerRutasDeAprendizaje(areasDeInteres: String): Result<List<RutaAprendizaje>> {
        val generativeModelJson = GenerativeModel(
            modelName = "gemini-2.0-flash-lite",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = jsonConfig
        )

        val prompt = construirPromptRutas(areasDeInteres)
        Log.d("GEMINI_API_CALL", "--- Iniciando llamada para obtener rutas ---")
        Log.d("GEMINI_API_CALL", "Prompt enviado:\n$prompt")

        return try {
            Log.d("GEMINI_API_CALL", "Enviando contenido a la IA...")
            val response = generativeModelJson.generateContent(prompt)
            val jsonResponse = response.text.orEmpty()
            Log.i("GEMINI_API_CALL", "Respuesta JSON cruda recibida de la IA:\n$jsonResponse")

            if (jsonResponse.isBlank()) {
                Log.e("GEMINI_API_CALL", "Error: La IA devolvió una respuesta vacía.")
                return Result.failure(Exception("La IA devolvió una respuesta vacía."))
            }
            Log.d("GEMINI_API_CALL", "Intentando parsear el JSON con GSON...")
            // Usamos GSON para convertir el string JSON en nuestros objetos Kotlin
            val rutasResponse = gson.fromJson(jsonResponse, RutasResponse::class.java)
            Log.i("GEMINI_API_CALL", "¡Éxito! JSON parseado correctamente. ${rutasResponse.rutas.size} rutas encontradas.")
            Result.success(rutasResponse.rutas)
        } catch (e: Exception) {
            Log.e("GEMINI_API_CALL", "Error durante la llamada a la API o el parseo JSON", e)
            Result.failure(e)
        }
    }

    private fun construirPromptRutas(areas: String): String {
        return """
        Eres un diseñador de currículos educativos para carreras tecnológicas.
        Basado en las siguientes áreas de interés de un usuario, genera una lista de 2 a 3 rutas de aprendizaje detalladas.
        
        Áreas de interés del usuario:
        $areas
        
        Para CADA ruta de aprendizaje, debes proporcionar:
        1. Un título claro para la ruta.
        2. Una descripción concisa de la ruta.
        3. Una lista de 3 a 5 cursos clave (solo nombres de cursos).
        4. Una lista de 3 a 5 habilidades que se adquirirán (ej: "Resolución de problemas", "Python", "SQL").
        5. Una lista de 3 a 5 oportunidades profesionales (ej: "Analista de Datos Jr.", "Ingeniero de Machine Learning").
        
        Debes devolver tu respuesta EXCLUSIVAMENTE en formato JSON, siguiendo esta estructura exacta:
        {
          "rutas_aprendizaje": [
            {
              "titulo": "Nombre de la Ruta 1",
              "descripcion": "Descripción de la ruta 1.",
              "cursos": ["Curso A", "Curso B", "Curso C"],
              "habilidades": ["Habilidad X", "Habilidad Y", "Habilidad Z"],
              "oportunidades": ["Puesto 1", "Puesto 2", "Puesto 3"]
            },
            {
              "titulo": "Nombre de la Ruta 2",
              "descripcion": "Descripción de la ruta 2.",
              "cursos": ["Curso D", "Curso E", "Curso F"],
              "habilidades": ["Habilidad A", "Habilidad B", "Habilidad C"],
              "oportunidades": ["Puesto 4", "Puesto 5", "Puesto 6"]
            }
          ]
        }
        No incluyas ningún texto, explicación o markdown antes o después del objeto JSON.
        """.trimIndent()
    }


}