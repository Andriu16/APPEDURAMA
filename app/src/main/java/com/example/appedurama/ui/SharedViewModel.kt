package com.example.appedurama.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Este ViewModel será compartido por toda la Activity
class SharedViewModel : ViewModel() {
    private val _textoRecomendaciones = MutableStateFlow<String?>(null)
    val textoRecomendaciones = _textoRecomendaciones.asStateFlow()

    fun setTextoRecomendaciones(texto: String) {
        _textoRecomendaciones.value = texto
    }
}