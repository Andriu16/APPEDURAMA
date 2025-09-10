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
        Eres un orientador vocacional experto en tecnología para estudiantes.
        Analiza las siguientes respuestas de una encuesta de un usuario y, basándote en ellas,
        proporciona una lista de 3 a 5 áreas de especialización o cursos tecnológicos recomendados.
        Para cada recomendación, explica brevemente (1-2 frases) por qué encaja con el perfil del usuario.
        Formatea tu respuesta como una lista numerada, clara y concisa. No añadas introducciones o conclusiones genéricas.

        Respuestas del usuario:
        1. Problemas que le gustaría resolver: "${respuestas.respuesta1}"
        2. Cómo aborda proyectos difíciles: "${respuestas.respuesta2}"
        3. Su preferencia principal es '${respuestas.respuesta3_preferencia}' y la razón que da es: "${respuestas.respuesta3_detalle}"
        4. Una experiencia frustrante que tuvo con la tecnología: "${respuestas.respuesta4}"

        Ejemplo de respuesta esperada:
        1. Desarrollo de Aplicaciones Móviles: Tu interés en resolver problemas cotidianos y tu preferencia por la lógica encajan perfectamente con la creación de apps útiles.
        2. Ciberseguridad: Tu enfoque metódico para abordar proyectos y tu experiencia frustrante con la tecnología sugieren que te motivaría proteger sistemas.
        3. ...
        """.trimIndent()
    }
}