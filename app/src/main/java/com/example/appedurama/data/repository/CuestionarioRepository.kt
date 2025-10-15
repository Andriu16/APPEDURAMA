package com.example.appedurama.data.repository

import android.util.Log
import com.example.appedurama.AccesoSql.DatabaseManager
import com.example.appedurama.data.model.CursoSeleccionado
import com.example.appedurama.data.model.PreguntaQuiz
import com.example.appedurama.data.datasource.GeminiApiService
import com.example.appedurama.ui.perfil.cuestionario.TemarioData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class CuestionarioRepository {


    suspend fun verificarDisponibilidadQuiz(usuarioId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        val sql = "EXEC dbo.sp_ObtenerUltimaFechaCuestionario ?"
        val result = DatabaseManager.executeSelectOne(sql, listOf(usuarioId)) { rs ->
            rs.getString("C_fecha")
        }

        return@withContext result.map { fechaString ->
            if (fechaString == null) {
                Log.d("CuestionarioRepo", "No hay cuestionarios previos. El usuario puede continuar.")
                return@map true // No hay fecha, puede hacer el quiz
            }

            try {
                // Formato de fecha de SQL Server: "2023-10-27 15:45:23.123"
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
                val ultimaFecha = LocalDateTime.parse(fechaString, formatter)
                val ahoraEnPeru = LocalDateTime.now(ZoneId.of("America/Lima"))

                val duracion = Duration.between(ultimaFecha, ahoraEnPeru)
                val diasPasados = duracion.toDays()

                Log.d("CuestionarioRepo", "Último quiz: $ultimaFecha. Ahora: $ahoraEnPeru. Días pasados: $diasPasados")
                return@map diasPasados >= 7
            } catch (e: DateTimeParseException) {
                Log.e("CuestionarioRepo", "Error al parsear la fecha: $fechaString", e)
                return@map false // En caso de error, bloqueamos por seguridad
            }
        }
    }

    //chekear
//    suspend fun obtenerTemarioParaQuiz(usuarioId: Int): Result<String> = withContext(Dispatchers.IO) {
//        val sql = "SELECT CS_titulo FROM CursosSeleccionados WHERE CS_usuarioID = ?"
//        val result = DatabaseManager.executeSelectList(sql, listOf(usuarioId)) { rs ->
//            rs.getString("CS_titulo") // Solo necesitamos el título
//        }
//
//        return@withContext result.map { titulos ->
//            if (titulos.isEmpty()) {
//                "El usuario no ha seleccionado cursos. Genera un cuestionario de cultura general sobre tecnología."
//            } else {
//
//                titulos.joinToString(separator = " / ")
//            }
//        }
//    }
    suspend fun tieneCursosSeleccionados(usuarioId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        // Usamos "SELECT TOP 1 1" que es muy eficiente. Solo nos importa si existe al menos una fila.
        val sql = "SELECT TOP 1 1 FROM CursosSeleccionados WHERE CS_usuarioID = ?"
        val result = DatabaseManager.executeSelectOne(sql, listOf(usuarioId)) { rs ->
            rs.getInt(1) // Si encuentra una fila, esto devolverá 1
        }
        // Si el resultado no es nulo, significa que se encontró una fila.
        return@withContext result.map { it != null }
    }
    suspend fun obtenerTemarioParaQuiz(usuarioId: Int): Result<TemarioData> = withContext(Dispatchers.IO) {
        val sql = "SELECT CS_titulo, CS_descripcion FROM CursosSeleccionados WHERE CS_usuarioID = ?"
        val result = DatabaseManager.executeSelectList(sql, listOf(usuarioId)) { rs ->
            CursoSeleccionado(
                titulo = rs.getString("CS_titulo"),
                descripcion = rs.getString("CS_descripcion")
            )
        }

        return@withContext result.map { cursos ->
            // AHORA ESTA FUNCIÓN ASUME QUE LOS CURSOS EXISTEN.
            // Si la lista está vacía (lo que no debería ocurrir si la verificación previa funciona),
            // lanzamos un error para identificar el problema.
            if (cursos.isEmpty()) {
                throw IllegalStateException("Se intentó generar un temario para un usuario sin cursos seleccionados. La verificación previa falló.")
            }

            // La lógica para crear los temarios se mantiene igual.
            val temarioIA = cursos.joinToString(separator = "\n\n") {
                "Título: ${it.titulo}\nDescripción: ${it.descripcion ?: ""}"
            }
            val temarioDB = cursos.joinToString(separator = " / ") { it.titulo }

            TemarioData(paraIA = temarioIA, paraBaseDeDatos = temarioDB)
        }
    }

    suspend fun generarCuestionarioConIA(temario: String): Result<List<PreguntaQuiz>> {
        return GeminiApiService.generarCuestionario(temario).map { it.cuestionario }
    }

    suspend fun guardarResultadoQuiz(
        usuarioId: Int,
        temario: String,
        tiempoRespuestaSegundos: Int,
        preguntas: List<PreguntaQuiz>,
        respuestasUsuario: Map<Int, Int>, // Mapa de [índice de pregunta -> opción marcada]
        puntaje: Int
    ): Result<Int> = withContext(Dispatchers.IO) {

        val minutos = tiempoRespuestaSegundos / 60
        val segundos = tiempoRespuestaSegundos % 60
        val tiempoFormateado = "$minutos:${String.format("%02d", segundos)}"

        // El SQL es enorme, lo construimos dinámicamente
        val columnas = StringBuilder("C_usuarioID, C_temario, C_tiempoRespuesta, C_puntaje")
        val valores = StringBuilder("?, ?, ?, ?")
        val params = mutableListOf<Any>(usuarioId, temario, tiempoFormateado, "$puntaje/10")

        for (i in 0 until 10) {
            val preguntaNum = i + 1
            columnas.append(", C_pregunta$preguntaNum, C_p${preguntaNum}_opcion1, C_p${preguntaNum}_opcion2, C_p${preguntaNum}_opcion3, C_p${preguntaNum}_opcion4, C_p${preguntaNum}_respuesta_marcada, C_p${preguntaNum}_respuesta_correcta")
            valores.append(", ?, ?, ?, ?, ?, ?, ?")

            val p = preguntas.getOrElse(i) { PreguntaQuiz("N/A", List(4) { "N/A" }, 0) }
            params.add(p.pregunta)
            params.add(p.opciones.getOrElse(0) { "N/A" })
            params.add(p.opciones.getOrElse(1) { "N/A" })
            params.add(p.opciones.getOrElse(2) { "N/A" })
            params.add(p.opciones.getOrElse(3) { "N/A" })
            params.add(respuestasUsuario[i] ?: 0) // Respuesta marcada (0 si no respondió)
            params.add(p.respuestaCorrecta)
        }

        val sql = "INSERT INTO Cuestionario ($columnas) VALUES ($valores)"

        return@withContext DatabaseManager.executeUpdateOperation(sql, params)
    }
}