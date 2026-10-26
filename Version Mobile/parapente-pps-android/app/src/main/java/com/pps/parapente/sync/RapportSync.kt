package com.pps.parapente.sync

/** Résumé chiffré d'une synchronisation. */
data class RapportSync(
    var utilisateursRecus: Int = 0, var utilisateursEnvoyes: Int = 0,
    var pilotesRecus: Int = 0, var pilotesEnvoyes: Int = 0,
    var epreuvesRecues: Int = 0, var epreuvesEnvoyees: Int = 0,
    var inscriptionsRecues: Int = 0, var inscriptionsEnvoyees: Int = 0,
    var resultatsRecus: Int = 0, var resultatsEnvoyes: Int = 0,
    var pilotesSupprimes: Int = 0, var epreuvesSupprimees: Int = 0,
    var inscriptionsSupprimees: Int = 0, var resultatsSupprimes: Int = 0,
    var utilisateursSupprimes: Int = 0
) {
    fun resume(): String {
        val total = pilotesSupprimes + epreuvesSupprimees + inscriptionsSupprimees + resultatsSupprimes + utilisateursSupprimes
        if (total == 0) return resumeBase()
        return resumeBase() + "\n\nSuppressions (pour rendre les deux bases identiques) :\n" +
            "Pilotes : $pilotesSupprimes, Épreuves : $epreuvesSupprimees, " +
            "Inscriptions : $inscriptionsSupprimees, Résultats : $resultatsSupprimes, " +
            "Comptes : $utilisateursSupprimes"
    }

    private fun resumeBase(): String = """
        Comptes utilisateurs : $utilisateursRecus reçus, $utilisateursEnvoyes envoyés
        Pilotes : $pilotesRecus reçus, $pilotesEnvoyes envoyés
        Épreuves (+ variables/barèmes) : $epreuvesRecues reçues, $epreuvesEnvoyees envoyées
        Inscriptions : $inscriptionsRecues reçues, $inscriptionsEnvoyees envoyées
        Résultats : $resultatsRecus reçus, $resultatsEnvoyes envoyés
    """.trimIndent()
}
