package com.pps.parapente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pps.parapente.data.ErreurMetier
import com.pps.parapente.data.MessageErreur
import com.pps.parapente.data.PiloteEntity
import com.pps.parapente.data.Repository
import com.pps.parapente.data.nomComplet
import com.pps.parapente.ui.BoutonDanger
import com.pps.parapente.ui.BoutonPrincipal
import com.pps.parapente.ui.BoutonSecondaire
import com.pps.parapente.ui.CarteBloc
import com.pps.parapente.ui.Champ
import com.pps.parapente.ui.DialogueConfirmation
import com.pps.parapente.ui.Erreur
import com.pps.parapente.ui.Gris
import com.pps.parapente.ui.Page
import kotlinx.coroutines.launch

@Composable
fun PilotesScreen(repo: Repository, onBack: () -> Unit, onOuvrir: (PiloteEntity?) -> Unit) {
    var pilotes by remember { mutableStateOf<List<PiloteEntity>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try { pilotes = repo.pilotes() } catch (e: Exception) { erreur = MessageErreur.traduire(e) }
    }

    Page("Gestion des pilotes", onBack) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BoutonPrincipal("+ Nouveau pilote") { onOuvrir(null) }
            Erreur(erreur)
            if (pilotes.isEmpty()) Text("Aucun pilote inscrit pour le moment.", color = Gris)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pilotes, key = { it.id }) { p ->
                    CarteBloc(onClick = { onOuvrir(p) }) {
                        Text(p.nomComplet, fontWeight = FontWeight.Bold)
                        Text("Licence n°${p.numeroLicence} • ${p.caserne ?: "caserne non renseignée"}", color = Gris)
                        Text(
                            listOfNotNull(p.categorie, p.anneeNaissance?.toString(), p.poids?.let { "$it kg" })
                                .joinToString(" • "),
                            color = Gris
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PiloteFormScreen(repo: Repository, pilote: PiloteEntity?, onTermine: () -> Unit) {
    var licence by remember { mutableStateOf(pilote?.numeroLicence ?: "") }
    var nom by remember { mutableStateOf(pilote?.nom ?: "") }
    var prenom by remember { mutableStateOf(pilote?.prenom ?: "") }
    var caserne by remember { mutableStateOf(pilote?.caserne ?: "") }
    var poids by remember { mutableStateOf(pilote?.poids?.toString() ?: "") }
    var email by remember { mutableStateOf(pilote?.email ?: "") }
    var annee by remember { mutableStateOf(pilote?.anneeNaissance?.toString() ?: "") }
    var categorie by remember { mutableStateOf(pilote?.categorie ?: "") }
    var erreur by remember { mutableStateOf<String?>(null) }
    var confirmerSuppression by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun enregistrer() {
        scope.launch {
            try {
                val poidsNombre = if (poids.isBlank()) null
                else poids.trim().replace(',', '.').toDoubleOrNull()
                    ?: throw ErreurMetier("Le poids doit être un nombre.")
                val anneeNombre = if (annee.isBlank()) null
                else annee.trim().toIntOrNull()
                    ?: throw ErreurMetier("L'année de naissance doit être un nombre entier.")
                val p = PiloteEntity(
                    id = pilote?.id ?: 0,
                    numeroLicence = licence.trim(), nom = nom.trim(), prenom = prenom.trim(),
                    caserne = caserne.trim().ifBlank { null }, poids = poidsNombre,
                    email = email.trim().ifBlank { null }, anneeNaissance = anneeNombre,
                    categorie = categorie.trim().ifBlank { null }
                )
                if (pilote == null) repo.creerPilote(p) else repo.majPilote(p)
                onTermine()
            } catch (e: Exception) {
                erreur = MessageErreur.traduire(e)
            }
        }
    }

    Page(if (pilote == null) "Nouveau pilote" else "Fiche pilote", onBack = onTermine) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Champ("N° de licence *", licence, { licence = it })
            Champ("Nom *", nom, { nom = it })
            Champ("Prénom *", prenom, { prenom = it })
            Champ("Caserne", caserne, { caserne = it })
            Champ("Poids (kg)", poids, { poids = it }, numerique = true)
            Champ("Email", email, { email = it })
            Champ("Année de naissance", annee, { annee = it }, numerique = true)
            Champ("Catégorie", categorie, { categorie = it })
            Row(
                Modifier.padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Espoir", "Senior", "Vétéran", "Féminine").forEach { c ->
                    AssistChip(onClick = { categorie = c }, label = { Text(c) })
                }
            }
            Erreur(erreur)
            BoutonPrincipal(if (pilote == null) "Ajouter le pilote" else "Enregistrer les modifications") { enregistrer() }
            if (pilote != null) BoutonDanger("Supprimer ce pilote") { confirmerSuppression = true }
            BoutonSecondaire("Annuler", onClick = onTermine)
        }
    }

    if (confirmerSuppression && pilote != null) {
        DialogueConfirmation(
            message = "Supprimer définitivement ${pilote.nomComplet} ainsi que tous ses résultats ?",
            onConfirmer = {
                confirmerSuppression = false
                scope.launch {
                    try { repo.supprimerPilote(pilote.id); onTermine() }
                    catch (e: Exception) { erreur = MessageErreur.traduire(e) }
                }
            },
            onAnnuler = { confirmerSuppression = false }
        )
    }
}
