package com.example.appedurama.ui.perfil.cuestionario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appedurama.data.model.PreguntaQuiz
import com.example.appedurama.data.repository.CuestionarioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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

//    fun generarCuestionario(usuarioId: Int) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true, error = null) }
//
//            // --- LÓGICA MODIFICADA ---
//            repository.obtenerTemarioParaQuiz(usuarioId).onSuccess { temarioData ->
//                // Guardamos el objeto completo en el estado
//                _uiState.update { it.copy(temarioData = temarioData) }
//
//                repository.generarCuestionarioConIA(temarioData.paraIA).onSuccess { preguntas ->
//                    _uiState.update {
//                        it.copy(isLoading = false, cuestionario = preguntas)
//                    }
//                    iniciarTimer()
//                }.onFailure { error ->
//                    _uiState.update { it.copy(isLoading = false, error = "Error generando preguntas: ${error.message}") }
//                }
//            }.onFailure { error ->
//                _uiState.update { it.copy(isLoading = false, error = "Error obteniendo temario: ${error.message}") }
//            }
//        }
//    }

    fun generarCuestionario(usuarioId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Obtenemos los datos necesarios en paralelo.
            //    Sabemos que el usuario tiene cursos y que hay cursos nuevos,
            //    así que estas llamadas son para obtener el contenido.
            val cursosActualesDeferred = async { repository.obtenerCursosSeleccionadosActuales(usuarioId) }
            val ultimoTemarioDeferred = async { repository.obtenerUltimoTemario(usuarioId) }

            val cursosActualesResult = cursosActualesDeferred.await()
            val ultimoTemarioResult = ultimoTemarioDeferred.await()

            // 2. Manejamos posibles errores de base de datos que pudieran ocurrir.
            if (cursosActualesResult.isFailure || ultimoTemarioResult.isFailure) {
                val errorMsg = cursosActualesResult.exceptionOrNull()?.message ?: ultimoTemarioResult.exceptionOrNull()?.message
                _uiState.update { it.copy(isLoading = false, error = "Error al obtener datos del temario: $errorMsg") }
                return@launch
            }

            val cursosActuales = cursosActualesResult.getOrThrow()
            val ultimoTemarioString = ultimoTemarioResult.getOrNull()

            // 3. Lógica de filtrado: encontrar los cursos que son nuevos (igual que antes).
            val cursosNuevos = if (ultimoTemarioString == null) {
                cursosActuales
            } else {
                val temasAntiguos = ultimoTemarioString.split(" / ").map { it.trim() }.toSet()
                cursosActuales.filter { !temasAntiguos.contains(it.titulo) }
            }

            // Ya no se necesita la comprobación 'if (cursosNuevos.isEmpty())',
            // porque se hizo en la pantalla anterior.

            // 4. Construimos el TemarioData SOLO con los cursos nuevos.
            val temarioIA = cursosNuevos.joinToString(separator = "\n\n") { "Título: ${it.titulo}\nDescripción: ${it.descripcion ?: ""}" }
            val temarioDB = cursosNuevos.joinToString(separator = " / ") { it.titulo }
            val nuevoTemarioData = TemarioData(paraIA = temarioIA, paraBaseDeDatos = temarioDB)

            _uiState.update { it.copy(temarioData = nuevoTemarioData) }

            // 5. Generamos el cuestionario con Gemini usando solo los temas nuevos.
            repository.generarCuestionarioConIA(nuevoTemarioData.paraIA).onSuccess { preguntas ->
                _uiState.update { it.copy(isLoading = false, cuestionario = preguntas) }
                iniciarTimer()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = "Error al generar preguntas con la IA: ${error.message}") }
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