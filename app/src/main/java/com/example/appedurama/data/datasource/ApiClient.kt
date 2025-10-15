package com.example.appedurama.data.datasource

import com.example.appedurama.data.model.ApiResponseCursos
import com.example.appedurama.data.model.SearchRequestBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit



// ---- OBJETO API CLIENT MODIFICADO ----
object ApiClient {
    private const val BASE_URL = "https://cursowebs.myvnc.com/"

    // 1. Creamos un interceptor para ver los logs de las llamadas. ¡Muy útil para depurar!
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Muestra todo: headers, body, etc.
    }

    // 2. Creamos un cliente OkHttp personalizado con timeouts largos.
    private val okHttpClient = OkHttpClient.Builder()
        // Timeout para establecer la conexión inicial.
        .connectTimeout(30, TimeUnit.SECONDS)
        // Timeout para leer la respuesta. ¡Este es el más importante para ti!
        // Lo ponemos en 10 minutos para estar seguros.
        .readTimeout(10, TimeUnit.MINUTES)
        // Timeout para escribir la petición.
        .writeTimeout(10, TimeUnit.MINUTES)
        // Añadimos el interceptor de logs.
        .addInterceptor(loggingInterceptor)
        .build()

    // 3. Creamos la instancia de Retrofit usando nuestro cliente personalizado.
    val instance: CursosApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // ¡Línea clave! Le decimos a Retrofit que use nuestro cliente OkHttp paciente.
            .client(okHttpClient)
            .build()
        retrofit.create(CursosApiService::class.java)
    }
}