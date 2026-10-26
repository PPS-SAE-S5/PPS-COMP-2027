package com.pps.parapente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pps.parapente.data.AppDatabase
import com.pps.parapente.data.MessageErreur
import com.pps.parapente.sync.RapportSync
import com.pps.parapente.sync.SyncConfig
import com.pps.parapente.sync.SyncService
import com.pps.parapente.ui.BoutonPrincipal
import com.pps.parapente.ui.BoutonSecondaire
import com.pps.parapente.ui.CarteBloc
import com.pps.parapente.ui.Champ
import com.pps.parapente.ui.Erreur
import com.pps.parapente.ui.Gris
import com.pps.parapente.ui.Page
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Configuration et déclenchement de la synchronisation Supabase.
 * L'application reste 100 % utilisable hors ligne : cet écran est entièrement optionnel.
 */
@Composable
fun SyncScreen(db: AppDatabase, config: SyncConfig, onBack: () -> Unit) {
    var url by remember { mutableStateOf(config.url) }
    var cle by remember { mutableStateOf(config.cleAnon) }
    var auto by remember { mutableStateOf(config.syncAuto) }
    var enCours by remember { mutableStateOf(false) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var resultat by remember { mutableStateOf<String?>(null) }
    var derniereSync by remember { mutableStateOf(config.derniereSync) }
    var confirmation by remember { mutableStateOf<Confirmation?>(null) }
    val scope = rememberCoroutineScope()

    fun enregistrerConfig() {
        config.url = url
        config.cleAnon = cle
        config.syncAuto = auto
    }

    /** Exécute une synchronisation en arrière-plan puis affiche le résumé (ou l'erreur). */
    fun lancer(titreSucces: String, tache: suspend () -> RapportSync) {
        enregistrerConfig()
        erreur = null; resultat = null; enCours = true
        scope.launch {
            try {
                val rapport = tache()
                val maintenant = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())
                config.derniereSync = maintenant
                derniereSync = maintenant
                resultat = titreSucces + "\n\n" + rapport.resume()
            } catch (e: Exception) {
                erreur = MessageErreur.traduire(e)
            } finally {
                enCours = false
            }
        }
    }

    confirmation?.let { c ->
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = { Text(c.titre) },
            text = { Text(c.message) },
            confirmButton = { TextButton(onClick = { confirmation = null; c.action() }) { Text("Continuer") } },
            dismissButton = { TextButton(onClick = { confirmation = null }) { Text("Annuler") } }
        )
    }

    Page("Synchronisation en ligne", onBack) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "L'application fonctionne entièrement hors connexion, même sans configurer cet écran. " +
                    "Quand l'appareil retrouve internet, utilisez-le pour recevoir ou envoyer les données " +
                    "avec la base en ligne (Supabase), partagée avec les autres appareils du championnat.",
                color = Gris
            )

            CarteBloc {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Champ("Project URL", url, { url = it })
                    Champ("Clé API publique (anon / publishable)", cle, { cle = it })
                    Row2(
                        checked = auto,
                        onCheckedChange = { auto = it },
                        texte = "Essayer de synchroniser automatiquement au démarrage"
                    )
                }
            }

            if (derniereSync.isNotBlank()) {
                Text("Dernière synchronisation réussie : $derniereSync", color = Gris)
            } else {
                Text("Aucune synchronisation effectuée depuis cet appareil pour le moment.", color = Gris)
            }

            BoutonSecondaire("Enregistrer la configuration") {
                enregistrerConfig()
                erreur = null; resultat = "Configuration enregistrée."
            }

            BoutonSecondaire("Tester la connexion") {
                enregistrerConfig()
                erreur = null; resultat = null; enCours = true
                scope.launch {
                    try {
                        SyncService(db, config).testerConnexion()
                        resultat = "Connexion réussie : la base Supabase est accessible."
                    } catch (e: Exception) {
                        erreur = MessageErreur.traduire(e)
                    } finally {
                        enCours = false
                    }
                }
            }

            Text("Choisissez le sens de la synchronisation (comme git pull / git push) :")

            BoutonPrincipal("↓  Synchroniser : base en ligne → application") {
                confirmation = Confirmation(
                    "Synchroniser : base en ligne → application",
                    "Votre application va devenir IDENTIQUE à la base en ligne.\n\n" +
                        "Les pilotes, épreuves, inscriptions, résultats et comptes utilisateurs (sauf « admin ») " +
                        "qui n'existent pas en ligne seront SUPPRIMÉS de cet appareil.\n\nContinuer ?"
                ) {
                    lancer("Réception terminée : l'application est maintenant identique à la base en ligne.") {
                        SyncService(db, config).recevoirDepuisSupabase()
                    }
                }
            }
            Text(
                "Récupère les données en ligne. Ce qui a été supprimé en ligne est supprimé ici.",
                color = Gris
            )

            BoutonPrincipal("↑  Synchroniser : application → base en ligne") {
                confirmation = Confirmation(
                    "Synchroniser : application → base en ligne",
                    "La base en ligne va devenir IDENTIQUE à votre application.\n\n" +
                        "Les pilotes, épreuves, inscriptions, résultats et comptes utilisateurs (sauf « admin ») " +
                        "qui n'existent pas dans cette application seront SUPPRIMÉS de la base en ligne, " +
                        "donc pour tous les appareils.\n\nContinuer ?"
                ) {
                    lancer("Envoi terminé : la base en ligne est maintenant identique à l'application.") {
                        SyncService(db, config).envoyerVersSupabase()
                    }
                }
            }
            Text(
                "Envoie vos données vers la base en ligne. Ce que vous avez supprimé ici est supprimé en ligne.",
                color = Gris
            )

            BoutonSecondaire("Fusionner les deux bases (sans aucune suppression)") {
                lancer("Synchronisation terminée.") { SyncService(db, config).synchroniser() }
            }

            if (enCours) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }

            Erreur(erreur)
            resultat?.let { Text(it) }

            Text(
                "Remarque : les comptes utilisateurs sont synchronisés comme le reste. Les deux boutons « miroir » " +
                    "suppriment aussi les comptes (sauf « admin ») ; « Fusionner » ne supprime jamais rien.",
                color = Gris
            )
        }
    }
}

/** Demande de confirmation affichée avant une synchronisation qui supprime des données. */
private class Confirmation(val titre: String, val message: String, val action: () -> Unit)

/** Ligne case à cocher + texte (évite d'ajouter une dépendance pour un simple CheckBox+Label). */
@Composable
private fun Row2(checked: Boolean, onCheckedChange: (Boolean) -> Unit, texte: String) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors())
        Text(texte)
    }
}
