package com.pps.parapente.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pps.parapente.R
import com.pps.parapente.data.MessageErreur
import com.pps.parapente.data.Repository
import com.pps.parapente.data.UtilisateurEntity
import com.pps.parapente.ui.BoutonPrincipal
import com.pps.parapente.ui.Bleu
import com.pps.parapente.ui.CarteBloc
import com.pps.parapente.ui.Champ
import com.pps.parapente.ui.Erreur
import com.pps.parapente.ui.Fond
import com.pps.parapente.ui.Gris
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(repo: Repository, onConnecte: (UtilisateurEntity) -> Unit) {
    var identifiant by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }
    var erreur by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun connexion() {
        if (identifiant.isBlank() || motDePasse.isEmpty()) {
            erreur = "Veuillez saisir votre identifiant et votre mot de passe."
            return
        }
        scope.launch {
            try {
                val u = repo.seConnecter(identifiant.trim(), motDePasse)
                if (u == null) erreur = "Identifiant ou mot de passe incorrect." else onConnecte(u)
            } catch (e: Exception) {
                erreur = MessageErreur.traduire(e)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Fond), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Logo du club : remplacer app/src/main/res/drawable/logo.png (même nom de fichier)
            Image(painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.size(96.dp))
            Text(
                "Championnat de France Pompiers de Parapente",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Bleu, textAlign = TextAlign.Center
            )
            Text("Parapente Pays de Sault - Été 2027", color = Gris)
            CarteBloc {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Champ("Identifiant", identifiant, { identifiant = it })
                    Champ("Mot de passe", motDePasse, { motDePasse = it }, motDePasse = true)
                    Erreur(erreur)
                    BoutonPrincipal("Se connecter") { connexion() }
                }
            }
            Text("Compte par défaut : admin / admin123", color = Gris, fontSize = 11.sp)
        }
    }
}
