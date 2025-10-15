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
}