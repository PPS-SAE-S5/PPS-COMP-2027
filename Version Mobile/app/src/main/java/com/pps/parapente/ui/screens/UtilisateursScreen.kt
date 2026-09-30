package com.pps.parapente.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pps.parapente.data.MessageErreur
import com.pps.parapente.data.Repository
import com.pps.parapente.data.Role
import com.pps.parapente.data.UtilisateurEntity
import com.pps.parapente.ui.BoutonPrincipal
import com.pps.parapente.ui.CarteBloc
import com.pps.parapente.ui.Champ
import com.pps.parapente.ui.Deroulant
import com.pps.parapente.ui.Erreur
import com.pps.parapente.ui.Gris
import com.pps.parapente.ui.Page
import com.pps.parapente.ui.Rouge
import com.pps.parapente.ui.SousTitre
import kotlinx.coroutines.launch

@Composable
fun UtilisateursScreen(repo: Repository, onBack: () -> Unit) {
    var utilisateurs by remember { mutableStateOf<List<UtilisateurEntity>>(emptyList()) }
    var identifiant by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }
    var role by remember { mutableStateOf<Role?>(null) }
    var erreur by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun recharger() { utilisateurs = repo.utilisateurs() }

    LaunchedEffect(Unit) {
        try { recharger() } catch (e: Exception) { erreur = MessageErreur.traduire(e) }
    }

    Page("Gestion des comptes", onBack) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SousTitre("Nouveau compte")
            CarteBloc {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Champ("Identifiant *", identifiant, { identifiant = it })
                    Champ("Mot de passe *", motDePasse, { motDePasse = it }, motDePasse = true)
                    Deroulant("Rôle *", Role.values().toList(), role, { it.libelle }, { role = it })
                    BoutonPrincipal("Créer le compte") {
                        scope.launch {
                            try {
                                repo.creerUtilisateur(identifiant, motDePasse, role)
                                identifiant = ""; motDePasse = ""; role = null; erreur = null
                                recharger()
                            } catch (e: Exception) {
                                erreur = MessageErreur.traduire(e)
                            }
                        }
                    }
                }
            }
            Erreur(erreur)

            SousTitre("Comptes existants")
            utilisateurs.forEach { u ->
                CarteBloc {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(u.identifiant, fontWeight = FontWeight.Bold)
                            Text(Role.valueOf(u.role).libelle, color = Gris)
                        }
                        if (u.identifiant != "admin") {
                            TextButton(onClick = {
                                scope.launch {
                                    try { repo.supprimerUtilisateur(u); erreur = null; recharger() }
                                    catch (e: Exception) { erreur = MessageErreur.traduire(e) }
                                }
                            }) { Text("Supprimer", color = Rouge) }
                        }
                    }
                }
            }
        }
    }
}
