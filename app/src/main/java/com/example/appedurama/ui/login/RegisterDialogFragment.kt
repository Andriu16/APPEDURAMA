package com.example.appedurama.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.appedurama.databinding.DialogRegisterUserBinding
import com.example.appedurama.ui.login.LoginViewModel
import com.example.appedurama.ui.login.RegistrationEvent
import com.example.appedurama.utils.ValidationUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterDialogFragment : DialogFragment() {

    private var _binding: DialogRegisterUserBinding? = null
    private val binding get() = _binding!!

    // Usamos activityViewModels para compartir el ViewModel con LoginFragment
    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogRegisterUserBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancelar.setOnClickListener { dismiss() }
        binding.btnRegistrar.setOnClickListener { handleRegistration() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observamos el estado de carga general
            viewModel.uiState.collect { state ->
                // Muestra un ProgressBar si lo añades al dialog_register_user.xml
                // binding.progressBarRegister.isVisible = state.isLoading
                binding.btnRegistrar.isEnabled = !state.isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observamos los eventos específicos de registro
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationEvent.collect { event ->
                    when (event) {
                        is RegistrationEvent.Success -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            dismiss()
                        }
                        is RegistrationEvent.Error -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun handleRegistration() {
        // 1. Obtener los valores de los campos de texto
        val nombres = binding.etNombres.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val dni = binding.etDni.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val password = binding.etPassword.text.toString() // La contraseña no se trimea
        val terminos = binding.cbTerminos.isChecked

        // 2. Realizar todas las validaciones
        val nameError = ValidationUtils.validateName(nombres)
        val lastNameError = ValidationUtils.validateName(apellidos)
        val dniError = ValidationUtils.validateDni(dni)
        val phoneError = ValidationUtils.validatePhone(telefono)
        val emailError = ValidationUtils.validateEmail(correo)
        val passwordError = ValidationUtils.validatePassword(password)
        val termsError = ValidationUtils.validateTerms(terminos)

        // 3. Mostrar errores en los campos correspondientes
        binding.etNombres.setErrorAndFocus(nameError)
        binding.etApellidos.setErrorAndFocus(lastNameError)
        binding.etDni.setErrorAndFocus(dniError)
        binding.etTelefono.setErrorAndFocus(phoneError)
        binding.etCorreo.setErrorAndFocus(emailError)
        binding.etPassword.setErrorAndFocus(passwordError)

        // 4. Si hay algún error, detener el proceso
        val errors = listOfNotNull(nameError, lastNameError, dniError, phoneError, emailError, passwordError)
        if (errors.isNotEmpty()) {
            return // Detiene la ejecución si algún campo de texto tiene error
        }

        // El error de los términos se muestra con un Toast
        if (termsError != null) {
            Toast.makeText(context, termsError, Toast.LENGTH_SHORT).show()
            return
        }

        // 5. Si todo es válido, llamamos al ViewModel para registrar al usuario
        viewModel.registerUser(nombres, apellidos, dni, telefono, correo, password, terminos)
    }

    private fun TextInputEditText.setErrorAndFocus(errorMessage: String?) {
        val parentLayout = this.parent.parent as? TextInputLayout
        if (errorMessage != null) {
            parentLayout?.error = errorMessage
            this.requestFocus()
        } else {
            parentLayout?.error = null
        }
    }

    override fun onStart() {
        super.onStart()
        // Ajustar el tamaño del diálogo
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RegisterDialog"
    }
}