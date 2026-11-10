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

/** Compte « invité » de la version PC : accès au classement seulement (rôle Pilote). */
object Invite {
    const val IDENTIFIANT = "invité"
    const val MOT_DE_PASSE = "invitationPPS"
}

/** Moment de la journée d'un atelier. */
object Creneau {
    const val MATIN = "MATIN"
    const val APRES_MIDI = "APRES_MIDI"
    val tous = listOf(MATIN, APRES_MIDI)
    fun libelle(c: String): String = if (c == APRES_MIDI) "Après-midi" else "Matin"
}

/** Un atelier avec ses épreuves dans l'ordre de passage. */
data class AtelierComplet(val atelier: AtelierEntity, val epreuves: List<EpreuveEntity>)

/** "08:00 – 10:30", "dès 08:00", "jusqu'à 10:30" ou chaîne vide. */
fun AtelierEntity.plageHoraire(): String {
    val d = !heureDebut.isNullOrBlank()
    val f = !heureFin.isNullOrBlank()
    return when {
        d && f -> "$heureDebut – $heureFin"
        d -> "dès $heureDebut"
        f -> "jusqu'à $heureFin"
        else -> ""
    }
}

/** "2027-07-25" -> "25/07/2027" (chaîne vide si absente ou illisible). */
fun formaterDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val p = iso.trim().split("-")
    return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
}

val JourneeEntity.titre: String
    get() = if (dateJournee.isNullOrBlank()) nom else "$nom · ${formaterDate(dateJournee)}"

/** Situation d'un pilote sur une épreuve (écran « Suivi des participants »). */
enum class StatutSuivi(val libelle: String) {
    SAISI("Saisi"), DISQUALIFIE("Disqualifié"), PAS_ENCORE_SAISI("Pas encore saisi")
}

data class LigneSuivi(
    val epreuve: String, val valeurs: String, val points: Double?,
    val statut: StatutSuivi, val compteGeneral: Boolean
)

data class SuiviParticipant(
    val pilote: PiloteEntity, val lignes: List<LigneSuivi>, val totalPoints: Double,
    val rang: Int, val nbPilotes: Int, val epreuvesSaisies: Int, val epreuvesAttendues: Int
)

/** Droits (identiques à la version PC) : seuls l'administrateur et le responsable modifient le planning. */
fun Role.peutModifierPlanning(): Boolean = this == Role.ADMINISTRATEUR || this == Role.RESPONSABLE_EPREUVE

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
