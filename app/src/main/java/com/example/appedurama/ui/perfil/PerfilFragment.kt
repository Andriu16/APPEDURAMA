// Archivo: ui/perfil/PerfilFragment.kt
package com.example.appedurama.ui.perfil

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
import com.example.appedurama.databinding.FragmentDatosUsuarioBinding // <-- IMPORTANTE
import com.example.appedurama.ui.SharedViewModel
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.example.appedurama.R

class PerfilFragment : Fragment() {

    // ViewModel propio del fragmento
    private val viewModel: PerfilViewModel by viewModels()
    // ViewModel compartido con la Activity
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // ViewBinding
    private var _binding: FragmentDatosUsuarioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatosUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeSharedViewModel()
        observePerfilUiState()
        observeNavigationEvents()
    }
    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            // 1. Limpiamos los datos del usuario en el ViewModel compartido.
            sharedViewModel.clearUsuario()

            // 2. Usamos la acción global para navegar y limpiar el historial.
            findNavController().navigate(R.id.action_global_logout_to_loginFragment)
        }
        binding.cardQuiz.setOnClickListener {
            val usuarioActual = sharedViewModel.usuarioActual.value
            viewModel.onQuizCardClicked(usuarioActual)
        }
    }
    private fun observeNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventFlow.collect { event ->
                    when (event) {
                        is PerfilEvent.NavigateToQuiz -> {
                            findNavController().navigate(R.id.action_perfil_to_cuestionario)
                        }
                        is PerfilEvent.ShowToast -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun observeSharedViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Cada vez que el usuario en SharedViewModel cambie...
                sharedViewModel.usuarioActual.collect { usuario ->
                    // ...le decimos a nuestro ViewModel local que cargue esos datos.
                    viewModel.cargarDatosUsuario(usuario)
                }
            }
        }
    }

    private fun observePerfilUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Cada vez que el estado de la UI del perfil cambie...
                viewModel.uiState.collect { state ->
                    binding.cardQuiz.isEnabled = !state.isLoading
                    // ...actualizamos las vistas.
                    binding.tvNombreYApellido.text = state.nombreCompleto
                    binding.tvCorreo.text = state.correo
                    binding.tvDni.text = state.dni
                    binding.tvTelefono.text = state.telefono
                    binding.tvFecha.text = state.miembroDesde
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpiar la referencia al binding
    }
}