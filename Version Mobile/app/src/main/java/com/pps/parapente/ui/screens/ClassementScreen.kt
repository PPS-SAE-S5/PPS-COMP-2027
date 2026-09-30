package com.pps.parapente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pps.parapente.data.EpreuveComplete
import com.pps.parapente.data.LigneClassement
import com.pps.parapente.data.MessageErreur
import com.pps.parapente.data.Repository
import com.pps.parapente.data.ResultatAffiche
import com.pps.parapente.data.fmt
import com.pps.parapente.data.nomComplet
import com.pps.parapente.ui.Bleu
import com.pps.parapente.ui.Cellule
import com.pps.parapente.ui.Deroulant
import com.pps.parapente.ui.Erreur
import com.pps.parapente.ui.Gris
import com.pps.parapente.ui.LigneTableau
import com.pps.parapente.ui.Orange
import com.pps.parapente.ui.Page
import com.pps.parapente.ui.Rouge
import kotlinx.coroutines.launch

/**
 * Deux vues (comme la version PC) :
 *  - Classement général : une colonne par épreuve qui compte au général et dont le classement est visible
 *  - Détail par épreuve : rang, pilote, valeurs saisies, points (épreuves avec "afficher le classement" coché)
 * Accessible à tous les rôles, y compris Pilote et Comité des pilotes.
 */
@Composable
fun ClassementScreen(repo: Repository, onBack: () -> Unit) {
    var onglet by remember { mutableStateOf(0) }
    var epreuves by remember { mutableStateOf<List<EpreuveComplete>>(emptyList()) }
    var general by remember { mutableStateOf<List<LigneClassement>>(emptyList()) }
    var epreuveDetail by remember { mutableStateOf<EpreuveComplete?>(null) }
    var detail by remember { mutableStateOf<List<ResultatAffiche>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun charger() {
        try {
            epreuves = repo.epreuves()
            general = repo.classementGeneral()
            val affichables = epreuves.filter { it.epreuve.afficherClassement }
            val choix = affichables.firstOrNull { it.epreuve.id == epreuveDetail?.epreuve?.id } ?: affichables.firstOrNull()
            epreuveDetail = choix
            detail = if (choix != null) repo.classementEpreuve(choix.epreuve.id) else emptyList()
            erreur = null
        } catch (e: Exception) {
            erreur = MessageErreur.traduire(e)
        }
    }

    LaunchedEffect(Unit) { charger() }

    Page(
        "Classements", onBack,
        actions = { TextButton(onClick = { scope.launch { charger() } }) { Text("Actualiser", color = Color.White) } }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = onglet) {
                Tab(selected = onglet == 0, onClick = { onglet = 0 }, text = { Text("Général") })
                Tab(selected = onglet == 1, onClick = { onglet = 1 }, text = { Text("Détail par épreuve") })
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Erreur(erreur)
                if (onglet == 0) {
                    ClassementGeneral(general, epreuves)
                } else {
                    val affichables = epreuves.filter { it.epreuve.afficherClassement }
                    Deroulant("Épreuve", affichables, epreuveDetail, { it.epreuve.nom }, { choix ->
                        epreuveDetail = choix
                        scope.launch {
                            try { detail = repo.classementEpreuve(choix.epreuve.id) }
                            catch (e: Exception) { erreur = MessageErreur.traduire(e) }
                        }
                    })
                    Text(
                        "Seules les épreuves avec « Afficher le classement » coché apparaissent ici.",
                        color = Gris, fontSize = 11.sp
                    )
                    DetailEpreuve(detail)
                }
            }
        }
    }
}

@Composable
private fun ClassementGeneral(lignes: List<LigneClassement>, epreuves: List<EpreuveComplete>) {
    val colonnes = epreuves.filter { it.epreuve.actif && it.epreuve.compteDansGeneral && it.epreuve.afficherClassement }
    if (lignes.isEmpty()) {
        Text("Aucun pilote inscrit pour le moment.", color = Gris)
        return
    }
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            LigneTableau(entete = true) {
                Cellule("Rang", 48.dp, true, Color.White)
                Cellule("Pilote", 150.dp, true, Color.White)
                Cellule("Caserne", 110.dp, true, Color.White)
                colonnes.forEach { Cellule(it.epreuve.nom, 96.dp, true, Color.White) }
                Cellule("TOTAL", 70.dp, true, Color.White)
            }
            lignes.forEach { l ->
                LigneTableau {
                    Cellule(l.rang.toString(), 48.dp)
                    Cellule(l.pilote.nomComplet, 150.dp, true)
                    Cellule(l.pilote.caserne ?: "", 110.dp)
                    colonnes.forEach { c -> Cellule(fmt(l.pointsParEpreuve[c.epreuve.id] ?: 0.0), 96.dp) }
                    Cellule(fmt(l.total), 70.dp, true, Orange)
                }
            }
        }
    }
}

@Composable
private fun DetailEpreuve(lignes: List<ResultatAffiche>) {
    if (lignes.isEmpty()) {
        Text("Aucun résultat saisi pour cette épreuve.", color = Gris)
        return
    }
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            LigneTableau(entete = true) {
                Cellule("Rang", 48.dp, true, Color.White)
                Cellule("Pilote", 150.dp, true, Color.White)
                Cellule("Caserne", 110.dp, true, Color.White)
                Cellule("Valeurs saisies", 170.dp, true, Color.White)
                Cellule("Points", 64.dp, true, Color.White)
                Cellule("Disq.", 50.dp, true, Color.White)
            }
            lignes.forEachIndexed { index, r ->
                LigneTableau {
                    Cellule((index + 1).toString(), 48.dp)
                    Cellule(r.pilote?.nomComplet ?: "?", 150.dp, true)
                    Cellule(r.pilote?.caserne ?: "", 110.dp)
                    Cellule(r.valeurs.entries.joinToString(", ") { "${it.key}=${fmt(it.value)}" }, 170.dp)
                    Cellule(fmt(r.resultat.points), 64.dp, true, Orange)
                    Cellule(if (r.resultat.disqualifie) "Oui" else "", 50.dp, false, Rouge)
                }
            }
        }
    }
}
