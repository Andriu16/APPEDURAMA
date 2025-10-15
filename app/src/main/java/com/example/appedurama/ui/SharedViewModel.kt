package com.example.appedurama.ui

import androidx.lifecycle.ViewModel
import com.example.appedurama.data.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Este ViewModel será compartido por toda la Activity
class SharedViewModel : ViewModel() {
    private val _textoRecomendaciones = MutableStateFlow<String?>(null)
    val textoRecomendaciones = _textoRecomendaciones.asStateFlow()

    fun setTextoRecomendaciones(texto: String) {
        _textoRecomendaciones.value = texto
    }


    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual = _usuarioActual.asStateFlow()

    /**
     * Guarda los datos del usuario cuando inicia sesión.
     */
    fun setUsuario(usuario: Usuario) {
        _usuarioActual.update { usuario }
    }

    /**
     * Limpia los datos del usuario al cerrar sesión.
     */
    fun clearUsuario() {
        _usuarioActual.update { null }
    }
}