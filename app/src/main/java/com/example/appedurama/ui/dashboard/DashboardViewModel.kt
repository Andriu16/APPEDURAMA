package com.example.appedurama.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appedurama.data.model.UniversidadRecomendada
import com.example.appedurama.data.repository.UniversidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val universidades: List<UniversidadRecomendada> = emptyList(),
    val error: String? = null
)

class DashboardViewModel : ViewModel() {

    private val repository = UniversidadRepository()
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()
    private var datosYaCargados = false

    fun cargarUniversidades(respuestasUsuario: String?) {
        if (datosYaCargados || _uiState.value.isLoading) return

        if (respuestasUsuario.isNullOrEmpty()) {
            _uiState.update { it.copy(error = "Completa la encuesta en la pantalla de inicio para ver recomendaciones.") }
            return
        }

        datosYaCargados = true
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.obtenerRecomendaciones(respuestasUsuario).onSuccess { listaUniversidades ->
                _uiState.update {
                    it.copy(isLoading = false, universidades = listaUniversidades)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al generar recomendaciones: ${error.message}")
                }
                datosYaCargados = false // Permitir reintentar si falla
            }
        }
    }
}