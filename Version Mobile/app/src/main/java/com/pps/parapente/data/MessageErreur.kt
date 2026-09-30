package com.pps.parapente.data

/** Erreur "métier" : son message est déjà en français et destiné à être affiché tel quel. */
class ErreurMetier(message: String) : RuntimeException(message)

/**
 * Traduit toute erreur technique en message français compréhensible.
 * Aucun message technique brut (SQL, Kotlin, anglais) n'est affiché à l'utilisateur.
 */
object MessageErreur {
    fun traduire(e: Throwable): String {
        if (e is ErreurMetier) return e.message ?: "La demande n'a pas pu être traitée."

        val m = (e.message ?: "").lowercase()
        return when {
            "unique constraint" in m && "numerolicence" in m ->
                "Ce numéro de licence est déjà utilisé par un autre pilote."
            "unique constraint" in m && "identifiant" in m ->
                "Cet identifiant de compte est déjà utilisé. Choisissez-en un autre."
            "unique constraint" in m ->
                "Cette information existe déjà dans la base de données (doublon détecté)."
            "foreign key" in m ->
                "Impossible d'effectuer cette action : l'élément est encore utilisé ailleurs (résultats, inscriptions...)."
            "not null" in m ->
                "Un champ obligatoire n'a pas été renseigné. Merci de vérifier votre saisie."
            e is NumberFormatException ->
                "Une valeur numérique attendue n'est pas valide."
            else ->
                "Une erreur technique est survenue. Merci de réessayer ; " +
                    "si le problème persiste, contactez la personne en charge de l'application."
        }
    }
}
