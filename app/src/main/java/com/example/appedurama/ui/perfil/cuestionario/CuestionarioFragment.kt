package com.example.appedurama.ui.perfil.cuestionario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.appedurama.databinding.FragmentCuestionarioBinding
import com.example.appedurama.ui.SharedViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class CuestionarioFragment : Fragment() {

    private var _binding: FragmentCuestionarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CuestionarioViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // Listas para acceder a las vistas de forma programática
    private lateinit var preguntaTextViews: List<TextView>
    private lateinit var preguntaRadioGroups: List<RadioGroup>
    private lateinit var preguntaCards: List<MaterialCardView>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCuestionarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inicializarListasDeVistas()

        val usuario = sharedViewModel.usuarioActual.value
        if (usuario == null) {
            Toast.makeText(context, "Error: No se pudo identificar al usuario.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }

        setupListeners(usuario.id)
        observeUiState()

        // Iniciar la generación del cuestionario
        if (viewModel.uiState.value.cuestionario.isEmpty()) {
            viewModel.generarCuestionario(usuario.id)
        }
    }

    private fun inicializarListasDeVistas() {
        preguntaCards = listOf(
            binding.cardPregunta1, binding.cardPregunta2, binding.cardPregunta3, binding.cardPregunta4, binding.cardPregunta5,
            binding.cardPregunta6, binding.cardPregunta7, binding.cardPregunta8, binding.cardPregunta9, binding.cardPregunta10
        )
        preguntaTextViews = listOf(
            binding.tvPregunta1, binding.tvPregunta2, binding.tvPregunta3, binding.tvPregunta4, binding.tvPregunta5,
            binding.tvPregunta6, binding.tvPregunta7, binding.tvPregunta8, binding.tvPregunta9, binding.tvPregunta10
        )
        preguntaRadioGroups = listOf(
            binding.rgPregunta1, binding.rgPregunta2, binding.rgPregunta3, binding.rgPregunta4, binding.rgPregunta5,
            binding.rgPregunta6, binding.rgPregunta7, binding.rgPregunta8, binding.rgPregunta9, binding.rgPregunta10
        )
    }

    private fun setupListeners(usuarioId: Int) {
        preguntaRadioGroups.forEachIndexed { index, radioGroup ->
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                val radioButton = view?.findViewById<RadioButton>(checkedId)
                val opcionSeleccionada = radioGroup.indexOfChild(radioButton) + 1
                viewModel.responderPregunta(index, opcionSeleccionada)
            }
        }

        binding.btnEnviarQuiz.setOnClickListener {
            val respuestasContadas = viewModel.uiState.value.respuestasUsuario.size
            if (respuestasContadas < 10) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Cuestionario Incompleto")
                    .setMessage("Aún no has respondido todas las preguntas. ¿Deseas enviarlo de todas formas?")
                    .setPositiveButton("Sí, enviar") { _, _ -> viewModel.finalizarQuiz(usuarioId) }
                    .setNegativeButton("No, continuar", null)
                    .show()
            } else {
                viewModel.finalizarQuiz(usuarioId)
            }
        }
    }

