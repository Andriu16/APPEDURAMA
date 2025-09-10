package com.example.appedurama.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appedurama.databinding.FragmentNotificationsBinding
import com.example.appedurama.ui.notifications.adapter.RutaAprendizajeAdapter
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels
import com.example.appedurama.ui.SharedViewModel
class NotificationsFragment : Fragment() {

    // ViewModel para esta pantalla
    private val viewModel: NotificationsViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by activityViewModels()
    // ViewBinding para acceder a las vistas de forma segura
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    // El adapter para nuestro RecyclerView principal
    private lateinit var rutaAdapter: RutaAprendizajeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeUiState()
        observeSharedViewModel()
    }

    private fun observeSharedViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.textoRecomendaciones.collect { texto ->
                    // Cuando el texto no sea nulo, se lo pasamos a nuestro ViewModel local
                    // para que inicie la carga de datos.
                    if (texto != null) {
                        viewModel.cargarRutasDeAprendizaje(texto)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {

        rutaAdapter = RutaAprendizajeAdapter(
            onToggleCursos = { ruta -> viewModel.toggleCursos(ruta) },
            onToggleHabilidades = { ruta -> viewModel.toggleHabilidades(ruta) },
            onToggleOportunidades = { ruta -> viewModel.toggleOportunidades(ruta) }
        )
        binding.recyclerViewLearningPaths.apply {
            adapter = rutaAdapter
            layoutManager = LinearLayoutManager(context)

            setHasFixedSize(true)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    binding.progressBar.isVisible = state.isLoading


                    binding.textViewStatus.isVisible = state.error != null
                    if (state.error != null) {
                        binding.textViewStatus.text = "Error: ${state.error}"
                    }


                    rutaAdapter.submitList(state.rutas)
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos la referencia al binding para evitar fugas de memoria
        _binding = null
    }
}