package com.pps.parapente.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pps.parapente.data.MessageErreur
import com.pps.parapente.data.PiloteEntity
import com.pps.parapente.data.Repository
import com.pps.parapente.data.StatutSuivi
import com.pps.parapente.data.SuiviParticipant
import com.pps.parapente.data.age
import com.pps.parapente.data.fmt
import com.pps.parapente.data.nomComplet
import com.pps.parapente.ui.Bleu
import com.pps.parapente.ui.CarteBloc
import com.pps.parapente.ui.Champ
import com.pps.parapente.ui.Erreur
import com.pps.parapente.ui.Gris
import com.pps.parapente.ui.Orange
import com.pps.parapente.ui.Page
import com.pps.parapente.ui.Rouge
import com.pps.parapente.ui.SousTitre
import kotlinx.coroutines.launch
import java.text.Normalizer

/** Minuscules et sans accents : « Édouard » est trouvé en tapant « edouard ». */
private fun normaliser(texte: String?): String {
    if (texte == null) return ""
    return Normalizer.normalize(texte, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase().trim()
}

private fun formaterPoints(v: Double): String = fmt(v)

/** Suivi en détail des scores d'un pilote (comme l'écran « Suivi des participants » de la version PC). */
@Composable
fun SuiviScreen(repo: Repository, onBack: () -> Unit) {
    var pilotes by remember { mutableStateOf<List<PiloteEntity>>(emptyList()) }
    var recherche by remember { mutableStateOf("") }
    var selection by remember { mutableStateOf<PiloteEntity?>(null) }
    var suivi by remember { mutableStateOf<SuiviParticipant?>(null) }
    var erreur by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun charger() {
        try {
            pilotes = repo.pilotesTries()
            val p = selection
            suivi = if (p != null) repo.suivi(p) else null
            erreur = null
        } catch (e: Exception) {
            erreur = MessageErreur.traduire(e)
        }
    }

    LaunchedEffect(Unit) { charger() }

    fun fermerDetail() { selection = null; suivi = null }

    // Dans le détail, « retour » revient à la liste des pilotes (et non au tableau de bord)
    BackHandler(enabled = selection != null) { fermerDetail() }

    Page(
        "Suivi des participants",
        onBack = { if (selection != null) fermerDetail() else onBack() },
        actions = { TextButton(onClick = { scope.launch { charger() } }) { Text("Actualiser", color = Color.White) } }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Erreur(erreur)
            val detail = suivi
            if (selection == null || detail == null) {
                Champ("Rechercher un pilote (nom, prénom, licence, caserne)", recherche, { recherche = it })

                val mots = normaliser(recherche).split(Regex("\\s+")).filter { it.isNotEmpty() }
                val filtres = pilotes.filter { p ->
                    val botte = normaliser("${p.nom} ${p.prenom} ${p.prenom} ${p.nom} ${p.numeroLicence} ${p.caserne ?: ""}")
                    mots.all { it in botte }
                }
                val n = filtres.size
                Text(
                    "$n " + (if (n > 1) "pilotes" else "pilote") + (if (n == pilotes.size) "" else " sur ${pilotes.size}"),
                    color = Gris, fontSize = 12.sp
                )
                if (pilotes.isEmpty()) Text("Aucun pilote enregistré pour le moment.", color = Gris)
                else Text("Touchez un pilote pour afficher le détail de ses scores.", color = Gris, fontSize = 12.sp)

                filtres.forEach { p ->
                    CarteBloc(onClick = {
                        scope.launch {
                            try { suivi = repo.suivi(p); selection = p; erreur = null }
                            catch (e: Exception) { erreur = MessageErreur.traduire(e) }
                        }
                    }) {
                        Text(p.nomComplet, fontWeight = FontWeight.Bold)
                        val caserne = p.caserne?.takeIf { it.isNotBlank() }
                        if (caserne != null) Text(caserne, color = Gris, fontSize = 13.sp)
                    }
                }
            } else {
                DetailSuivi(detail)
            }
        }
    }
}

@Composable
private fun DetailSuivi(s: SuiviParticipant) {
    val p = s.pilote
    Text(p.nomComplet, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Bleu)
    val infos = listOfNotNull(
        p.numeroLicence.takeIf { it.isNotBlank() }?.let { "Licence n°$it" },
        p.caserne?.takeIf { it.isNotBlank() },
        p.genre?.takeIf { it.isNotBlank() },
        if (p.anneeNaissance != null) "${p.age} ans" else null
    ).joinToString("   ·   ")
    if (infos.isNotEmpty()) Text(infos, color = Gris, fontSize = 13.sp)

    val rang = if (s.rang == 0) "—" else s.rang.toString() + (if (s.rang == 1) "er" else "e") + " / ${s.nbPilotes}"
    CarteBloc {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LigneStat("Total de points", formaterPoints(s.totalPoints), Orange)
            LigneStat("Rang au classement général", rang, Bleu)
            LigneStat("Épreuves saisies", "${s.epreuvesSaisies} / ${s.epreuvesAttendues}", Bleu)
        }
    }

    SousTitre("Scores par épreuve")
    if (s.lignes.isEmpty()) Text("Ce pilote n'est inscrit à aucune épreuve.", color = Gris)
    s.lignes.forEach { l ->
        CarteBloc {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(l.epreuve, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(
                        if (l.points == null) "—" else formaterPoints(l.points) + " pts",
                        fontWeight = FontWeight.Bold, color = Orange
                    )
                }
                if (l.valeurs.isNotBlank()) Text(l.valeurs, color = Gris, fontSize = 13.sp)
                Text(
                    l.statut.libelle + "   ·   Compte au général : " + (if (l.compteGeneral) "oui" else "non"),
                    fontSize = 12.sp,
                    color = when (l.statut) {
                        StatutSuivi.DISQUALIFIE -> Rouge
                        StatutSuivi.PAS_ENCORE_SAISI -> Gris
                        StatutSuivi.SAISI -> Color.Unspecified
                    },
                    fontWeight = if (l.statut == StatutSuivi.DISQUALIFIE) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
    Text(
        "Un pilote inscrit qui n'a pas encore de résultat compte 0 point pour l'épreuve (règle du classement général).",
        color = Gris, fontSize = 11.sp
    )
}

@Composable
private fun LigneStat(libelle: String, valeur: String, couleur: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(libelle, modifier = Modifier.weight(1f), color = Gris)
        Text(valeur, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = couleur)
    }
}
