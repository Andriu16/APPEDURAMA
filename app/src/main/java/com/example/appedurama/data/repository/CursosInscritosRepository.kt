package com.example.appedurama.data.repository

import com.example.appedurama.AccesoSql.DatabaseManager
import com.example.appedurama.data.model.CursoInscrito
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CursosInscritosRepository {

    suspend fun obtenerCursos(usuarioId: Int): Result<List<CursoInscrito>> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT CS_id, CS_titulo, CS_descripcion, CS_imagen, CS_plataforma, CS_Fecha
            FROM CursosSeleccionados
            WHERE CS_usuarioID = ?
            ORDER BY CS_Fecha DESC
        """.trimIndent()

        return@withContext DatabaseManager.executeSelectList(sql, listOf(usuarioId)) { rs ->
            CursoInscrito(
                id = rs.getInt("CS_id"),
                titulo = rs.getString("CS_titulo"),
                descripcion = rs.getString("CS_descripcion"),
                imagenUrl = rs.getString("CS_imagen"),
                plataforma = rs.getString("CS_plataforma"),
                fechaInscripcion = rs.getString("CS_Fecha")
            )
        }
    }
}