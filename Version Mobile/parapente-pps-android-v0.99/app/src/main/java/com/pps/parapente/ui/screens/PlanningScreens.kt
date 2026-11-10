package com.pps.parapente.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pps.parapente.data.AtelierComplet
import com.pps.parapente.data.AtelierEntity
import com.pps.parapente.data.Creneau
import com.pps.parapente.data.EpreuveEntity
import com.pps.parapente.data.JourneeEntity
import com.pps.parapente.data.MessageErreur
import com.pps.parapente.data.Repository
import com.pps.parapente.data.Role
import com.pps.parapente.data.formaterDate
import com.pps.parapente.data.peutModifierPlanning
import com.pps.parapente.data.plageHoraire
import com.pps.parapente.data.titre
import com.pps.parapente.ui.Bleu
import com.pps.parapente.ui.BoutonDanger
import com.pps.parapente.ui.BoutonPrincipal
import com.pps.parapente.ui.BoutonSecondaire
import com.pps.parapente.ui.CarteBloc
import com.pps.parapente.ui.Champ
import com.pps.parapente.ui.Deroulant
import com.pps.parapente.ui.DialogueConfirmation
import com.pps.parapente.ui.Erreur
import com.pps.parapente.ui.Gris
import com.pps.parapente.ui.Orange
import com.pps.parapente.ui.Page
import com.pps.parapente.ui.SousTitre
import kotlinx.coroutines.launch

/** Fenêtre « nouvelle journée » / « modifier la journée ». */
private class DialogueJournee(val existante: JourneeEntity?)

/**
 * Planning de la compétition : journées > créneaux (matin / après-midi) > ateliers > épreuves.
 * Seuls l'administrateur et le responsable de l'épreuve peuvent modifier le planning ;
 * les autres rôles le consultent en lecture seule (mêmes règles que la version PC).
 */
