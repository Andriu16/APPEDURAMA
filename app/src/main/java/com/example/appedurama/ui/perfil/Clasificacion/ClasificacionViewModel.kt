package com.example.appedurama.ui.perfil.Clasificacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appedurama.data.model.RankingItem
import com.example.appedurama.data.repository.ClasificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClasificacionUiState(
    val isLoading: Boolean = false,
    val rankingList: List<RankingItem> = emptyList(),
    val miRanking: RankingItem? = null,
    val error: String? = null
)

class ClasificacionViewModel : ViewModel() {
    private val repository = ClasificacionRepository()
    private val _uiState = MutableStateFlow(ClasificacionUiState())
    val uiState = _uiState.asStateFlow()

    fun cargarDatosClasificacion(usuarioId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.obtenerClasificacionConNombres(usuarioId).onSuccess { lista ->
                // Buscamos los datos del usuario actual en la lista devuelta por el SP
                val miItem = lista.find { it.usuarioId == usuarioId }

                _uiState.update {
                    it.copy(isLoading = false, rankingList = lista, miRanking = miItem)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}