package com.example.appedurama.ui.cursos_inscritos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appedurama.databinding.FragmentCursosInscritosBinding
import com.example.appedurama.ui.SharedViewModel
import kotlinx.coroutines.launch

class CursosInscritosFragment : Fragment() {

    private var _binding: FragmentCursosInscritosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CursosInscritosViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var cursosAdapter: CursosInscritosAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCursosInscritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeUiState()

        val usuario = sharedViewModel.usuarioActual.value
        if (usuario != null) {
            viewModel.cargarCursos(usuario.id)
        } else {
            Toast.makeText(context, "Error: No se pudo identificar al usuario.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        cursosAdapter = CursosInscritosAdapter()
        binding.rvCursos.apply {
            adapter = cursosAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading

                    if (state.error != null) {
                        Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                    }

                    cursosAdapter.submitList(state.cursos)

                    // Lógica para mostrar el estado vacío o la lista
                    val hayCursos = state.cursos.isNotEmpty()
                    binding.emptyState.isVisible = !state.isLoading && !hayCursos
                    binding.rvCursos.isVisible = !state.isLoading && hayCursos

                    // Actualizar contadores del header
                    if (!state.isLoading) {
                        binding.tvTotalCursos.text = state.cursos.size.toString()
                        // Lógica simple para "recientes", puedes hacerla más compleja si quieres
                        val recientes = state.cursos.take(3).size
                        binding.tvCursosRecientes.text = recientes.toString()
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