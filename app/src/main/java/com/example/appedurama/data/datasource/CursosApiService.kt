package com.example.appedurama.data.datasource

import com.example.appedurama.data.model.ApiResponseCursos
import com.example.appedurama.data.model.SearchRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CursosApiService {
    @POST("search")
    suspend fun buscarCursos(
        @Body body: SearchRequestBody
    ): Response<ApiResponseCursos>
}