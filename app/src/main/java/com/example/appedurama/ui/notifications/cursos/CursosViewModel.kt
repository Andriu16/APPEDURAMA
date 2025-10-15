package com.example.appedurama.ui.notifications.cursos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appedurama.data.model.Curso
import com.example.appedurama.data.repository.CursosRepository
import com.example.appedurama.data.repository.CursosSeleccionadosRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Define el estado de la UI para la pantalla de cursos.
data class CursosUiState(
    val isLoading: Boolean = false,
    val cursos: List<Curso> = emptyList(),
    val error: String? = null
)

sealed class MarcarCursoEvent {
    data class Success(val message: String) : MarcarCursoEvent()
    data class Error(val message: String) : MarcarCursoEvent()
}

class CursosViewModel(
    // SavedStateHandle es la forma moderna de recibir argumentos de navegación en un ViewModel.
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository = CursosRepository()
    private val seleccionadosRepository = CursosSeleccionadosRepository()
    private val _uiState = MutableStateFlow(CursosUiState())
    val uiState = _uiState.asStateFlow()

    private val _marcarCursoEvent = MutableSharedFlow<MarcarCursoEvent>()
    val marcarCursoEvent = _marcarCursoEvent.asSharedFlow()

    init {
        // Se ejecuta en cuanto se crea el ViewModel.
        // Obtenemos el término de búsqueda que se pasó desde NotificationsFragment.
        val termino = savedStateHandle.get<String>("terminoBusqueda")
        if (!termino.isNullOrBlank()) {
            // Si hay un término, iniciamos la búsqueda automáticamente.
            buscarCursos(termino)
        }
    }

    /**
     * Inicia la búsqueda de cursos a través del repositorio.
     * Actualiza el estado de la UI para reflejar el proceso de carga y el resultado.
     * @param termino El término a buscar.
     */
    fun buscarCursos(termino: String) {
        viewModelScope.launch {
            // 1. Pone la UI en estado de carga.
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 2. Llama al repositorio para obtener los datos.
            val result = repository.obtenerCursos(termino)

            // 3. Actualiza la UI con el resultado.
            result.onSuccess { cursosObtenidos ->
                _uiState.update {
                    it.copy(isLoading = false, cursos = cursosObtenidos)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al buscar cursos: ${error.message}")
                }
            }
        }
    }

    fun marcarCursoComoSeleccionado(curso: Curso, usuarioId: Int) {
        viewModelScope.launch {
            val result = seleccionadosRepository.marcarCurso(curso, usuarioId)
            result.onSuccess {
                _marcarCursoEvent.emit(MarcarCursoEvent.Success("¡Curso guardado con éxito!"))
            }.onFailure { error ->
                _marcarCursoEvent.emit(MarcarCursoEvent.Error("Error al guardar: ${error.message}"))
            }
        }
    }

    fun getCursoById(cursoId: String): Curso? {
        return uiState.value.cursos.find { it.id == cursoId }
    }
}