@Composable
fun PlanningScreen(
    repo: Repository, role: Role, onBack: () -> Unit,
    onAtelier: (atelier: AtelierEntity?, journeeId: Int?) -> Unit
) {
    val peutModifier = role.peutModifierPlanning()
    var journees by remember { mutableStateOf<List<JourneeEntity>>(emptyList()) }
    var selectionId by remember { mutableStateOf(0) }
    var ateliers by remember { mutableStateOf<List<AtelierComplet>>(emptyList()) }
    var libres by remember { mutableStateOf<List<EpreuveEntity>>(emptyList()) }
    var totalActives by remember { mutableStateOf(0) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var dialogueJournee by remember { mutableStateOf<DialogueJournee?>(null) }
    var erreurDialogue by remember { mutableStateOf<String?>(null) }
    var atelierASupprimer by remember { mutableStateOf<AtelierEntity?>(null) }
    var journeeASupprimer by remember { mutableStateOf<JourneeEntity?>(null) }
    var nbAteliersJournee by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    suspend fun rafraichir() {
        try {
            val liste = repo.journees()
            journees = liste
            if (liste.none { it.id == selectionId }) selectionId = liste.firstOrNull()?.id ?: 0
            ateliers = if (selectionId != 0) repo.ateliersComplets(selectionId) else emptyList()
            libres = repo.epreuvesNonAffectees()
            totalActives = repo.compterEpreuvesActives()
            erreur = null
        } catch (e: Exception) {
            erreur = MessageErreur.traduire(e)
        }
    }

    LaunchedEffect(Unit) { rafraichir() }

    Page(
        "Planning de la compétition", onBack,
        actions = { TextButton(onClick = { scope.launch { rafraichir() } }) { Text("Actualiser", color = androidx.compose.ui.graphics.Color.White) } }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Erreur(erreur)

            if (peutModifier) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BoutonPrincipal("+ Nouvelle journée", Modifier.weight(1f)) {
                        erreurDialogue = null
                        dialogueJournee = DialogueJournee(null)
                    }
                    BoutonPrincipal("+ Nouvel atelier", Modifier.weight(1f)) {
                        if (journees.isEmpty()) erreur = "Créez d'abord une journée : un atelier doit être rattaché à une journée."
                        else onAtelier(null, if (selectionId == 0) null else selectionId)
                    }
                }
            }

            // Journées (une pastille par journée)
            if (journees.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    journees.forEach { j ->
                        val choisir = { selectionId = j.id; scope.launch { rafraichir() }; Unit }
                        if (j.id == selectionId) {
                            Button(onClick = choisir, colors = ButtonDefaults.buttonColors(containerColor = Bleu)) { Text(j.titre) }
                        } else {
                            OutlinedButton(onClick = choisir) { Text(j.titre) }
                        }
                    }
                }
            }

            val courante = journees.firstOrNull { it.id == selectionId }
            if (courante == null) {
                Text(
                    if (peutModifier) "Aucune journée pour le moment. Touchez « + Nouvelle journée » pour commencer à organiser la compétition."
                    else "Le planning n'a pas encore été créé.",
                    color = Gris
                )
            } else {
                SousTitre(courante.titre)
                if (peutModifier) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BoutonSecondaire("Renommer / date", Modifier.weight(1f)) {
                            erreurDialogue = null
                            dialogueJournee = DialogueJournee(courante)
                        }
                        BoutonDanger("Supprimer la journée", Modifier.weight(1f)) {
                            scope.launch {
                                try {
                                    nbAteliersJournee = repo.compterAteliers(courante.id)
                                    journeeASupprimer = courante
                                } catch (e: Exception) { erreur = MessageErreur.traduire(e) }
                            }
                        }
                    }
                }

                Creneau.tous.forEach { creneau ->
                    val duCreneau = ateliers.filter { it.atelier.creneau == creneau }
                    val compte = when (duCreneau.size) {
                        0 -> "aucun atelier"
                        1 -> "1 atelier"
                        else -> "${duCreneau.size} ateliers"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            Creneau.libelle(creneau), fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            color = if (creneau == Creneau.MATIN) Orange else Bleu
                        )
                        Text("   ·   $compte", color = Gris)
                    }
                    if (duCreneau.isEmpty()) Text("Aucun atelier sur ce créneau.", color = Gris, fontSize = 13.sp)
                    duCreneau.forEach { ac ->
                        CarteBloc {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(ac.atelier.nom, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                val meta = listOfNotNull(
                                    ac.atelier.plageHoraire().ifBlank { null },
                                    ac.atelier.lieu?.ifBlank { null },
                                    ac.atelier.responsable?.ifBlank { null }?.let { "Resp. $it" }
                                ).joinToString("   ·   ")
                                if (meta.isNotEmpty()) Text(meta, color = Gris, fontSize = 13.sp)
                                if (ac.epreuves.isEmpty()) {
                                    Text("Aucune épreuve dans cet atelier.", color = Gris, fontSize = 13.sp)
                                } else {
                                    ac.epreuves.forEachIndexed { i, e -> Text("${i + 1}.  ${e.nom}") }
                                }
                                if (peutModifier) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        BoutonSecondaire("Modifier", Modifier.weight(1f)) { onAtelier(ac.atelier, null) }
                                        BoutonDanger("Supprimer", Modifier.weight(1f)) { atelierASupprimer = ac.atelier }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Épreuves qui ne sont encore dans aucun atelier
            SousTitre("Épreuves à placer")
            Text(
                "$totalActives " + (if (totalActives > 1) "épreuves actives" else "épreuve active") + " · ${libres.size} à placer",
                color = Gris, fontSize = 13.sp
            )
            if (libres.isEmpty()) {
                Text("Toutes les épreuves sont placées.", color = Gris)
            } else {
                libres.forEach { Text("•  ${it.nom}") }
                if (peutModifier) Text("Ouvrez ou créez un atelier pour les y ajouter.", color = Gris, fontSize = 12.sp)
            }
        }
    }

    // ----- fenêtres -----
    dialogueJournee?.let { d ->
        DialogueJourneeUi(
            existante = d.existante, suggestion = "Journée ${journees.size + 1}", erreur = erreurDialogue,
            onValider = { nom, date ->
                scope.launch {
                    try {
                        if (d.existante == null) selectionId = repo.creerJournee(nom, date).id
                        else repo.modifierJournee(d.existante, nom, date)
                        dialogueJournee = null
                        erreurDialogue = null
                        rafraichir()
                    } catch (e: Exception) {
                        erreurDialogue = MessageErreur.traduire(e)
                    }
                }
            },
            onAnnuler = { dialogueJournee = null; erreurDialogue = null }
        )
    }

    journeeASupprimer?.let { j ->
        DialogueConfirmation(
            message = if (nbAteliersJournee == 0) "Supprimer la journée « ${j.nom} » ?"
            else "Supprimer la journée « ${j.nom} » et ses $nbAteliersJournee atelier(s) ?\n" +
                "Les épreuves ne sont pas supprimées : elles redeviendront « à placer ».",
            onConfirmer = {
                journeeASupprimer = null
                scope.launch {
                    try { repo.supprimerJournee(j.id); selectionId = 0; rafraichir() }
                    catch (e: Exception) { erreur = MessageErreur.traduire(e) }
                }
            },
            onAnnuler = { journeeASupprimer = null }
        )
    }

    atelierASupprimer?.let { a ->
        DialogueConfirmation(
            message = "Supprimer l'atelier « ${a.nom} » ?\nSes épreuves ne sont pas supprimées : elles redeviendront « à placer ».",
            onConfirmer = {
                atelierASupprimer = null
                scope.launch {
                    try { repo.supprimerAtelier(a.id); rafraichir() }
                    catch (e: Exception) { erreur = MessageErreur.traduire(e) }
                }
            },
            onAnnuler = { atelierASupprimer = null }
        )
    }
}

@Composable
private fun DialogueJourneeUi(
    existante: JourneeEntity?, suggestion: String, erreur: String?,
    onValider: (nom: String, date: String) -> Unit, onAnnuler: () -> Unit
) {
    var nom by remember { mutableStateOf(existante?.nom ?: suggestion) }
    var date by remember { mutableStateOf(formaterDate(existante?.dateJournee)) }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text(if (existante == null) "Nouvelle journée" else "Modifier la journée") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Champ("Nom *", nom, { nom = it })
                Champ("Date (jj/mm/aaaa, facultatif)", date, { date = it })
                Erreur(erreur)
            }
        },
        confirmButton = { TextButton(onClick = { onValider(nom, date) }) { Text("Enregistrer") } },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

