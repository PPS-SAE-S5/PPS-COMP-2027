package com.pps.parapente.data

import org.json.JSONObject
import java.util.Calendar

enum class Role(val libelle: String) {
    ADMINISTRATEUR("Administrateur"),
    RESPONSABLE_EPREUVE("Responsable de l'épreuve"),
    BENEVOLE("Bénévole"),
    PILOTE("Pilote"),
    COMITE_PILOTES("Comité des pilotes")
}

/** Modes de calcul des points d'une épreuve (mêmes que la version PC). */
object Mode {
    const val FORMULE = "FORMULE"
    const val BAREME = "BAREME"
}

data class EpreuveComplete(
    val epreuve: EpreuveEntity,
    val parametres: List<ParametreEntity>,
    val bareme: List<BaremeEntity>
)

data class ResultatAffiche(
    val resultat: ResultatEntity,
    val pilote: PiloteEntity?,
    val valeurs: Map<String, Double>
)

data class LigneClassement(
    val pilote: PiloteEntity,
    val total: Double,
    val rang: Int,
    val pointsParEpreuve: Map<Int, Double>
)

val PiloteEntity.nomComplet: String get() = "$prenom $nom".trim()

/** Filtre de classement : mixte (hommes + femmes), hommes seulement ou femmes seulement. */
enum class FiltreGenre(val libelle: String, val valeur: String?) {
    MIXTE("Mixte (hommes + femmes)", null),
    HOMMES("Hommes", "Homme"),
    FEMMES("Femmes", "Femme")
}

/** Vrai si le pilote appartient au classement demandé (le mixte accepte tout le monde, même sans genre). */
fun PiloteEntity.correspond(filtre: FiltreGenre): Boolean =
    filtre.valeur == null || genre?.trim().equals(filtre.valeur, ignoreCase = true)

val PiloteEntity.age: Int
    get() = anneeNaissance?.let { Calendar.getInstance().get(Calendar.YEAR) - it } ?: 0

/** Texte affiché dans les listes déroulantes. */
fun PiloteEntity.libelle(): String =
    nomComplet + if (numeroLicence.isNotBlank()) " (n°$numeroLicence)" else ""

fun ResultatEntity.valeurs(): Map<String, Double> {
    val objet = JSONObject(valeursJson)
    val resultat = linkedMapOf<String, Double>()
    objet.keys().forEach { resultat[it] = objet.getDouble(it) }
    return resultat
}

fun mapVersJson(valeurs: Map<String, Double>): String = JSONObject(valeurs as Map<*, *>).toString()
