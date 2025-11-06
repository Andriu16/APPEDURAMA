package com.example.appedurama.utils

import android.util.Patterns

object ValidationUtils {

    // Expresión regular para nombres y apellidos (solo letras y espacios)
    private val nameRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚ ]+\$")

    // Dominios de correo permitidos
    private val allowedEmailDomains = setOf("gmail.com", "outlook.com", "hotmail.com")

    // Expresiones regulares para la contraseña
    private val uppercaseRegex = Regex(".*[A-Z].*")
    private val lowercaseRegex = Regex(".*[a-z].*")
    private val digitRegex = Regex(".*\\d.*")
    private val specialCharRegex = Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")
    private val noN_Regex = Regex(".*[ñÑ].*")

    /**
     * Valida nombres o apellidos.
     * @return null si es válido, o un mensaje de error si no lo es.
     */
    fun validateName(name: String): String? {
        if (name.isBlank()) {
            return "Este campo es obligatorio."
        }
        if (!name.matches(nameRegex)) {
            return "Solo se permiten letras y espacios."
        }
        return null
    }

    /**
     * Valida el DNI.
     * @return null si es válido, o un mensaje de error si no lo es.
     */
    fun validateDni(dni: String): String? {
        if (dni.isBlank()) {
            return "Este campo es obligatorio."
        }
        if (dni.length != 8 || !dni.all { it.isDigit() }) {
            return "Debe contener 8 dígitos numéricos."
        }
        return null
    }

    /**
     * Valida el teléfono.
     * @return null si es válido, o un mensaje de error si no lo es.
     */
    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) {
            return "Este campo es obligatorio."
        }
        if (phone.length != 9 || !phone.all { it.isDigit() }) {
            return "Debe contener 9 dígitos numéricos."
        }
        return null
    }

    /**
     * Valida el correo electrónico y su dominio.
     * @return null si es válido, o un mensaje de error si no lo es.
     */
    fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "Este campo es obligatorio."
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Formato de correo inválido."
        }
        val domain = email.substringAfter('@', "")
        if (domain !in allowedEmailDomains) {
            return "Solo se permiten dominios: gmail, outlook, hotmail."
        }
        return null
    }

    /**
     * Valida la contraseña según las reglas especificadas.
     * @return null si es válida, o un mensaje de error si no lo es.
     */
    fun validatePassword(password: String): String? {
        if (password.isBlank()) {
            return "Este campo es obligatorio."
        }
        if (password.length < 8) {
            return "Mínimo 8 caracteres."
        }
        if (!password.matches(uppercaseRegex)) {
            return "Debe contener al menos una mayúscula."
        }
        if (!password.matches(lowercaseRegex)) {
            return "Debe contener al menos una minúscula."
        }
        if (!password.matches(digitRegex)) {
            return "Debe contener al menos un número."
        }
        if (!password.matches(specialCharRegex)) {
            return "Debe contener al menos un carácter especial."
        }
        if (password.matches(noN_Regex)) {
            return "La letra 'ñ' no está permitida."
        }
        return null
    }

    /**
     * Valida si los términos y condiciones fueron aceptados.
     * @return null si es válido, o un mensaje de error si no lo es.
     */
    fun validateTerms(accepted: Boolean): String? {
        if (!accepted) {
            return "Debe aceptar los términos y condiciones."
        }
        return null
    }
}