package com.pps.parapente.data

import java.security.MessageDigest

object Securite {
    /** Hachage SHA-256 du mot de passe (identique à la version PC). */
    fun hacher(motDePasse: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(motDePasse.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
