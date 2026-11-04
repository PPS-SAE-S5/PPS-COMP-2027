package com.pps.parapente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pps.parapente.data.Role
import com.pps.parapente.data.UtilisateurEntity
import com.pps.parapente.ui.Bleu
import com.pps.parapente.ui.CarteBloc
import com.pps.parapente.ui.Ecran
import com.pps.parapente.ui.Gris
import com.pps.parapente.ui.Page

private data class Entree(val titre: String, val description: String, val ecran: Ecran)
private data class Section(val titre: String, val entrees: List<Entree>)

@Composable
fun DashboardScreen(utilisateur: UtilisateurEntity, aller: (Ecran) -> Unit, deconnexion: () -> Unit) {
    val role = Role.valueOf(utilisateur.role)

    // Droits d'accès par rôle (identiques à la version PC), rangés en trois sections comme sur PC.
    val gestionnaire = role == Role.ADMINISTRATEUR || role == Role.RESPONSABLE_EPREUVE
    val terrain = gestionnaire || role == Role.BENEVOLE
    val sections = listOf(
        Section("PRÉPARER LA COMPÉTITION", buildList {
            if (gestionnaire)
                add(Entree("Gestion des épreuves", "Créer et paramétrer les épreuves, formules et barèmes", Ecran.Epreuves))
            if (role != Role.PILOTE) // le pilote n'a pas accès au planning ; seuls admin et responsable le modifient
                add(Entree("Planning des journées", "Organiser la compétition en journées, ateliers et épreuves", Ecran.Planning))
            if (terrain)
                add(Entree("Gestion des pilotes", "Inscriptions et informations des pilotes", Ecran.Pilotes))
        }),
        Section("PENDANT LA COMPÉTITION", buildList {
            if (terrain)
                add(Entree("Saisie des résultats", "Saisir les performances par épreuve", Ecran.Saisie))
            add(Entree("Classements", "Classement général et détail par épreuve", Ecran.Classement))
            if (terrain)
                add(Entree("Suivi des participants", "Suivi en détail des scores de chaque pilote", Ecran.Suivi))
        }),
        Section("ADMINISTRATION", buildList {
            if (role == Role.ADMINISTRATEUR)
                add(Entree("Gestion des comptes", "Créer les comptes (responsables, bénévoles...)", Ecran.Utilisateurs))
            if (gestionnaire)
                add(Entree("Synchronisation en ligne", "Envoyer/recevoir les données avec Supabase quand internet est disponible", Ecran.Sync))
        })
    ).filter { it.entrees.isNotEmpty() } // une section sans carte visible n'est pas affichée

    Page(
        titre = "Tableau de bord", onBack = null, logo = true,
        actions = { TextButton(onClick = deconnexion) { Text("Déconnexion", color = Color.White) } }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("${utilisateur.identifiant} (${role.libelle})", color = Gris)
            sections.forEach { section ->
                Text(section.titre, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Gris)
                section.entrees.forEach { e ->
                    CarteBloc(onClick = { aller(e.ecran) }) {
                        Text(e.titre, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Bleu)
                        Text(e.description, color = Gris)
                    }
                }
            }
        }
    }
}
