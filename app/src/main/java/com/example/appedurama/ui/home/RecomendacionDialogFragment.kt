package com.example.appedurama.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.appedurama.databinding.DialogRecomendacionesPersonalizadoBinding
import com.example.appedurama.ui.home.adapter.RecomendacionAdapter
import com.example.appedurama.data.model.RecomendacionItem
import androidx.navigation.fragment.findNavController
import com.example.appedurama.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecomendacionDialogFragment : DialogFragment() {

    private var _binding: DialogRecomendacionesPersonalizadoBinding? = null
    private val binding get() = _binding!!

    // Usaremos un companion object para pasar los datos de forma segura
    companion object {
        private const val ARG_RECOMENDACIONES = "recomendaciones_texto"

        fun newInstance(textoRecomendacion: String): RecomendacionDialogFragment {
            val fragment = RecomendacionDialogFragment()
            val args = Bundle()
            args.putString(ARG_RECOMENDACIONES, textoRecomendacion)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogRecomendacionesPersonalizadoBinding.inflate(inflater, container, false)

        // Estilo para quitar el título por defecto del diálogo
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textoCompleto = arguments?.getString(ARG_RECOMENDACIONES) ?: ""
        val recomendacionesParseadas = parsearRecomendaciones(textoCompleto)

        val adapter = RecomendacionAdapter(recomendacionesParseadas)
        binding.recyclerViewRecomendaciones.adapter = adapter

        binding.btnAceptarDialogo.setOnClickListener {
            dismiss() // Cierra el diálogo
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNav?.selectedItemId = R.id.navigation_notifications
        }
    }

    private fun parsearRecomendaciones(texto: String): List<RecomendacionItem> {
        val recomendaciones = mutableListOf<RecomendacionItem>()
        // Expresión regular para capturar "1. **Título:** Descripción"
        val regex = """\d+\.\s*\*\*(.*?)\*\*[:\s]*(.*)""".toRegex()

        texto.lines().forEach { linea ->
            val matchResult = regex.find(linea)
            if (matchResult != null) {
                val (titulo, descripcion) = matchResult.destructured
                recomendaciones.add(RecomendacionItem(titulo.trim(), descripcion.trim()))
            }
        }
        return recomendaciones
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            // Obtenemos las dimensiones de la pantalla
            val displayMetrics = resources.displayMetrics
            // Calculamos el 90% del ancho de la pantalla
            val width = (displayMetrics.widthPixels * 0.90).toInt()
            // Aplicamos el nuevo ancho y mantenemos la altura ajustada al contenido
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}