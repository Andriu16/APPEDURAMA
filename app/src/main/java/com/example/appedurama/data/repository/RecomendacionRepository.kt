package com.example.appedurama.data.repository
import com.example.appedurama.data.datasource.GeminiApiService
import com.example.appedurama.data.model.EncuestaRespuestas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class RecomendacionRepository {
    suspend fun obtenerRecomendacionesDeIA(respuestas: EncuestaRespuestas): Result<String> = withContext(Dispatchers.IO) {
        val prompt = construirPrompt(respuestas)
        GeminiApiService.obtenerRecomendaciones(prompt)
    }

    private fun construirPrompt(respuestas: EncuestaRespuestas): String {
        return """
    Eres un orientador vocacional experto y empático.
    Analiza las siguientes respuestas de una encuesta de un estudiante y, basándote en ellas,
    proporciona una lista de 3 a 5 carreras, campos de estudio o áreas profesionales recomendadas.
    Las recomendaciones deben ser variadas y no limitarse a un solo campo como la tecnología.

    Para cada recomendación:
    1. Dale un título claro y conciso.
    2. Explica brevemente (1-2 frases) por qué encaja con el perfil del usuario, conectando directamente con sus respuestas.
    
    Formatea tu respuesta final como una lista numerada. Es MUY IMPORTANTE que cada ítem siga el formato: **Título:** Descripción.
    No añadas introducciones, conclusiones o texto extra fuera de la lista numerada.

    Respuestas del usuario:
    1. Problema en el mundo que le gustaría resolver: "${respuestas.respuesta1}"
    2. Momento en el que se sintió más motivado y productivo: "${respuestas.respuesta2}"
    3. El tipo de actividad que más le da energía es '${respuestas.respuesta3_preferencia}' y la razón que da es: "${respuestas.respuesta3_detalle}"
    4. Tareas o actividades que le resultan aburridas o agotadoras: "${respuestas.respuesta4}"

    Ejemplo de respuesta esperada:
    1. **Psicología Clínica:** Tu interés por ayudar a las personas y resolver problemas de salud mental, combinado con tu energía al interactuar con otros, sugiere una fuerte vocación en este campo.
    2. **Ingeniería Ambiental:** Tu deseo de abordar el cambio climático y tu motivación al organizar y planificar proyectos complejos encajan perfectamente con esta carrera.
    3. **Diseño Gráfico y Comunicación Visual:** Tu preferencia por la creatividad visual y tu motivación al dar vida a ideas abstractas te hacen un candidato ideal para esta área.
    """.trimIndent()
    }
}