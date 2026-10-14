package com.pps.parapente.data

import java.util.Locale

/** Affiche 100.0 -> "100" et 12.5 -> "12,50". */
fun fmt(valeur: Double): String =
    if (valeur == valeur.toLong().toDouble()) valeur.toLong().toString()
    else String.format(Locale.FRANCE, "%.2f", valeur)

/** Version sans virgule française, pour préremplir un champ de saisie. */
fun fmtSaisie(valeur: Double): String =
    if (valeur == valeur.toLong().toDouble()) valeur.toLong().toString() else valeur.toString()
