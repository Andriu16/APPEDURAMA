package com.example.appedurama.data.repository

import com.example.appedurama.AccesoSql.DatabaseManager
import com.example.appedurama.data.model.RankingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClasificacionRepository {

    suspend fun obtenerClasificacionConNombres(usuarioId: Int): Result<List<RankingItem>> = withContext(Dispatchers.IO) {
        val sqlRanking = "EXEC dbo.PuntajesTotales ?"

        val resultadoRanking = DatabaseManager.executeSelectList(sqlRanking, listOf(usuarioId)) { rs ->
            RankingItem(
                posicion = rs.getLong("Posicion"),
                puntajeTotal = rs.getInt("PuntajeTotal"),
                puntajePromedio = rs.getDouble("PuntajePromedio"),
                examenesCompletados = rs.getInt("ExamenesCompletados"),
                usuarioId = rs.getInt("UsuarioID")
            )
        }

        if (resultadoRanking.isFailure) return@withContext resultadoRanking

        val listaRanking = resultadoRanking.getOrThrow()

        val listaConNombres = listaRanking.map { item ->
            val sqlNombre = "SELECT U_Nombre, U_Apellido FROM UsuariosAppPrueba WHERE U_ID = ?"

            val nombreResult = DatabaseManager.executeSelectOne(sqlNombre, listOf(item.usuarioId)) { rs ->
                val nombre = rs.getString("U_Nombre") ?: "Usuario"
                val apellido = rs.getString("U_Apellido") ?: ""

                if (apellido.isNotEmpty()) {
                    "$nombre ${apellido.first()}."
                } else {
                    nombre
                }
            }

            item.nombreCompleto = nombreResult.getOrDefault("Usuario ${item.usuarioId}").toString()
            item
        }

        return@withContext Result.success(listaConNombres)
    }
}