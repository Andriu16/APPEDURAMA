package com.example.appedurama.data.repository

import android.util.Log
import com.example.appedurama.AccesoSql.DatabaseManager
import com.example.appedurama.data.model.CursoSeleccionado
import com.example.appedurama.data.model.PreguntaQuiz
import com.example.appedurama.data.datasource.GeminiApiService
import com.example.appedurama.ui.perfil.cuestionario.TemarioData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

private val TAG = "QuizRepoDebug"
class CuestionarioRepository {


    suspend fun verificarDisponibilidadQuiz(usuarioId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        val sql = "EXEC dbo.sp_ObtenerUltimaFechaCuestionario ?"
        val result = DatabaseManager.executeSelectOne(sql, listOf(usuarioId)) { rs ->
            // La base de datos podría devolver "2025-10-16 00:00:00.000" aunque solo te interese la fecha.
            // Tomamos solo los primeros 10 caracteres para asegurar el formato YYYY-MM-DD.
            rs.getString("C_fecha")?.take(10)
        }

        return@withContext result.map { fechaString ->
            if (fechaString == null) {
                Log.d(TAG, "verificarDisponibilidadQuiz: No se encontró fecha previa. PERMITIDO.")
                return@map true
            }

            Log.d(TAG, "verificarDisponibilidadQuiz: Fecha extraída de la DB = '$fechaString'")

            try {
                // 1. Nuevo formateador, mucho más simple.
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                // 2. Usamos LocalDate para parsear, ya que no hay hora.
                val ultimaFecha = LocalDate.parse(fechaString, formatter)

                // 3. Obtenemos la fecha actual en Perú, también como LocalDate.
                val ahoraEnPeru = LocalDate.now(ZoneId.of("America/Lima"))

                Log.d(TAG, "verificarDisponibilidadQuiz: Fecha DB parseada   = $ultimaFecha")
                Log.d(TAG, "verificarDisponibilidadQuiz: Fecha Actual (Perú) = $ahoraEnPeru")

                // 4. Usamos ChronoUnit.DAYS.between, la forma ideal de calcular días entre LocalDates.
                val diasPasados = ChronoUnit.DAYS.between(ultimaFecha, ahoraEnPeru)

                Log.d(TAG, "verificarDisponibilidadQuiz: Días de diferencia = $diasPasados")

                val puedeContinuar = diasPasados >= 7

                if (puedeContinuar) {
                    Log.d(TAG, "verificarDisponibilidadQuiz: Han pasado 7 días o más. PERMITIDO.")
                } else {
                    Log.d(TAG, "verificarDisponibilidadQuiz: NO han pasado 7 días. BLOQUEADO.")
                }

                return@map puedeContinuar
            } catch (e: DateTimeParseException) {
                Log.e(TAG, "verificarDisponibilidadQuiz: ERROR al parsear la fecha. BLOQUEADO.", e)
                return@map false
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

    suspend fun obtenerCursosSeleccionadosActuales(usuarioId: Int): Result<List<CursoSeleccionado>> = withContext(Dispatchers.IO) {
        val sql = "SELECT CS_titulo, CS_descripcion FROM CursosSeleccionados WHERE CS_usuarioID = ?"
        return@withContext DatabaseManager.executeSelectList(sql, listOf(usuarioId)) { rs ->
            CursoSeleccionado(
                titulo = rs.getString("CS_titulo"),
                descripcion = rs.getString("CS_descripcion")
            )
        }
    }
    suspend fun obtenerUltimoTemario(usuarioId: Int): Result<String?> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT TOP (1) C_temario
            FROM dbo.Cuestionario
            WHERE C_usuarioID = ?
            ORDER BY C_fecha DESC;
        """.trimIndent()
        return@withContext DatabaseManager.executeSelectOne(sql, listOf(usuarioId)) { rs ->
            rs.getString("C_temario")
        }
    }

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

    suspend fun hayCursosNuevosParaEvaluar(usuarioId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Usamos async para lanzar ambas consultas en paralelo
            val cursosActualesDeferred = async { obtenerCursosSeleccionadosActuales(usuarioId) }
            val ultimoTemarioDeferred = async { obtenerUltimoTemario(usuarioId) }

            val cursosActualesResult = cursosActualesDeferred.await()
            val ultimoTemarioResult = ultimoTemarioDeferred.await()

            // Si alguna de las consultas falla, propagamos el error
            cursosActualesResult.onFailure { return@withContext Result.failure(it) }
            ultimoTemarioResult.onFailure { return@withContext Result.failure(it) }

            val cursosActuales = cursosActualesResult.getOrThrow()
            val ultimoTemarioString = ultimoTemarioResult.getOrNull()

            if (ultimoTemarioString == null) {
                // Es el primer quiz, por lo tanto, hay "cursos nuevos" si la lista no está vacía
                return@withContext Result.success(cursosActuales.isNotEmpty())
            }

            val temasAntiguos = ultimoTemarioString.split(" / ").map { it.trim() }.toSet()
            val cursosNuevos = cursosActuales.filter { !temasAntiguos.contains(it.titulo) }

            Log.d(TAG, "Cursos nuevos encontrados: ${cursosNuevos.size}")
            return@withContext Result.success(cursosNuevos.isNotEmpty())

        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

}