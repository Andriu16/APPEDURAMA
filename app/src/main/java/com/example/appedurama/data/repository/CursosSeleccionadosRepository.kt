package com.example.appedurama.data.repository

import android.util.Log
import com.example.appedurama.AccesoSql.DatabaseManager
import com.example.appedurama.data.model.Curso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CursosSeleccionadosRepository {

    suspend fun marcarCurso(curso: Curso, usuarioId: Int): Result<Int> = withContext(Dispatchers.IO) {
        val sql = """
            INSERT INTO CursosSeleccionados (CS_titulo, CS_descripcion, CS_imagen, CS_plataforma, CS_usuarioID)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        val descripcionCorta = curso.descripcion?.take(490) ?: "Sin descripción"

        val params = listOf(
            curso.nombre,
            descripcionCorta,
            curso.imagen,
            curso.plataforma,
            usuarioId
        )

        Log.d("CursosSeleccionadosRepo", "Intentando marcar curso: ${curso.nombre} para usuario ID: $usuarioId")


        val databaseResult = DatabaseManager.executeUpdateOperation(sql, params)


        return@withContext databaseResult.fold(
            onSuccess = { filasAfectadas ->

                if (filasAfectadas > 0) {
                    Log.i("CursosSeleccionadosRepo", "¡Curso marcado con éxito! Filas afectadas: $filasAfectadas")
                    Result.success(filasAfectadas)
                } else {

                    Log.e("CursosSeleccionadosRepo", "Error: El guardado no afectó ninguna fila.")
                    Result.failure(Exception("No se pudo guardar el curso en la base de datos."))
                }
            },
            onFailure = { exception ->

                Log.e("CursosSeleccionadosRepo", "Excepción al marcar el curso desde DatabaseManager", exception)
                Result.failure(exception)
            }
        )
    }
}