/**
 * Formulaire d'un atelier : informations (journée, créneau, horaires, lieu, responsable) + liste ORDONNÉE des
 * épreuves qu'il regroupe (l'ordre de la liste est l'ordre de passage).
 */
@Composable
fun AtelierFormScreen(repo: Repository, existant: AtelierEntity?, journeeParDefaut: Int?, onTermine: () -> Unit) {
    var nom by remember { mutableStateOf(existant?.nom ?: "") }
    var journees by remember { mutableStateOf<List<JourneeEntity>>(emptyList()) }
    var journeeId by remember { mutableStateOf(existant?.journeeId ?: journeeParDefaut ?: 0) }
    var creneau by remember { mutableStateOf(existant?.creneau ?: Creneau.MATIN) }
    var debut by remember { mutableStateOf(existant?.heureDebut ?: "") }
    var fin by remember { mutableStateOf(existant?.heureFin ?: "") }
    var lieu by remember { mutableStateOf(existant?.lieu ?: "") }
    var responsable by remember { mutableStateOf(existant?.responsable ?: "") }
    val epreuves = remember { mutableStateListOf<EpreuveEntity>() }
    var disponibles by remember { mutableStateOf<List<EpreuveEntity>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            journees = repo.journees()
            disponibles = repo.epreuvesDisponibles(existant?.id ?: 0)
            if (existant != null) {
                epreuves.clear()
                epreuves.addAll(repo.epreuvesDeAtelier(existant.id))
            }
        } catch (e: Exception) {
            erreur = MessageErreur.traduire(e)
        }
    }

    // On ne propose que les épreuves libres, et pas celles déjà dans la liste
    val proposees = disponibles.filter { d -> epreuves.none { it.id == d.id } }

    fun enregistrer() {
        scope.launch {
            try {
                val base = existant ?: AtelierEntity(journeeId = journeeId, nom = "")
                repo.sauverAtelier(
                    base.copy(
                        journeeId = journeeId, nom = nom, creneau = creneau,
                        heureDebut = debut, heureFin = fin, lieu = lieu, responsable = responsable
                    ),
                    epreuves.toList()
                )
                onTermine()
            } catch (e: Exception) {
                erreur = MessageErreur.traduire(e)
            }
        }
    }

    Page(if (existant == null) "Nouvel atelier" else "Modifier l'atelier", onBack = onTermine) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Un atelier regroupe plusieurs épreuves sur un créneau d'une journée de compétition.",
                color = Gris, fontSize = 12.sp
            )
            Champ("Nom de l'atelier * (ex : Atelier Atterrissage)", nom, { nom = it })
            Deroulant("Journée *", journees, journees.firstOrNull { it.id == journeeId }, { it.titre }, { journeeId = it.id })

            SousTitre("Créneau *")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Creneau.tous.forEach { c ->
                    RadioButton(selected = creneau == c, onClick = { creneau = c })
                    Text(Creneau.libelle(c), modifier = Modifier.padding(end = 16.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Champ("Début (HH:mm)", debut, { debut = it }, modifier = Modifier.weight(1f))
                Champ("Fin (HH:mm)", fin, { fin = it }, modifier = Modifier.weight(1f))
            }
            Champ("Lieu (ex : Zone d'atterrissage)", lieu, { lieu = it })
            Champ("Responsable (ex : Dupont)", responsable, { responsable = it })

            SousTitre("Épreuves de l'atelier")
            Text(
                "L'ordre de la liste est l'ordre de passage. Une épreuve ne peut appartenir qu'à un seul atelier.",
                color = Gris, fontSize = 12.sp
            )
            if (epreuves.isEmpty()) Text("Aucune épreuve dans cet atelier pour le moment.", color = Gris)
            epreuves.forEachIndexed { i, e ->
                CarteBloc {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 1}.  ${e.nom}", modifier = Modifier.weight(1f))
                        TextButton(onClick = { if (i > 0) epreuves.add(i - 1, epreuves.removeAt(i)) }) { Text("▲") }
                        TextButton(onClick = { if (i < epreuves.size - 1) epreuves.add(i + 1, epreuves.removeAt(i)) }) { Text("▼") }
                        TextButton(onClick = { epreuves.removeAt(i) }) { Text("✕") }
                    }
                }
            }
            if (proposees.isEmpty()) {
                Text("Toutes les épreuves actives sont déjà placées dans un atelier.", color = Gris, fontSize = 13.sp)
            } else {
                Deroulant("Ajouter une épreuve", proposees, null, { it.nom }, { epreuves.add(it) })
            }

            Erreur(erreur)
            BoutonPrincipal("Enregistrer l'atelier") { enregistrer() }
            BoutonSecondaire("Annuler", onClick = onTermine)
        }
    }
}
