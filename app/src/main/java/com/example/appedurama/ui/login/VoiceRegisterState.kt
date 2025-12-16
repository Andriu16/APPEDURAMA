package com.example.appedurama.ui.login

enum class VoiceRegisterState {
    IDLE,
    AWAITING_NOMBRES,
    AWAITING_APELLIDOS,
    AWAITING_DNI,
    AWAITING_TELEFONO,
    AWAITING_CORREO,
    AWAITING_PASSWORD,
    AWAITING_TERMS,
    AWAITING_COMMAND
}