//    private fun observeUiState() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.uiState.collect { state ->
//                    // Mostrar/Ocultar loading (puedes añadir un ProgressBar a tu XML)
//                    // binding.progressBar.isVisible = state.isLoading
//
//                    if (state.error != null) {
//                        binding.tvStatusMessage.text = state.error
//                        binding.tvStatusMessage.isVisible = true
//                        // Ocultar el resto de la UI
//                        binding.btnEnviarQuiz.isVisible = false
//                        preguntaCards.forEach { it.isVisible = false
//                        Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
//                     } else{
//                            binding.tvStatusMessage.isVisible = false
//                        }
//
//
//
//                    // Popular las preguntas cuando estén listas
//                    if (state.cuestionario.isNotEmpty()) {
//                        preguntaCards.forEach { it.isVisible = true }
//                        state.cuestionario.forEachIndexed { index, pregunta ->
//                            preguntaTextViews[index].text = pregunta.pregunta
//                            val radioGroup = preguntaRadioGroups[index]
//                            for (i in 0 until radioGroup.childCount) {
//                                (radioGroup.getChildAt(i) as? RadioButton)?.text = pregunta.opciones.getOrNull(i) ?: ""
//                            }
//                        }
//                    } else {
//                        preguntaCards.forEach { it.isVisible = false }
//                    }
//
//                    // Actualizar UI del progreso
//                    val completadas = state.respuestasUsuario.size
//                    binding.progressBar.progress = completadas
//                    binding.tvProgreso.text = "$completadas/10 Completadas"
//
//                    // Actualizar timer
//                    val minutos = state.tiempoTranscurridoSegundos / 60
//                    val segundos = state.tiempoTranscurridoSegundos % 60
//                    binding.tvTiempo.text = String.format("%02d:%02d", minutos, segundos)
//
//                    // Mostrar resultado final
//                    state.resultadoFinal?.let { resultado ->
//                        // Mostramos el diálogo
//                        mostrarDialogoResultado(resultado.puntaje)
//                        // Inmediatamente le decimos al ViewModel que ya lo mostramos (lo consumimos)
//                        viewModel.onResultadoMostrado()
//                    }
//                }
//            }
//        }
//    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Muestra un ProgressBar si tienes uno, ej: binding.loadingProgressBar.isVisible = state.isLoading

                    // 1. Manejo centralizado del error
                    if (state.error != null) {
                        // Si hay un error, mostramos el mensaje y ocultamos todo lo demás
                        binding.tvStatusMessage.text = state.error
                        binding.tvStatusMessage.isVisible = true
                        binding.btnEnviarQuiz.isVisible = false
                        preguntaCards.forEach { it.isVisible = false }
                        // También podrías mostrar un Toast si prefieres
                        // Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()

                    } else if (state.cuestionario.isNotEmpty()) {
                        // 2. Si NO hay error y SÍ hay preguntas, mostramos el cuestionario
                        binding.tvStatusMessage.isVisible = false
                        binding.btnEnviarQuiz.isVisible = true
                        preguntaCards.forEach { it.isVisible = true }

                        state.cuestionario.forEachIndexed { index, pregunta ->
                            preguntaTextViews[index].text = pregunta.pregunta
                            val radioGroup = preguntaRadioGroups[index]
                            for (i in 0 until radioGroup.childCount) {
                                (radioGroup.getChildAt(i) as? RadioButton)?.text = pregunta.opciones.getOrNull(i) ?: ""
                            }
                        }
                    } else if (state.isLoading) {
                        // 3. Si está cargando y aún no hay preguntas/error
                        binding.tvStatusMessage.text = "Generando tu cuestionario..."
                        binding.tvStatusMessage.isVisible = true
                        binding.btnEnviarQuiz.isVisible = false
                        preguntaCards.forEach { it.isVisible = false }
                    }

                    // 4. Actualizar UI de progreso (esto se ejecuta siempre)
                    val completadas = state.respuestasUsuario.size
                    binding.progressBar.progress = completadas
                    binding.tvProgreso.text = "$completadas/10 Completadas"

                    // 5. Actualizar timer
                    val minutos = state.tiempoTranscurridoSegundos / 60
                    val segundos = state.tiempoTranscurridoSegundos % 60
                    binding.tvTiempo.text = String.format("%02d:%02d", minutos, segundos)

                    // 6. Mostrar resultado final
                    state.resultadoFinal?.let { resultado ->
                        mostrarDialogoResultado(resultado.puntaje)
                        viewModel.onResultadoMostrado()
                    }
                }
            }
        }
    }

    private fun mostrarDialogoResultado(puntaje: Int) {

        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setTitle("¡Cuestionario Finalizado!")
            .setMessage("Has obtenido un puntaje de: $puntaje/10.\nTu resultado ha sido guardado.")
            .setPositiveButton("Aceptar") { _, _ ->
                findNavController().popBackStack() // Volver a la pantalla de perfil
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}