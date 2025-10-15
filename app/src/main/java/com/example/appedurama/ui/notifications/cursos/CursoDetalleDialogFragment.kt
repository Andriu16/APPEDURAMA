package com.example.appedurama.ui.notifications.cursos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.appedurama.R
import com.example.appedurama.data.model.Curso
import com.example.appedurama.databinding.DialogCursosBinding
import com.example.appedurama.ui.SharedViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class CursoDetalleDialogFragment : DialogFragment() {

    private var _binding: DialogCursosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CursosViewModel by viewModels({ requireParentFragment() })
    private val sharedViewModel: SharedViewModel by activityViewModels()
    companion object {
        private const val ARG_CURSO_ID = "curso_id"
        fun newInstance(cursoId: String) = CursoDetalleDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_CURSO_ID, cursoId) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCursosBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            // 1. Obtenemos las métricas de la pantalla del dispositivo.
            val displayMetrics = resources.displayMetrics

            // 2. Calculamos el ancho deseado.
            //    Usar un 90% (0.90) del ancho de la pantalla suele ser una buena medida.
            //    Puedes ajustar este valor (ej. 0.85 para más pequeño, 0.95 para más grande).
            val width = (displayMetrics.widthPixels * 0.90).toInt()

            // 3. Aplicamos el nuevo tamaño a la ventana del diálogo.
            //    - `width`: Nuestro ancho calculado.
            //    - `ViewGroup.LayoutParams.WRAP_CONTENT`: Le decimos que la altura se ajuste al contenido.
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cursoId = arguments?.getString(ARG_CURSO_ID) ?: run { dismiss(); return }
        val curso = viewModel.getCursoById(cursoId) ?: run { dismiss(); return }

        setupListeners(curso)
        populateUi(curso)
        observeMarcarCursoEvents()
    }

    private fun setupListeners(curso: Curso) {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnurl.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(curso.url)))
        }
        binding.btnmarcar.setOnClickListener {
            val usuarioActual = sharedViewModel.usuarioActual.value
            if (usuarioActual != null) {
                // Si hay un usuario logueado, llamamos al ViewModel para guardar
                binding.btnmarcar.isEnabled = false // Deshabilitar para evitar doble clic
                viewModel.marcarCursoComoSeleccionado(curso, usuarioActual.id)
            } else {
                // Si no hay usuario, mostramos un error
                Toast.makeText(context, "Error: Debes iniciar sesión para marcar un curso.", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun observeMarcarCursoEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.marcarCursoEvent.collect { event ->
                    when (event) {
                        is MarcarCursoEvent.Success -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            dismiss() // Opcional: cierra el diálogo después de guardar
                        }

                        is MarcarCursoEvent.Error -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            binding.btnmarcar.isEnabled =
                                true // Vuelve a habilitar el botón si hay error
                        }
                    }
                }
            }
        }
    }

    private fun populateUi(curso: Curso) {
        // --- 1. Llenar Vistas Comunes ---
        binding.ivImagen.load(curso.imagen) { placeholder(R.drawable.imgecel).error(R.drawable.imgecel) }
        binding.tvTitulo.text = curso.nombre
        binding.tvDescripcion.text = curso.descripcion ?: "No hay descripción disponible."

        // --- 2. Ocultar todos los contenedores de detalles ---
        hideAllDetails()

        // --- 3. Mostrar y llenar vistas según la plataforma ---
        when (curso.plataforma.lowercase(Locale.ROOT)) {
            "udemy" -> setupUdemy(curso)
            "platzi" -> setupPlatzi(curso)
            "upc" -> setupUpc(curso)
            "ulima" -> setupUlima(curso)
        }
    }

    private fun hideAllDetails() {
        binding.containerAutores.isVisible = false
        binding.containerCalificacion.isVisible = false
        binding.containerLectores.isVisible = false
        binding.containerNumeroClases.isVisible = false
        binding.containerDuracion.isVisible = false
        binding.containerHorasPractica.isVisible = false
        binding.containerDificultad.isVisible = false
        binding.containerModalidad.isVisible = false
        binding.containerCertificado.isVisible = false
        binding.containerFechaInicio.isVisible = false
        binding.containerHorario.isVisible = false
        binding.containerFechaPublicacion.isVisible = false
        binding.containerPrecio.isVisible = false
    }

    private fun setupUdemy(curso: Curso) {
        showAndSetText(binding.containerAutores, binding.tvAutores, curso.creadores)
        showAndSetText(binding.containerCalificacion, binding.tvCalificacion, curso.rating)
        showAndSetText(binding.containerLectores, binding.tvlectores, curso.lectores)
        showAndSetText(binding.containerDuracion, binding.tvhorasContenido, curso.horasCurso)
    }

    private fun setupPlatzi(curso: Curso) {
        showAndSetText(binding.containerDuracion, binding.tvhorasContenido, curso.horasContenido)
        showAndSetText(binding.containerFechaPublicacion, binding.tvfechaPublicacion, curso.fechaPublicacion)
        showAndSetText(binding.containerDificultad, binding.tvdificultad, curso.dificultad)
        showAndSetText(binding.containerNumeroClases, binding.tvnumeroDeClases, curso.numeroClases)
        showAndSetText(binding.containerHorasPractica, binding.tvHorasPracticas, curso.horasPractica)
    }

    private fun setupUpc(curso: Curso) {
        showAndSetText(binding.containerDuracion, binding.tvhorasContenido, curso.duracion)
        showAndSetText(binding.containerFechaInicio, binding.tvfechaDeInicio, curso.fechaInicio)
        showAndSetText(binding.containerModalidad, binding.tvmodalidad, curso.modalidad)
        showAndSetText(binding.containerCertificado, binding.tvcertificado, curso.certificado)
        showAndSetText(binding.containerHorario, binding.tvhorario, curso.horario)

        // Lógica especial para la tarjeta de precios de UPC
        if (curso.precioDescuento != null || curso.precioOriginal != null) {
            binding.containerPrecio.isVisible = true
            binding.tvprecioOriginal.text = curso.precioOriginal?.let { "S/ $it" } ?: ""
            binding.tvdescuento.text = curso.descuentoLabel
            binding.tvprecioConDescuento.text = curso.precioDescuento?.let { "S/ $it" } ?: ""
            binding.tvcuotas.text = curso.cuotas
            // Ocultar vistas de precio si no hay datos
            binding.tvprecioOriginal.isVisible = !curso.precioOriginal.isNullOrBlank()
            binding.tvdescuento.isVisible = !curso.descuentoLabel.isNullOrBlank()
            binding.tvcuotas.isVisible = !curso.cuotas.isNullOrBlank()
        }
    }

    private fun setupUlima(curso: Curso) {
        showAndSetText(binding.containerDuracion, binding.tvhorasContenido, curso.duracion)
        showAndSetText(binding.containerFechaInicio, binding.tvfechaDeInicio, curso.fechaInicio)
        showAndSetText(binding.containerModalidad, binding.tvmodalidad, curso.modalidad)

        // Lógica especial para la tarjeta de precios de ULima
        if (curso.precio != null) {
            binding.containerPrecio.isVisible = true
            binding.tvprecioConDescuento.text = curso.precio
            // Ocultamos los campos que ULima no usa
            binding.tvprecioOriginal.isVisible = false
            binding.tvdescuento.isVisible = false
            binding.tvcuotas.isVisible = false
        }
    }

    /**
     * Función de ayuda para mostrar un contenedor y asignarle texto si no es nulo.
     */
    private fun showAndSetText(container: View, textView: TextView, text: String?) {
        if (!text.isNullOrBlank()) {
            container.isVisible = true
            textView.text = text
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}