package com.example.appedurama.ui.notifications.cursos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appedurama.R
import com.example.appedurama.databinding.FragmentCursosBinding
import com.example.appedurama.ui.notifications.cursos.adapter.CursosAdapter
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels

class CursosFragment : Fragment(R.layout.fragment_cursos) { // Manera moderna de inflar
    private var _binding: FragmentCursosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CursosViewModel by viewModels()
    private lateinit var cursosAdapter: CursosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCursosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()
        setupListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {

        cursosAdapter = CursosAdapter(childFragmentManager)
        binding.recyclerViewItems.apply {
            adapter = cursosAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.btnBuscar.setOnClickListener {
            val termino = binding.editTextDescripcion.text.toString().trim()
            if (termino.isNotBlank()) {
                viewModel.buscarCursos(termino)
            } else {
                Toast.makeText(context, "Ingresa un término de búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Gestionar la visibilidad de los elementos de carga
                    val isLoading = state.isLoading
                    binding.progressBarCursos.isVisible = isLoading
                    binding.textViewLoadingMessage.isVisible = isLoading

                    // Deshabilitar la búsqueda mientras está cargando
                    binding.btnBuscar.isEnabled = !isLoading
                    binding.editTextDescripcion.isEnabled = !isLoading

                    // Actualizar la lista de cursos
                    cursosAdapter.submitList(state.cursos)
                    if (!isLoading) {
                        binding.textViewCantidad.text = "${state.cursos.size} cursos encontrados"
                    }

                    // Mostrar error si existe
                    state.error?.let { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar fugas de memoria
    }
}