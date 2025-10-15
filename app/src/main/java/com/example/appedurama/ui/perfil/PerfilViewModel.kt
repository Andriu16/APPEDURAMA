// Archivo: ui/perfil/PerfilViewModel.kt
package com.example.appedurama.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appedurama.data.Usuario
import com.example.appedurama.data.repository.CuestionarioRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed class PerfilEvent {
    object NavigateToQuiz : PerfilEvent()
    data class ShowToast(val message: String) : PerfilEvent()
}
// Estado para la UI del perfil
data class PerfilUiState(
    val isLoading: Boolean = false,
    val nombreCompleto: String = "",
    val correo: String = "",
    val dni: String = "",
    val telefono: String = "",
    val miembroDesde: String = ""
    // Puedes añadir más campos como la fecha aquí
)

class PerfilViewModel : ViewModel() {
    private val cuestionarioRepository = CuestionarioRepository()

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PerfilEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    /**
     * Procesa el objeto Usuario y actualiza el estado de la UI
     */
    fun cargarDatosUsuario(usuario: Usuario?) {
        if (usuario == null) {
            // Manejar caso en que no haya usuario (ej. error o deslogueado)
            _uiState.update { PerfilUiState(nombreCompleto = "Usuario no encontrado") }
            return
        }

        _uiState.update {
            it.copy(
                nombreCompleto = "${usuario.nombre} ${usuario.apellido}",
                correo = usuario.correo ?: "No disponible",
                dni = usuario.dni ?: "No disponible",
                telefono = usuario.telefono?.toString() ?: "No disponible",
                miembroDesde = formatarFecha(usuario.fecha)
            )
        }
    }

    fun onQuizCardClicked(usuario: Usuario?) {
        if (usuario == null) {
            viewModelScope.launch {
                _eventFlow.emit(PerfilEvent.ShowToast("Error: No se ha podido identificar al usuario."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. PRIMERA VERIFICACIÓN: ¿Tiene cursos seleccionados? (TU LÓGICA)
            cuestionarioRepository.tieneCursosSeleccionados(usuario.id).onSuccess { tieneCursos ->
                if (tieneCursos) {
                    // 2. SEGUNDA VERIFICACIÓN: ¿Han pasado 7 días?
                    cuestionarioRepository.verificarDisponibilidadQuiz(usuario.id).onSuccess { puedePorTiempo ->
                        if (puedePorTiempo) {
                            // Si ambas verificaciones pasan, navegamos.
                            _eventFlow.emit(PerfilEvent.NavigateToQuiz)
                        } else {
                            // Si tiene cursos pero no ha pasado el tiempo, mostramos mensaje de espera.
                            _eventFlow.emit(PerfilEvent.ShowToast("Usted ya completó la prueba semanal."))
                        }
                    }.onFailure { error ->
                        _eventFlow.emit(PerfilEvent.ShowToast("Error al verificar la fecha: ${error.message}"))
                    }
                } else {
                    // Si no tiene cursos, se detiene aquí y se muestra el mensaje.
                    _eventFlow.emit(PerfilEvent.ShowToast("Debe marcar al menos un curso para generar un cuestionario."))
                }
            }.onFailure { error ->
                _eventFlow.emit(PerfilEvent.ShowToast("Error al verificar cursos: ${error.message}"))
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }


    private fun formatarFecha(fechaSql: String?): String {
        // Si la fecha es nula o está vacía, devuelve un texto por defecto.
        if (fechaSql.isNullOrBlank()) {
            return "Fecha no disponible"
        }

        return try {
            // Toma solo la parte de la fecha (los primeros 10 caracteres: "YYYY-MM-DD")
            val datePart = fechaSql.substring(0, 10)

            // Define el formato de entrada (cómo viene la fecha)
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            // Define el formato de salida (cómo la queremos mostrar)
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

            // Convierte el texto en un objeto de fecha
            val date = LocalDate.parse(datePart, inputFormatter)

            // Formatea el objeto de fecha al formato de salida deseado
            date.format(outputFormatter)
        } catch (e: Exception) {
            // En caso de cualquier error de formato, devuelve un texto de error.
            "Fecha inválida"
        }
    }
}