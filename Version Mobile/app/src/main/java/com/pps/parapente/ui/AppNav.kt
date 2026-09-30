package com.pps.parapente.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pps.parapente.data.EpreuveComplete
import com.pps.parapente.data.PiloteEntity
import com.pps.parapente.data.Repository
import com.pps.parapente.data.UtilisateurEntity
import com.pps.parapente.ui.screens.ClassementScreen
import com.pps.parapente.ui.screens.DashboardScreen
import com.pps.parapente.ui.screens.EpreuveFormScreen
import com.pps.parapente.ui.screens.EpreuvesScreen
import com.pps.parapente.ui.screens.LoginScreen
import com.pps.parapente.ui.screens.PiloteFormScreen
import com.pps.parapente.ui.screens.PilotesScreen
import com.pps.parapente.ui.screens.SaisieScreen
import com.pps.parapente.ui.screens.UtilisateursScreen

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
}

@Composable
fun PpsApp(repo: Repository) {
    var ecran by remember { mutableStateOf<Ecran>(Ecran.Login) }
    var utilisateur by remember { mutableStateOf<UtilisateurEntity?>(null) }

    // Bouton "retour" du téléphone
    BackHandler(enabled = ecran != Ecran.Login && ecran != Ecran.Dashboard) {
        ecran = when (ecran) {
            is Ecran.PiloteForm -> Ecran.Pilotes
            is Ecran.EpreuveForm -> Ecran.Epreuves
            else -> Ecran.Dashboard
        }
    }

    val u = utilisateur
    if (u == null) {
        LoginScreen(repo) { connecte ->
            utilisateur = connecte
            ecran = Ecran.Dashboard
        }
        return
    }

    val versDashboard = { ecran = Ecran.Dashboard }

    when (val courant = ecran) {
        Ecran.Login -> LoginScreen(repo) { utilisateur = it; ecran = Ecran.Dashboard }
        Ecran.Dashboard -> DashboardScreen(
            utilisateur = u,
            aller = { ecran = it },
            deconnexion = { utilisateur = null; ecran = Ecran.Login }
        )
        Ecran.Pilotes -> PilotesScreen(repo, onBack = versDashboard, onOuvrir = { ecran = Ecran.PiloteForm(it) })
        is Ecran.PiloteForm -> PiloteFormScreen(repo, courant.pilote, onTermine = { ecran = Ecran.Pilotes })
        Ecran.Epreuves -> EpreuvesScreen(repo, onBack = versDashboard, onOuvrir = { ecran = Ecran.EpreuveForm(it) })
        is Ecran.EpreuveForm -> EpreuveFormScreen(repo, courant.epreuve, onTermine = { ecran = Ecran.Epreuves })
        Ecran.Saisie -> SaisieScreen(repo, u, onBack = versDashboard)
        Ecran.Classement -> ClassementScreen(repo, onBack = versDashboard)
        Ecran.Utilisateurs -> UtilisateursScreen(repo, onBack = versDashboard)
    }
}
