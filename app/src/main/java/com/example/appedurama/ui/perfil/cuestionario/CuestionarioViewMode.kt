package com.example.appedurama.ui.perfil.cuestionario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appedurama.data.model.PreguntaQuiz
import com.example.appedurama.data.repository.CuestionarioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TemarioData(
    val paraIA: String,
    val paraBaseDeDatos: String
)

data class ResultadoQuiz(val puntaje: Int)

data class CuestionarioUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val temario: String = "",
//    val cuestionario: List<PreguntaQuiz> = emptyList(),
    val respuestasUsuario: Map<Int, Int> = emptyMap(), // índice de pregunta -> opción marcada (1-4)
    val tiempoTranscurridoSegundos: Int = 0,
    val resultadoFinal: ResultadoQuiz? = null,
    val temarioData: TemarioData? = null,
    val cuestionario: List<PreguntaQuiz> = emptyList(),
    val guardadoExitoso: Boolean = false
)

class CuestionarioViewModel : ViewModel() {

    private val repository = CuestionarioRepository()
    private val _uiState = MutableStateFlow(CuestionarioUiState())
    val uiState = _uiState.asStateFlow()


    private var timerJob: Job? = null

    fun generarCuestionario(usuarioId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // --- LÓGICA MODIFICADA ---
            repository.obtenerTemarioParaQuiz(usuarioId).onSuccess { temarioData ->
                // Guardamos el objeto completo en el estado
                _uiState.update { it.copy(temarioData = temarioData) }

                repository.generarCuestionarioConIA(temarioData.paraIA).onSuccess { preguntas ->
                    _uiState.update {
                        it.copy(isLoading = false, cuestionario = preguntas)
                    }
                    iniciarTimer()
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = "Error generando preguntas: ${error.message}") }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = "Error obteniendo temario: ${error.message}") }
            }
        }
    }

    fun responderPregunta(preguntaIndex: Int, opcionSeleccionada: Int) {
        val nuevasRespuestas = _uiState.value.respuestasUsuario.toMutableMap()
        nuevasRespuestas[preguntaIndex] = opcionSeleccionada
        _uiState.update { it.copy(respuestasUsuario = nuevasRespuestas) }
    }

    fun finalizarQuiz(usuarioId: Int) {

        if (_uiState.value.resultadoFinal != null) return
        detenerTimer()
        val state = _uiState.value
        if (state.temarioData == null) {
            _uiState.update { it.copy(error = "Error: no se pudo encontrar el temario para guardar.") }
            return
        }

        var puntaje = 0
        state.cuestionario.forEachIndexed { index, pregunta ->
            if (state.respuestasUsuario[index] == pregunta.respuestaCorrecta) {
                puntaje++
            }
        }
        _uiState.update { it.copy(resultadoFinal = ResultadoQuiz(puntaje)) }

        // Guardar en la base de datos
        viewModelScope.launch {
            repository.guardarResultadoQuiz(
                usuarioId = usuarioId,
                temario = state.temarioData.paraBaseDeDatos,
                tiempoRespuestaSegundos = state.tiempoTranscurridoSegundos,
                preguntas = state.cuestionario,
                respuestasUsuario = state.respuestasUsuario,
                puntaje = puntaje
            ).onSuccess {
                _uiState.update { it.copy(guardadoExitoso = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = "Error al guardar el resultado: ${error.message}") }
            }
        }
    }

    private fun iniciarTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(tiempoTranscurridoSegundos = it.tiempoTranscurridoSegundos + 1) }
            }
        }
    }
    fun onResultadoMostrado() {
        _uiState.update { it.copy(resultadoFinal = null) }
    }

    private fun detenerTimer() {
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        detenerTimer()
    }
}