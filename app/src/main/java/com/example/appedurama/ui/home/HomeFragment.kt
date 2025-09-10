package com.example.appedurama.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appedurama.databinding.FragmentHomeBinding
import androidx.fragment.app.activityViewModels
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.appedurama.R
import com.example.appedurama.data.model.EncuestaRespuestas
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appedurama.ui.SharedViewModel
import com.example.appedurama.ui.notifications.NotificationsViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    // Referencia a la ProgressBar de MainActivity
    private var mainProgressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        mainProgressBar = activity?.findViewById(R.id.main_progress_bar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        val initialPaddingBottom = binding.homeScrollview.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.homeScrollview) { v, insets ->

            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())


            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                initialPaddingBottom + systemBarInsets.bottom
            )


            insets
        }

        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        binding.btnEnviar.setOnClickListener {
            recogerYEnviarDatos()
        }


        val radioButtons = listOf(binding.radioLogica, binding.radioDiseno, binding.radioDatos)

        radioButtons.forEach { clickedButton ->
            clickedButton.setOnClickListener {

                radioButtons.forEach { otherButton ->
                    if (otherButton.id != clickedButton.id) {
                        otherButton.isChecked = false
                    }
                }

                clickedButton.isChecked = true
            }
        }
    }

    private fun recogerYEnviarDatos() {
        val respuesta1 = binding.editTextPregunta1.text.toString().trim()
        val respuesta2 = binding.editTextPregunta2.text.toString().trim()
        val respuesta4 = binding.editTextPregunta4.text.toString().trim()

        val radioButtons = listOf(binding.radioLogica, binding.radioDiseno, binding.radioDatos)
        if (respuesta1.isEmpty() || respuesta2.isEmpty() || radioButtons.none { it.isChecked }) {
            Toast.makeText(context, "Por favor, completa todas las preguntas obligatorias", Toast.LENGTH_SHORT).show()
            return
        }
        val radioButtonSeleccionado = radioButtons.first { it.isChecked }
        val respuesta3_preferencia = radioButtonSeleccionado.text.toString()

        val respuesta3_detalle = binding.editTextPregunta3Detalle.text.toString().trim()

        val respuestas = EncuestaRespuestas(
            respuesta1 = respuesta1,
            respuesta2 = respuesta2,
            respuesta3_preferencia = respuesta3_preferencia,
            respuesta3_detalle = respuesta3_detalle,
            respuesta4 = respuesta4
        )

        homeViewModel.enviarEncuesta(respuestas)
    }

    private fun observeUiState(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.uiState.collect { state ->
                    // Gestionar la carga

                    mainProgressBar?.isVisible = state.isLoading
                    binding.btnEnviar.isEnabled = !state.isLoading



                    // Gestionar el error
                    state.error?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        homeViewModel.errorMostrado()
                    }

                    // Gestionar el éxito
                    state.recomendacion?.let { texto ->
                        mostrarDialogoRecomendacion(texto)
                        homeViewModel.recomendacionMostrada()
                    }
                }
            }
        }
    }

    private fun mostrarDialogoRecomendacion(textoRecomendacion: String) {
        sharedViewModel.setTextoRecomendaciones(textoRecomendacion)
        val dialogFragment = RecomendacionDialogFragment.newInstance(textoRecomendacion)
        dialogFragment.show(parentFragmentManager, "RecomendacionDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        mainProgressBar = null // Limpiar referencia
        _binding = null
    }
}