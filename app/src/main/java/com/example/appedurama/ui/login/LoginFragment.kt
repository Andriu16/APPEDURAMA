package com.example.appedurama.ui.login

import com.example.appedurama.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.appedurama.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.appedurama.ui.SharedViewModel

class LoginFragment : Fragment() {

    // ViewModel-ktx para obtener la instancia del ViewModel
    private val loginViewModel: LoginViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    // ViewBinding para acceder a las vistas de forma segura
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginViewModel.login(email, password)
            } else {
                Toast.makeText(
                    context,
                    "Por favor, ingresa correo y contraseña",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.textViewRegister.setOnClickListener {
            val registerDialog = RegisterDialogFragment()
            registerDialog.show(parentFragmentManager, RegisterDialogFragment.TAG)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading

                    // Mostrar error si existe
                    state.error?.let { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        loginViewModel.errorShown()
                    }




                    if (state.loginSuccess && state.usuario != null) {
                        Toast.makeText(
                            context,
                            "¡Bienvenido ${state.usuario.nombre}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 3. GUARDA EL USUARIO EN EL SHAREDVIEWMODEL
                        sharedViewModel.setUsuario(state.usuario)

                        // 4. NAVEGA A LA SIGUIENTE PANTALLA
                        findNavController().navigate(R.id.action_loginFragment_to_bienvenidaFragment)
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