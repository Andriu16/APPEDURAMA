package com.example.appedurama.ui.notifications


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appedurama.data.model.RutaAprendizaje
import com.example.appedurama.data.repository.RutaAprendizajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para esta pantalla
data class RutasUiState(
    val isLoading: Boolean = false,
    val rutas: List<RutaAprendizaje> = emptyList(),
    val error: String? = null
)

class NotificationsViewModel : ViewModel() {

    private val repository = RutaAprendizajeRepository()
    private val _uiState = MutableStateFlow(RutasUiState())
    val uiState = _uiState.asStateFlow()
    private var datosYaCargados = false



    fun cargarRutasDeAprendizaje(areasDeInteres: String?) {
        if (datosYaCargados || _uiState.value.isLoading) return


        if (areasDeInteres.isNullOrEmpty()) {
            _uiState.update { it.copy(error = "Primero completa la encuesta en la pantalla de Home.") }
            return
        }

        datosYaCargados = true

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.obtenerRutas(areasDeInteres).onSuccess { rutasObtenidas ->
                _uiState.update { it.copy(isLoading = false, rutas = rutasObtenidas, error = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = "Error al generar rutas: ${error.message}") }
                datosYaCargados = false
            }
        }
    }


    fun toggleCursos(ruta: RutaAprendizaje) {
        _uiState.update { currentState ->
            val updatedRutas = currentState.rutas.map {
                if (it.titulo == ruta.titulo) {
                    it.copy(cursosExpandido = !it.cursosExpandido)
                } else {
                    it
                }
            }
            currentState.copy(rutas = updatedRutas)
        }
    }

    fun toggleHabilidades(ruta: RutaAprendizaje) {
        _uiState.update { currentState ->
            val updatedRutas = currentState.rutas.map {
                if (it.titulo == ruta.titulo) {
                    it.copy(habilidadesExpandido = !it.habilidadesExpandido)
                } else {
                    it
                }
            }
            currentState.copy(rutas = updatedRutas)
        }
    }

    fun toggleOportunidades(ruta: RutaAprendizaje) {
        _uiState.update { currentState ->
            val updatedRutas = currentState.rutas.map {
                if (it.titulo == ruta.titulo) {
                    it.copy(oportunidadesExpandido = !it.oportunidadesExpandido)
                } else {
                    it
                }
            }
            currentState.copy(rutas = updatedRutas)
        }
    }
}