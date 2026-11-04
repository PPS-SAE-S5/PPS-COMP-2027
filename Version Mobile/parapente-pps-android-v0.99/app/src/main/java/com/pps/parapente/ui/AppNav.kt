package com.pps.parapente.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pps.parapente.data.AppDatabase
import com.pps.parapente.data.AtelierEntity
import com.pps.parapente.data.EpreuveComplete
import com.pps.parapente.data.PiloteEntity
import com.pps.parapente.data.Repository
import com.pps.parapente.data.Role
import com.pps.parapente.data.UtilisateurEntity
import com.pps.parapente.sync.SyncConfig
import com.pps.parapente.sync.SyncService
import com.pps.parapente.ui.screens.AtelierFormScreen
import com.pps.parapente.ui.screens.ClassementScreen
import com.pps.parapente.ui.screens.DashboardScreen
import com.pps.parapente.ui.screens.EpreuveFormScreen
import com.pps.parapente.ui.screens.EpreuvesScreen
import com.pps.parapente.ui.screens.LoginScreen
import com.pps.parapente.ui.screens.PiloteFormScreen
import com.pps.parapente.ui.screens.PilotesScreen
import com.pps.parapente.ui.screens.PlanningScreen
import com.pps.parapente.ui.screens.SaisieScreen
import com.pps.parapente.ui.screens.SuiviScreen
import com.pps.parapente.ui.screens.SyncScreen
import com.pps.parapente.ui.screens.UtilisateursScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tous les écrans de l'application (équivalent des fichiers FXML de la version PC). */
sealed interface Ecran {
    data object Login : Ecran
    data object Dashboard : Ecran
    data object Pilotes : Ecran
    data class PiloteForm(val pilote: PiloteEntity?) : Ecran
    data object Epreuves : Ecran
    data class EpreuveForm(val epreuve: EpreuveComplete?) : Ecran
    data object Saisie : Ecran
    data object Classement : Ecran
    data object Utilisateurs : Ecran
    data object Sync : Ecran
    data object Planning : Ecran
    data class AtelierForm(val atelier: AtelierEntity?, val journeeId: Int?) : Ecran
    data object Suivi : Ecran
}

@Composable
fun PpsApp(repo: Repository, db: AppDatabase, syncConfig: SyncConfig) {
    var ecran by remember { mutableStateOf<Ecran>(Ecran.Login) }
    var utilisateur by remember { mutableStateOf<UtilisateurEntity?>(null) }

    // Synchronisation automatique en arrière-plan au démarrage, si configurée et activée.
    // RÉCEPTION SEULE : on récupère les nouveautés sans rien envoyer ni supprimer. Un envoi automatique à
    // l'ouverture de l'appli pouvait être coupé net par une perte de réseau (variables/barèmes d'une épreuve
    // supprimés puis pas réécrits) et faire réapparaître des données supprimées par ailleurs.
    // Entièrement silencieuse : n'empêche jamais l'utilisation hors ligne de l'application.
    LaunchedEffect(Unit) {
        if (syncConfig.estConfigure() && syncConfig.syncAuto) {
            try {
                SyncService(db, syncConfig).recevoirNouveautes()
                syncConfig.derniereSync = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())
            } catch (ignoree: Exception) {
                // Échec silencieux (pas d'internet, Supabase indisponible...) : la synchronisation
                // manuelle reste disponible depuis l'écran dédié.
            }
        }
    }

    // Bouton "retour" du téléphone
    BackHandler(enabled = ecran != Ecran.Login && ecran != Ecran.Dashboard) {
        if (ecran == Ecran.Classement && utilisateur?.role == Role.PILOTE.name) {
            // Le pilote n'a pas de tableau de bord : « retour » le déconnecte (comme sur PC)
            utilisateur = null
            ecran = Ecran.Login
        } else {
            ecran = when (ecran) {
                is Ecran.PiloteForm -> Ecran.Pilotes
                is Ecran.EpreuveForm -> Ecran.Epreuves
                is Ecran.AtelierForm -> Ecran.Planning
                else -> Ecran.Dashboard
            }
        }
    }

    // Après connexion : le pilote arrive directement sur le classement, les autres rôles sur le tableau de bord
    fun apresConnexion(connecte: UtilisateurEntity) {
        utilisateur = connecte
        ecran = if (connecte.role == Role.PILOTE.name) Ecran.Classement else Ecran.Dashboard
    }

    val u = utilisateur
    if (u == null) {
        LoginScreen(repo) { connecte -> apresConnexion(connecte) }
        return
    }

    val versDashboard = { ecran = Ecran.Dashboard }
    val deconnexion = { utilisateur = null; ecran = Ecran.Login }

    when (val courant = ecran) {
        Ecran.Login -> LoginScreen(repo) { apresConnexion(it) }
        Ecran.Dashboard -> DashboardScreen(
            utilisateur = u,
            aller = { ecran = it },
            deconnexion = deconnexion
        )
        Ecran.Pilotes -> PilotesScreen(repo, onBack = versDashboard, onOuvrir = { ecran = Ecran.PiloteForm(it) })
        is Ecran.PiloteForm -> PiloteFormScreen(repo, courant.pilote, onTermine = { ecran = Ecran.Pilotes })
        Ecran.Epreuves -> EpreuvesScreen(repo, onBack = versDashboard, onOuvrir = { ecran = Ecran.EpreuveForm(it) })
        is Ecran.EpreuveForm -> EpreuveFormScreen(repo, courant.epreuve, onTermine = { ecran = Ecran.Epreuves })
        Ecran.Saisie -> SaisieScreen(repo, u, onBack = versDashboard)
        Ecran.Classement -> ClassementScreen(
            repo,
            onBack = if (u.role == Role.PILOTE.name) deconnexion else versDashboard
        )
        Ecran.Utilisateurs -> UtilisateursScreen(repo, onBack = versDashboard)
        Ecran.Sync -> SyncScreen(db, syncConfig, onBack = versDashboard)
        Ecran.Planning -> PlanningScreen(
            repo, Role.valueOf(u.role), onBack = versDashboard,
            onAtelier = { atelier, journeeId -> ecran = Ecran.AtelierForm(atelier, journeeId) }
        )
        is Ecran.AtelierForm -> AtelierFormScreen(repo, courant.atelier, courant.journeeId, onTermine = { ecran = Ecran.Planning })
        Ecran.Suivi -> SuiviScreen(repo, onBack = versDashboard)
    }
}
