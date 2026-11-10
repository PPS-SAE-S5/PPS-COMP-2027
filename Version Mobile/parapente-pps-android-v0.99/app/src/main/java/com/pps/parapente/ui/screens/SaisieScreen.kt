package com.pps.parapente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pps.parapente.data.EpreuveComplete
import com.pps.parapente.data.MessageErreur
import com.pps.parapente.data.Mode
import com.pps.parapente.data.PiloteEntity
import com.pps.parapente.data.Repository
import com.pps.parapente.data.ResultatAffiche
import com.pps.parapente.data.UtilisateurEntity
import com.pps.parapente.data.fmt
import com.pps.parapente.data.fmtSaisie
import com.pps.parapente.data.libelle
import com.pps.parapente.data.nomComplet
import com.pps.parapente.data.valeurs
import com.pps.parapente.ui.BoutonPrincipal
import com.pps.parapente.ui.CarteBloc
import com.pps.parapente.ui.Champ
import com.pps.parapente.ui.Deroulant
import com.pps.parapente.ui.Erreur
import com.pps.parapente.ui.Gris
import com.pps.parapente.ui.Info
import com.pps.parapente.ui.Orange
import com.pps.parapente.ui.Page
import com.pps.parapente.ui.Rouge
import com.pps.parapente.ui.SousTitre
import kotlinx.coroutines.launch

/**
 * Saisie des résultats : les champs à remplir sont générés dynamiquement à partir des variables
 * définies pour l'épreuve choisie (aucun champ n'est codé en dur).
 */
@Composable
fun SaisieScreen(repo: Repository, utilisateur: UtilisateurEntity, onBack: () -> Unit) {
    var epreuves by remember { mutableStateOf<List<EpreuveComplete>>(emptyList()) }
    var epreuve by remember { mutableStateOf<EpreuveComplete?>(null) }
    var pilotes by remember { mutableStateOf<List<PiloteEntity>>(emptyList()) }
    var pilote by remember { mutableStateOf<PiloteEntity?>(null) }
    val champs = remember { mutableStateMapOf<String, String>() }
    var disqualifie by remember { mutableStateOf(false) }
    var resultats by remember { mutableStateOf<List<ResultatAffiche>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try { epreuves = repo.epreuves().filter { it.epreuve.actif } }
        catch (e: Exception) { erreur = MessageErreur.traduire(e) }
    }

    fun choisirEpreuve(ep: EpreuveComplete) {
        epreuve = ep; pilote = null; champs.clear(); disqualifie = false; erreur = null; info = null
        scope.launch {
            try {
                pilotes = repo.pilotesInscrits(ep.epreuve.id)
                resultats = repo.classementEpreuve(ep.epreuve.id)
            } catch (e: Exception) { erreur = MessageErreur.traduire(e) }
        }
    }

    fun choisirPilote(p: PiloteEntity) {
        val ep = epreuve ?: return
        pilote = p; erreur = null; info = null
        scope.launch {
            try {
                val existant = repo.resultat(ep.epreuve.id, p.id)
                champs.clear()
                disqualifie = existant?.disqualifie ?: false
                existant?.valeurs()?.forEach { (nom, valeur) -> champs[nom] = fmtSaisie(valeur) }
            } catch (e: Exception) { erreur = MessageErreur.traduire(e) }
        }
    }

    fun enregistrer() {
        erreur = null; info = null
        val ep = epreuve
        val p = pilote
        if (ep == null || p == null) { erreur = "Choisissez une épreuve et un pilote."; return }

        val valeurs = linkedMapOf<String, Double>()
        for (param in ep.parametres) {
            val texte = champs[param.nomVariable]?.trim().orEmpty()
            if (texte.isEmpty()) continue
            val nombre = texte.replace(',', '.').toDoubleOrNull()
            if (nombre == null) { erreur = "Toutes les valeurs saisies doivent être numériques."; return }
            valeurs[param.nomVariable] = nombre
        }

        scope.launch {
            try {
                if (ep.epreuve.modeCalcul == Mode.FORMULE)
                    repo.calculerFormule(ep, p, valeurs, disqualifie, utilisateur.identifiant)
                else
                    repo.enregistrerBareme(ep, p, valeurs, disqualifie, utilisateur.identifiant)
                info = "Résultat enregistré pour ${p.nomComplet}."
                resultats = repo.classementEpreuve(ep.epreuve.id)
            } catch (e: Exception) {
                erreur = MessageErreur.traduire(e)
            }
        }
    }

    Page("Saisie des résultats", onBack) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Deroulant("Épreuve", epreuves, epreuve, { it.epreuve.nom }, { choisirEpreuve(it) })
            Deroulant("Pilote", pilotes, pilote, { it.libelle() }, { choisirPilote(it) })

            val ep = epreuve
            if (ep != null) {
                SousTitre("Saisie de la performance")
                CarteBloc {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (ep.parametres.isEmpty()) Text("Aucune variable définie pour cette épreuve.", color = Gris)
                        ep.parametres.forEach { param ->
                            val libelle = param.label + (param.unite?.let { " ($it)" } ?: "")
                            Champ(
                                libelle, champs[param.nomVariable] ?: "",
                                { champs[param.nomVariable] = it }, numerique = true
                            )
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = disqualifie, onCheckedChange = { disqualifie = it })
                            Text("Disqualifié / problème de sécurité (0 point automatique)", fontSize = 13.sp)
                        }
                        Erreur(erreur)
                        Info(info)
                        BoutonPrincipal("Enregistrer le résultat") { enregistrer() }
                    }
                }

                SousTitre("Résultats déjà saisis pour cette épreuve")
                if (resultats.isEmpty()) Text("Aucun résultat saisi pour le moment.", color = Gris)
                resultats.forEachIndexed { index, r ->
                    CarteBloc {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${index + 1}. ${r.pilote?.nomComplet ?: "?"}", fontWeight = FontWeight.Bold)
                                Text(
                                    r.valeurs.entries.joinToString(", ") { "${it.key}=${fmt(it.value)}" },
                                    color = Gris, fontSize = 12.sp
                                )
                                if (r.resultat.disqualifie) Text("Disqualifié", color = Rouge, fontSize = 12.sp)
                            }
                            Text("${fmt(r.resultat.points)} pts", fontWeight = FontWeight.Bold, color = Orange)
                        }
                    }
                }
            } else {
                Erreur(erreur)
            }
        }
    }
}
