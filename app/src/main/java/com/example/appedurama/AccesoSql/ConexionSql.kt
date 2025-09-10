package com.example.appedurama.AccesoSql
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.*
object DatabaseManager {
    private const val TAG = "DatabaseManager"

    private const val DB_NAME = "BD_APPEDURAMA"
    private const val SERVER_REMOTE_IP = "34.121.218.139"
    private const val SERVER_PORT = "1433"
    private const val USER = "sqlserver"
    private const val PASS = "46073651_Jf"
    private val connectionUrl = "jdbc:jtds:sqlserver://${SERVER_REMOTE_IP}:${SERVER_PORT}/${DB_NAME}"

    init {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "jTDS Driver not found", e)
        }
    }

    private suspend fun <T> executeQuery(
        sql: String,
        params: List<Any> = emptyList(),
        isUpdate: Boolean = false,
        mapper: (ResultSet) -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            Log.d(TAG, "Connecting to DB...")
            connection = DriverManager.getConnection(connectionUrl, USER, PASS)
            Log.d(TAG, "Connected. Preparing statement for: $sql")
            preparedStatement = connection.prepareStatement(sql)
            params.forEachIndexed { index, param ->
                preparedStatement.setObject(index + 1, param)
            }
            Log.d(TAG, "Parameters set: $params")

            if (isUpdate) {
                Log.d(TAG, "Executing update...")
                val rowsAffected = preparedStatement.executeUpdate()
                Log.d(TAG, "Rows affected: $rowsAffected")
                @Suppress("UNCHECKED_CAST")
                Result.success(rowsAffected as T)
            } else {
                Log.d(TAG, "Executing query...")
                resultSet = preparedStatement.executeQuery()
                Log.d(TAG, "Query executed. Mapping results...")
                val mappedResult = mapper(resultSet)
                Log.d(TAG, "Mapping complete.")
                Result.success(mappedResult)
            }
        } catch (e: SQLException) {
            Log.e(TAG, "SQL Error for query: $sql", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Generic Error for query: $sql", e)
            Result.failure(e)
        } finally {
            Log.d(TAG, "Closing resources...")
            try { resultSet?.close() } catch (e: SQLException) { Log.e(TAG, "Error closing ResultSet", e) }
            try { preparedStatement?.close() } catch (e: SQLException) { Log.e(TAG, "Error closing PreparedStatement", e) }
            try { connection?.close() } catch (e: SQLException) { Log.e(TAG, "Error closing Connection", e) }
            Log.d(TAG, "Resources closed.")
        }
    }
    suspend fun <T> executeSelectList(
        sql: String,
        params: List<Any> = emptyList(),
        rowMapper: (ResultSet) -> T
    ): Result<List<T>> {
        return executeQuery(sql, params, isUpdate = false) { rs ->
            val list = mutableListOf<T>()
            while (rs.next()) {
                list.add(rowMapper(rs))
            }
            list
        }
    }
    suspend fun <T> executeSelectOne(
        sql: String,
        params: List<Any> = emptyList(),
        rowMapper: (ResultSet) -> T
    ): Result<T?> {
        return executeQuery(sql, params, isUpdate = false) { rs ->
            if (rs.next()) {
                rowMapper(rs)
            } else {
                null
            }
        }
    }
    suspend fun executeUpdateOperation(
        sql: String,
        params: List<Any> = emptyList()
    ): Result<Int> {
        @Suppress("UNCHECKED_CAST")
        return executeQuery(sql, params, isUpdate = true) {
        } as Result<Int>
    }
}