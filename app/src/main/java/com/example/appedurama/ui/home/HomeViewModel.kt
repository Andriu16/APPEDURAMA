package com.example.appedurama.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope
import com.example.appedurama.data.model.EncuestaRespuestas
import com.example.appedurama.data.repository.RecomendacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val recomendacion: String? = null,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val recomendacionRepository = RecomendacionRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun enviarEncuesta(respuestas: EncuestaRespuestas) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = recomendacionRepository.obtenerRecomendacionesDeIA(respuestas)

            // 3. Actualizar la UI con el resultado
            result.onSuccess { recomendacionTexto ->
                _uiState.update {
                    it.copy(isLoading = false, recomendacion = recomendacionTexto, error = null)
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al contactar la IA: ${exception.message}")
                }
            }
        }
    }

    // Funciones para resetear el estado una vez mostrado
    fun recomendacionMostrada() {
        _uiState.update { it.copy(recomendacion = null) }
    }

    fun errorMostrado() {
        _uiState.update { it.copy(error = null) }
    }


}