package com.example.appedurama.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.findNavController
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
//        (activity as? AppCompatActivity)?.supportActionBar?.hide()

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
        // Inicializamos el adapter pasándole todas las acciones (lambdas) que puede realizar.
        rutaAdapter = RutaAprendizajeAdapter(
            // Acción para cuando se pulsa el botón de Cursos
            onToggleCursos = { ruta -> viewModel.toggleCursos(ruta) },

            // Acción para cuando se pulsa el botón de Habilidades
            onToggleHabilidades = { ruta -> viewModel.toggleHabilidades(ruta) },

            // Acción para cuando se pulsa el botón de Oportunidades
            onToggleOportunidades = { ruta -> viewModel.toggleOportunidades(ruta) },

            // --- NUEVA ACCIÓN PARA LA NAVEGACIÓN ---
            // Acción para cuando se hace clic en CUALQUIER PARTE de la tarjeta (item).
            onItemClicked = { ruta ->
                // --- AQUÍ OCURRE LA MAGIA ---

                // 1. Tomamos el título original.
                val tituloOriginal = ruta.titulo

                // 2. Definimos las frases que queremos eliminar.
                val prefijosAEliminar = listOf("Ruta hacia la ", "Ruta hacia el ", "Camino del ", "Especialización en ")

                // 3. Limpiamos el título.
                var terminoDeBusqueda = tituloOriginal
                prefijosAEliminar.forEach { prefijo ->
                    if (terminoDeBusqueda.startsWith(prefijo, ignoreCase = true)) {
                        terminoDeBusqueda = terminoDeBusqueda.substring(prefijo.length)
                    }
                }

                // Log para verificar (opcional pero muy útil)
                Log.d("NotificationsFragment", "Título original: '$tituloOriginal' -> Término de búsqueda: '$terminoDeBusqueda'")

                // 4. Usamos el término limpio para la navegación.
                val action = NotificationsFragmentDirections.actionNotificationsToCursos(
                    terminoBusqueda = terminoDeBusqueda.trim() // .trim() para quitar espacios sobrantes
                )
                findNavController().navigate(action)
            }
        )

        // Aplicamos la configuración al RecyclerView del layout.
        binding.recyclerViewLearningPaths.apply {
            // Asignamos el adapter que acabamos de crear.
            adapter = rutaAdapter
            // Definimos cómo se mostrarán los items (en una lista vertical).
            layoutManager = LinearLayoutManager(context)
            // Optimización: si los items del RecyclerView tienen un tamaño fijo,
            // esto mejora el rendimiento.
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
//        (activity as? AppCompatActivity)?.supportActionBar?.show()

        super.onDestroyView()
        // Limpiamos la referencia al binding para evitar fugas de memoria
        _binding = null
    }
}