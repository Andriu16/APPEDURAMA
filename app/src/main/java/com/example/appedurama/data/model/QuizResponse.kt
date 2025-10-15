package com.example.appedurama.data.model

import com.google.gson.annotations.SerializedName

// Modelo para la respuesta JSON de Gemini
data class QuizResponse(
    @SerializedName("cuestionario")
    val cuestionario: List<PreguntaQuiz>
)

// Modelo para una única pregunta del cuestionario
data class PreguntaQuiz(
    @SerializedName("pregunta")
    val pregunta: String,

    @SerializedName("opciones")
    val opciones: List<String>, // Siempre debe tener 4 opciones

    @SerializedName("respuesta_correcta")
    val respuestaCorrecta: Int // Índice de la respuesta correcta (1 a 4)
)