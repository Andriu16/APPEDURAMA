package com.example.appedurama.ui.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.speech.tts.UtteranceProgressListener

import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
//import androidx.navigation.fragment.R
import androidx.fragment.app.activityViewModels
import com.example.appedurama.ui.SharedViewModel
import androidx.navigation.fragment.findNavController
import com.example.appedurama.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.appedurama.R

class LoginFragment : Fragment(), TextToSpeech.OnInitListener, RecognitionListener {

    private val loginViewModel: LoginViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val VOICE_TAG = "VOICE_DEBUG"

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private var currentState: VoiceLoginState = VoiceLoginState.IDLE
    private var isListening = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(VOICE_TAG, "Permiso de audio CONCEDIDO")
                startVoiceLoginFlow()
            } else {
                Log.d(VOICE_TAG, "Permiso de audio DENEGADO")
                Toast.makeText(requireContext(), "Permiso de micrófono necesario", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeUiState()
        initializeVoiceComponents()
    }

    private fun initializeVoiceComponents() {
        Log.d(VOICE_TAG, "Inicializando componentes de voz...")

        textToSpeech = TextToSpeech(requireContext(), this)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext()).apply {
            setRecognitionListener(this@LoginFragment)
        }
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

        binding.fabVoiceLogin.setOnClickListener {
            Log.d(VOICE_TAG, "Botón de micrófono presionado.")
            checkAudioPermissionAndStart()
        }
    }

    private fun checkAudioPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(VOICE_TAG, "Permiso de audio concedido. Iniciando flujo.")
                startVoiceLoginFlow()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(requireContext(), "Necesitamos acceso al micrófono para reconocimiento de voz", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startVoiceLoginFlow() {
        if (!::textToSpeech.isInitialized || !::speechRecognizer.isInitialized) {
            Log.e(VOICE_TAG, "Error: Componentes de voz no inicializados")
            return
        }

        if (isListening) {
            stopListening()
        }

        currentState = VoiceLoginState.AWAITING_EMAIL
        speak("Por favor, dígame su correo electrónico.") {
            startListening()
        }


    }

    private fun startListening() {
        Log.d(VOICE_TAG, ">>> Iniciando escucha...")

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Escuchando...")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer.startListening(intent)
            isListening = true
            Log.d(VOICE_TAG, "Escucha iniciada correctamente")

        } catch (e: Exception) {
            Log.e(VOICE_TAG, "Error al iniciar reconocimiento: ${e.message}", e)
            speak("Error al iniciar el micrófono. Por favor, intenta nuevamente.")
            currentState = VoiceLoginState.IDLE
            isListening = false
        }
    }

    private fun stopListening() {
        try {
            speechRecognizer.stopListening()
            isListening = false
            Log.d(VOICE_TAG, "Escucha detenida")
        } catch (e: Exception) {
            Log.d(VOICE_TAG, "Error al detener escucha: ${e.message}")
        }
    }


    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(VOICE_TAG, "onReadyForSpeech: Listo para escuchar")
    }

    override fun onBeginningOfSpeech() {
        Log.d(VOICE_TAG, "onBeginningOfSpeech: Inicio de habla detectado")
    }

    override fun onRmsChanged(rmsdB: Float) {

    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d(VOICE_TAG, "onBufferReceived: Buffer recibido")
    }

    override fun onEndOfSpeech() {
        Log.d(VOICE_TAG, "onEndOfSpeech: Fin de habla detectado")
        isListening = false
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
            SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
            SpeechRecognizer.ERROR_NETWORK -> "Error de red"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout de red"
            SpeechRecognizer.ERROR_NO_MATCH -> "No se encontró coincidencia"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
            SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout de voz - no se detectó habla"
            else -> "Error desconocido: $error"
        }
        Log.e(VOICE_TAG, "onError: $errorMessage")


        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (currentState != VoiceLoginState.IDLE) {
                    startListening()
                }
            }, 2000)
        } else {
            speak("No pude entenderte. Por favor, intenta nuevamente.")
            Handler(Looper.getMainLooper()).postDelayed({
                if (currentState != VoiceLoginState.IDLE) {
                    startListening()
                }
            }, 3000)
        }

        isListening = false
    }

    override fun onResults(results: Bundle?) {
        Log.d(VOICE_TAG, "onResults: Resultados recibidos")

        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.d(VOICE_TAG, "Número de matches: ${matches?.size ?: 0}")

        if (!matches.isNullOrEmpty()) {
            val voiceInput = matches[0].trim()
            Log.i(VOICE_TAG, "Texto reconocido: '$voiceInput'")

            Handler(Looper.getMainLooper()).post {
                processVoiceInput(voiceInput)
            }
        } else {
            Log.w(VOICE_TAG, "No se reconocieron palabras")
            speak("No pude entender lo que dijiste. Por favor, intenta nuevamente.")
            Handler(Looper.getMainLooper()).postDelayed({
                if (currentState != VoiceLoginState.IDLE) {
                    startListening()
                }
            }, 3000)
        }

        isListening = false
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.d(VOICE_TAG, "onPartialResults: Resultados parciales")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(VOICE_TAG, "onEvent: Tipo de evento $eventType")
    }

    private fun processVoiceInput(input: String) {
        Log.i(VOICE_TAG, "Procesando: '$input' en estado: $currentState")

        if (input.isBlank()) {
            Log.w(VOICE_TAG, "Entrada vacía")
            speak("No escuché nada. Por favor, intenta nuevamente.")
            Handler(Looper.getMainLooper()).postDelayed({ startListening() }, 2000)
            return
        }

        when (currentState) {
            VoiceLoginState.AWAITING_EMAIL -> {

                val email = input.replace(" ", "")
                    .replace("arroba", "@")
                    .replace(" arroba ", "@")
                    .replace("punto", ".")
                    .replace(" punto ", ".")
                    .lowercase(Locale.ROOT)

                binding.editTextEmail.setText(email)
                Log.d(VOICE_TAG, "Email establecido: $email")

                speak("Correo registrado. Ahora, dígame su contraseña.") {
                    startListening()
                }
                currentState = VoiceLoginState.AWAITING_PASSWORD


            }

            VoiceLoginState.AWAITING_PASSWORD -> {
                binding.editTextPassword.setText(input)
                Log.d(VOICE_TAG, "Contraseña establecida")

                speak("Contraseña registrada. Diga 'iniciar sesión' para continuar o 'cancelar' para volver a empezar.") {
                    startListening()
                }
                currentState = VoiceLoginState.AWAITING_COMMAND


            }

            VoiceLoginState.AWAITING_COMMAND -> {
                when {
                    input.contains("iniciar sesión", ignoreCase = true) ||
                            input.contains("entrar", ignoreCase = true) -> {
                        speak("Iniciando sesión.")
                        binding.buttonLogin.performClick()
                        currentState = VoiceLoginState.IDLE
                    }
                    input.contains("registrarse", ignoreCase = true) -> {
                        speak("Abriendo registro")
                        currentState = VoiceLoginState.IDLE
                    }
                    input.contains("cancelar", ignoreCase = true) -> {
                        speak("Cancelando. Toque el micrófono para empezar de nuevo.")
                        currentState = VoiceLoginState.IDLE
                    }
                    else -> {
                        speak("Comando no reconocido. Diga 'iniciar sesión', 'registrarse' o 'cancelar'.")
                        Handler(Looper.getMainLooper()).postDelayed({
                            startListening()
                        }, 3000)
                    }
                }
            }

            VoiceLoginState.IDLE -> {
                Log.w(VOICE_TAG, "Entrada recibida en estado IDLE")
            }
        }
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(VOICE_TAG, "TTS: Idioma español no soportado")
            } else {
                Log.i(VOICE_TAG, "TTS inicializado correctamente en español.")
            }
        } else {
            Log.e(VOICE_TAG, "TTS: Falló la inicialización")
        }
    }

    private fun speak(text: String, onDone: (() -> Unit)? = null) {
        Log.d(VOICE_TAG, "Hablando: '$text'")

        if (!::textToSpeech.isInitialized) {
            Log.e(VOICE_TAG, "TTS no inicializado")
            onDone?.invoke()
            return
        }

        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }


        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {

                Handler(Looper.getMainLooper()).post {
                    onDone?.invoke()
                }
            }
            override fun onError(utteranceId: String?) {
                Handler(Looper.getMainLooper()).post {
                    onDone?.invoke()
                }
            }
        })


        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID")
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "UniqueID")
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading

                    state.error?.let { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        loginViewModel.errorShown()
                    }

                    if (state.loginSuccess && state.usuario != null) {
                        sharedViewModel.setUsuario(state.usuario)
                        Toast.makeText(
                            context,
                            "¡Bienvenido ${state.usuario.nombre}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_loginFragment_to_bienvenidaFragment)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(VOICE_TAG, "Destruyendo recursos de voz")

        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }

        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }

        _binding = null
    }
}