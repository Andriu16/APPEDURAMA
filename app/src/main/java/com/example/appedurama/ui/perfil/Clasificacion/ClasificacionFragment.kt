package com.example.appedurama.ui.perfil.Clasificacion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.appedurama.databinding.FragmentClasificacionBinding
import com.example.appedurama.ui.SharedViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class ClasificacionFragment : Fragment() {

    private var _binding: FragmentClasificacionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClasificacionViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClasificacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener ID del usuario logueado
        val usuarioActual = sharedViewModel.usuarioActual.value
        if (usuarioActual != null) {
            viewModel.cargarDatosClasificacion(usuarioActual.id)
        }

        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.error != null) {
                        Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                    }

                    // --- 1. LLENAR EL PODIO (TOP 3) ---
                    val lista = state.rankingList

                    // Primer Puesto (Posicion == 1)
                    val primero = lista.find { it.posicion == 1L }
                    if (primero != null) {
                        binding.tvNombre1ro.text = primero.nombreCompleto
                        binding.tvPuntos1ro.text = primero.puntajeTotal.toString()
                        // binding.imgAvatar1ro.setImageResource(...) // Si tuvieras avatar real
                    } else {
                        binding.tvNombre1ro.text = "---"
                        binding.tvPuntos1ro.text = "0"
                    }

                    // Segundo Puesto (Posicion == 2)
                    val segundo = lista.find { it.posicion == 2L }
                    if (segundo != null) {
                        binding.tvNombre2do.text = segundo.nombreCompleto
                        binding.tvPuntos2do.text = segundo.puntajeTotal.toString()
                    } else {
                        binding.tvNombre2do.text = "---"
                        binding.tvPuntos2do.text = "0"
                    }

                    // Tercer Puesto (Posicion == 3)
                    val tercero = lista.find { it.posicion == 3L }
                    if (tercero != null) {
                        binding.tvNombre3ro.text = tercero.nombreCompleto
                        binding.tvPuntos3ro.text = tercero.puntajeTotal.toString()
                    } else {
                        binding.tvNombre3ro.text = "---"
                        binding.tvPuntos3ro.text = "0"
                    }

                    // --- 2. LLENAR DATOS DEL USUARIO LOGUEADO ---
                    state.miRanking?.let { miData ->
                        // Fila de "Tu Posición"
                        binding.tvPosicionUsuario.text = "#${miData.posicion}"
                        binding.tvNombreUsuario.text = miData.nombreCompleto // O usar sharedViewModel.usuarioActual.value?.nombre
                        binding.tvPuntosUsuario.text = "${miData.puntajeTotal} puntos"

                        // Barra de progreso (basada en el puntaje total, asumiendo max 500 o dinámico)
                        // Para que se vea bonito, si el 1ro tiene 100, usamos ese como max
                        val maxPuntaje = primero?.puntajeTotal ?: 100
                        binding.progressUsuario.max = if (maxPuntaje > 0) maxPuntaje else 100
                        binding.progressUsuario.progress = miData.puntajeTotal

                        // Estadísticas inferiores
                        // Formato a 1 decimal
                        binding.tvPromedioUsuario.text = String.format(Locale.US, "%.1f", miData.puntajePromedio)
                        binding.tvExamenesCompletados.text = miData.examenesCompletados.toString()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}