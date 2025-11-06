package com.example.appedurama.ui.cursos_inscritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appedurama.data.model.CursoInscrito
import com.example.appedurama.data.repository.CursosInscritosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CursosInscritosUiState(
    val isLoading: Boolean = false,
    val cursos: List<CursoInscrito> = emptyList(),
    val error: String? = null
)

class CursosInscritosViewModel : ViewModel() {

    private val repository = CursosInscritosRepository()
    private val _uiState = MutableStateFlow(CursosInscritosUiState())
    val uiState = _uiState.asStateFlow()

    fun cargarCursos(usuarioId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.obtenerCursos(usuarioId).onSuccess { cursosObtenidos ->
                _uiState.update {
                    it.copy(isLoading = false, cursos = cursosObtenidos, error = null)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al cargar cursos: ${error.message}")
                }
            }
        }
    }
}