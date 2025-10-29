package com.example.appedurama.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.appedurama.R
import com.example.appedurama.databinding.DialogMensajeBinding

class MensajeDialogFragment : DialogFragment() {

    private var _binding: DialogMensajeBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG = "MensajeDialog"
        private const val ARG_TITULO = "arg_titulo"
        private const val ARG_MENSAJE = "arg_mensaje"

        fun newInstance(titulo: String, mensaje: String): MensajeDialogFragment {
            val args = Bundle()
            args.putString(ARG_TITULO, titulo)
            args.putString(ARG_MENSAJE, mensaje)
            val fragment = MensajeDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogMensajeBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitulo.text = arguments?.getString(ARG_TITULO) ?: "Atención"
        binding.tvMensage.text = arguments?.getString(ARG_MENSAJE) ?: "Ha ocurrido un problema."

        // En este caso, solo necesitamos el botón de aceptar. Ocultamos el otro.
        binding.btnCancelar.visibility = View.GONE
        binding.btnAceptar.text = "ENTENDIDO"
        binding.btnAceptar.setOnClickListener {
            dismiss()
        }

        // Puedes personalizar el icono si lo pasas como argumento también
        binding.ivIcon.setImageResource(R.drawable.ic_mensaje_warning)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}