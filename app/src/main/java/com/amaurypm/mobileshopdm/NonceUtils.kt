package com.amaurypm.mobileshopdm

import android.util.Base64
import java.security.SecureRandom

object NonceUtils {

    private val secureRandom = SecureRandom()

    fun generateNonce(length: Int): String {
        val bytes = ByteArray(length)
        // SecureRandom genera bytes aleatorios de manera segura
        secureRandom.nextBytes(bytes)
        // Codificamos los bytes generados de manera segura en una cadena Base64
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP).substring(0, length)
    }
}