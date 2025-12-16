package com.example.appedurama.data.model

import com.google.gson.annotations.SerializedName

data class QuizResponse(
    @SerializedName("cuestionario")
    val cuestionario: List<PreguntaQuiz>
)

data class PreguntaQuiz(
    @SerializedName("pregunta")
    val pregunta: String,

    @SerializedName("opciones")
    val opciones: List<String>,

    @SerializedName("respuesta_correcta")
    val respuestaCorrecta: Int
)