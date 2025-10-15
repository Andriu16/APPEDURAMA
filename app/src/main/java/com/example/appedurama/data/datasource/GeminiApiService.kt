package com.example.appedurama.data.datasource


import android.util.Log
import com.example.appedurama.BuildConfig
import com.example.appedurama.data.model.QuizResponse
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

    suspend fun generarCuestionario(temario: String): Result<QuizResponse> {
        val generativeModelJson = GenerativeModel(
            modelName = "gemini-2.0-flash-lite",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = jsonConfig // Reutilizamos la config para que devuelva JSON
        )

        val prompt = construirPromptCuestionario(temario)
        Log.d("GEMINI_API_CALL", "--- Iniciando llamada para generar cuestionario ---")
        Log.d("GEMINI_API_CALL", "Prompt enviado:\n$prompt")

        return try {
            val response = generativeModelJson.generateContent(prompt)
            val jsonResponse = response.text.orEmpty()
            Log.i("GEMINI_API_CALL", "Respuesta JSON cruda del cuestionario:\n$jsonResponse")

            if (jsonResponse.isBlank()) {
                return Result.failure(Exception("La IA devolvió una respuesta vacía."))
            }

            // Usamos GSON para convertir el string JSON en nuestros objetos Kotlin
            val quizResponse = gson.fromJson(jsonResponse, QuizResponse::class.java)
            Log.i("GEMINI_API_CALL", "¡Éxito! JSON del cuestionario parseado. ${quizResponse.cuestionario.size} preguntas encontradas.")
            Result.success(quizResponse)
        } catch (e: Exception) {
            Log.e("GEMINI_API_CALL", "Error durante la llamada a la IA o el parseo del JSON del cuestionario", e)
            Result.failure(e)
        }
    }

    private fun construirPromptCuestionario(temario: String): String {
        return """
        Eres un experto educador y creador de exámenes.
        Basado en el siguiente temario extraído de cursos que un usuario ha guardado, genera un cuestionario de 10 preguntas.

        Temario del usuario:
        $temario

        Para CADA una de las 10 preguntas, debes proporcionar:
        1. El texto de la pregunta.
        2. Una lista de EXACTAMENTE 4 opciones de respuesta.
        3. El número de la respuesta correcta (un entero del 1 al 4).

        Debes devolver tu respuesta EXCLUSIVAMENTE en formato JSON, siguiendo esta estructura exacta:
        {
          "cuestionario": [
            {
              "pregunta": "Texto de la pregunta 1...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 3
            },
            {
              "pregunta": "Texto de la pregunta 2...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 1
            },
            {
              "pregunta": "Texto de la pregunta 3...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 1
            },
            {
              "pregunta": "Texto de la pregunta 4...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 1
            },
            {
              "pregunta": "Texto de la pregunta 5...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 1
            },
            {
              "pregunta": "Texto de la pregunta 6...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 1
            },
            {
              "pregunta": "Texto de la pregunta 7...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 1
            },
            {
              "pregunta": "Texto de la pregunta 8...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 1
            },
            {
              "pregunta": "Texto de la pregunta 9...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 1
            },
            {
              "pregunta": "Texto de la pregunta 10...",
              "opciones": ["Opción 1", "Opción 2", "Opción 3", "Opción 4"],
              "respuesta_correcta": 1
            }
          ]
        }
        Asegúrate de que la lista "cuestionario" contenga exactamente 10 objetos de pregunta.
        No incluyas ningún texto, explicación o markdown antes o después del objeto JSON.
        """.trimIndent()
    }


}