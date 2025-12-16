package com.example.appedurama.data.repository

import com.example.appedurama.AccesoSql.DatabaseManager
import com.example.appedurama.data.Usuario

class UsuarioRepository {


    suspend fun login(correo: String, contrasena: String): Result<Usuario?> {
        val sql = "SELECT U_ID, U_Nombre, U_Apellido, U_Correo, U_Telefono, U_Dni, U_Fecha FROM UsuariosAppPrueba WHERE U_Correo = ? AND U_Contrasena = ?"

        return DatabaseManager.executeSelectOne(sql, listOf(correo, contrasena)) { rs ->

            Usuario(
                id = rs.getInt("U_ID"),
                nombre = rs.getString("U_Nombre"),
                apellido = rs.getString("U_Apellido"),
                correo = rs.getString("U_Correo"),
                telefono = rs.getInt("U_Telefono"),
                dni = rs.getString("U_Dni"),
                fecha = rs.getString("U_Fecha")
            )
        }
    }

    suspend fun checkEmailExists(correo: String): Result<Boolean> {
        val sql = "SELECT COUNT(1) FROM UsuariosAppPrueba WHERE U_Correo = ?"
        val result = DatabaseManager.executeSelectOne(sql, listOf(correo)) { rs ->
            rs.getInt(1) > 0
        }

        return result.map { it ?: false }
    }
    suspend fun register(
        nombres: String,
        apellidos: String,
        dni: String,
        telefono: String,
        correo: String,
        contrasena: String,
        aceptoTerminos: Boolean
    ): Result<Int> {
        val sql = """
            INSERT INTO UsuariosAppPrueba 
            (U_Nombre, U_Apellido, U_Dni, U_Telefono, U_Correo, U_Contrasena, U_AceptoTerminos) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val params = listOf(
            nombres,
            apellidos,
            dni,
            telefono,
            correo,
            contrasena,
            if (aceptoTerminos) "SI" else "NO"
        )
        return DatabaseManager.executeUpdateOperation(sql, params)
    }
}