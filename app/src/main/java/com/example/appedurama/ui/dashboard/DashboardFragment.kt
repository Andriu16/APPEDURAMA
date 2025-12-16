package com.example.appedurama.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appedurama.databinding.FragmentDashboardBinding
import com.example.appedurama.ui.SharedViewModel
import com.example.appedurama.ui.dashboard.UniversidadAdapter
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var universidadAdapter: UniversidadAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeUiState()
        observeSharedViewModel()
    }

    private fun setupRecyclerView() {
        universidadAdapter = UniversidadAdapter()
        binding.recyclerViewUniversidades.apply {
            adapter = universidadAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeSharedViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.textoRecomendaciones.collect { texto ->

                    if (texto != null) {
                        viewModel.cargarUniversidades(texto)
                    } else {
                        // Opcional: Mostrar un mensaje si aún no se ha completado la encuesta
                        binding.textViewStatus.text = "Completa la encuesta en la pantalla de inicio para ver tus recomendaciones."
                        binding.textViewStatus.isVisible = true
                    }
                }
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    binding.textViewStatus.isVisible = state.error != null
                    binding.recyclerViewUniversidades.isVisible = state.error == null && !state.isLoading

                    if (state.error != null) {
                        binding.textViewStatus.text = state.error
                    }

                    universidadAdapter.submitList(state.universidades)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}