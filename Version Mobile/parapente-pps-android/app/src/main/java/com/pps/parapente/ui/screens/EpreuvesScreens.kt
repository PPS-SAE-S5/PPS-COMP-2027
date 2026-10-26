package com.pps.parapente.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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
import com.pps.parapente.data.BaremeEntity
import com.pps.parapente.data.EpreuveComplete
import com.pps.parapente.data.EpreuveEntity
import com.pps.parapente.data.ErreurMetier
import com.pps.parapente.data.MessageErreur
import com.pps.parapente.data.Mode
import com.pps.parapente.data.ParametreEntity
import com.pps.parapente.data.Repository
import com.pps.parapente.data.fmtSaisie
import com.pps.parapente.ui.BoutonDanger
import com.pps.parapente.ui.BoutonPrincipal
import com.pps.parapente.ui.BoutonSecondaire
import com.pps.parapente.ui.CarteBloc
import com.pps.parapente.ui.Champ
import com.pps.parapente.ui.Deroulant
import com.pps.parapente.ui.DialogueConfirmation
import com.pps.parapente.ui.Erreur
import com.pps.parapente.ui.Gris
import com.pps.parapente.ui.Page
import com.pps.parapente.ui.SousTitre
import kotlinx.coroutines.launch

@Composable
fun EpreuvesScreen(repo: Repository, onBack: () -> Unit, onOuvrir: (EpreuveComplete?) -> Unit) {
    var epreuves by remember { mutableStateOf<List<EpreuveComplete>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try { epreuves = repo.epreuves() } catch (e: Exception) { erreur = MessageErreur.traduire(e) }
    }

    Page("Gestion des épreuves", onBack) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BoutonPrincipal("+ Nouvelle épreuve") { onOuvrir(null) }
            Erreur(erreur)
            if (epreuves.isEmpty()) Text("Aucune épreuve créée pour le moment.", color = Gris)
            epreuves.forEach { ec ->
                val e = ec.epreuve
                CarteBloc(onClick = { onOuvrir(ec) }) {
                    Text(e.nom, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (e.modeCalcul == Mode.FORMULE) {
                        Text("Formule : ${e.formule ?: ""}", color = Gris)
                    } else {
                        Text("Barème : classement sur « ${e.valeurCle ?: ""} » (${e.sensClassement})", color = Gris)
                    }
                    Text(
                        "Classement visible : ${if (e.afficherClassement) "oui" else "non"} • " +
                            "Compte au général : ${if (e.compteDansGeneral) "oui" else "non"} • " +
                            "Active : ${if (e.actif) "oui" else "non"}",
                        color = Gris, fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private data class ParamUi(val nom: String, val label: String, val unite: String)
private data class BaremeUi(val rang: String, val points: String)

private fun nettoyerNom(saisie: String) = saisie.trim().replace(Regex("\\s+"), "")

@Composable
fun EpreuveFormScreen(repo: Repository, existante: EpreuveComplete?, onTermine: () -> Unit) {
    val e = existante?.epreuve
    var nom by remember { mutableStateOf(e?.nom ?: "") }
    var description by remember { mutableStateOf(e?.description ?: "") }
    var mode by remember { mutableStateOf(e?.modeCalcul ?: Mode.FORMULE) }
    var formule by remember { mutableStateOf(e?.formule ?: "") }
    var valeurCle by remember { mutableStateOf(e?.valeurCle ?: "") }
    var sens by remember { mutableStateOf(e?.sensClassement ?: "ASC") }
    var afficher by remember { mutableStateOf(e?.afficherClassement ?: true) }
    var compte by remember { mutableStateOf(e?.compteDansGeneral ?: true) }
    var actif by remember { mutableStateOf(e?.actif ?: true) }
    val parametres = remember {
        mutableStateListOf<ParamUi>().apply {
            existante?.parametres?.forEach { add(ParamUi(it.nomVariable, it.label, it.unite ?: "")) }
        }
    }
    val bareme = remember {
        mutableStateListOf<BaremeUi>().apply {
            existante?.bareme?.forEach { add(BaremeUi(it.rang.toString(), fmtSaisie(it.points))) }
        }
    }
    var erreur by remember { mutableStateOf<String?>(null) }
    var confirmerSuppression by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Liste des variables disponibles pour le classement : toujours à jour dès qu'un nom change
    val nomsVariables = parametres.map { nettoyerNom(it.nom) }.filter { it.isNotBlank() }

    fun enregistrer() {
        scope.launch {
            try {
                val noms = parametres.map { nettoyerNom(it.nom) }
                if (noms.any { it.isBlank() }) throw ErreurMetier("Chaque variable doit avoir un nom (sans espace).")
                if (noms.toSet().size != noms.size) throw ErreurMetier("Deux variables portent le même nom.")
                if (mode == Mode.BAREME && (valeurCle.isBlank() || valeurCle !in noms))
                    throw ErreurMetier("Choisissez la variable de classement parmi les variables définies.")

                val lignesBareme = bareme.map { b ->
                    val rang = b.rang.trim().toIntOrNull()
                    val points = b.points.trim().replace(',', '.').toDoubleOrNull()
                    if (rang == null || points == null)
                        throw ErreurMetier("Le barème contient une valeur non numérique (rang entier, points numériques).")
                    BaremeEntity(epreuveId = 0, rang = rang, points = points)
                }
                if (lignesBareme.map { it.rang }.toSet().size != lignesBareme.size)
                    throw ErreurMetier("Le barème contient deux fois le même rang.")

                val params = parametres.mapIndexed { i, p ->
                    ParametreEntity(
                        epreuveId = 0, nomVariable = nettoyerNom(p.nom),
                        label = p.label.ifBlank { nettoyerNom(p.nom) },
                        unite = p.unite.trim().ifBlank { null }, ordre = i
                    )
                }
                val entite = EpreuveEntity(
                    id = e?.id ?: 0, nom = nom.trim(), description = description.trim().ifBlank { null },
                    modeCalcul = mode, formule = formule.trim().ifBlank { null },
                    valeurCle = valeurCle.ifBlank { null }, sensClassement = sens,
                    afficherClassement = afficher, compteDansGeneral = compte, actif = actif,
                    ordre = e?.ordre ?: 0
                )
                repo.sauverEpreuve(EpreuveComplete(entite, params, lignesBareme))
                onTermine()
            } catch (ex: Exception) {
                erreur = MessageErreur.traduire(ex)
            }
        }
    }

    Page(if (existante == null) "Nouvelle épreuve" else "Modifier l'épreuve", onBack = onTermine) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "La liste d'épreuves du club n'est qu'un exemple : créez ici l'épreuve et la règle de calcul de votre choix.",
                color = Gris, fontSize = 12.sp
            )
            Champ("Nom de l'épreuve *", nom, { nom = it })
            Champ("Description", description, { description = it })

            SousTitre("Mode de calcul des points")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = mode == Mode.FORMULE, onClick = { mode = Mode.FORMULE })
                Text("Formule mathématique")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = mode == Mode.BAREME, onClick = { mode = Mode.BAREME })
                Text("Barème par classement (rang)")
            }

            if (mode == Mode.FORMULE) {
                Champ("Formule", formule, { formule = it })
                Text(
                    "Utilisez les noms des variables définies ci-dessous. Variables automatiques : " +
                        "age et poidsPilote. Ex : 100 - temps*2 + nbrBalises*10",
                    color = Gris, fontSize = 12.sp
                )
            } else {
                Deroulant("Variable de classement", nomsVariables, valeurCle.ifBlank { null }, { it }, { valeurCle = it })
                Deroulant(
                    "Meilleure performance = valeur…", listOf("ASC", "DESC"), sens,
                    { if (it == "ASC") "La plus petite gagne (ex: temps)" else "La plus grande gagne (ex: distance)" },
                    { sens = it }
                )
                SousTitre("Barème de points par rang")
                bareme.forEachIndexed { i, b ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Champ("Rang", b.rang, { bareme[i] = b.copy(rang = it) }, numerique = true, modifier = Modifier.weight(1f))
                        Champ("Points", b.points, { bareme[i] = b.copy(points = it) }, numerique = true, modifier = Modifier.weight(1f))
                        androidx.compose.material3.TextButton(onClick = { bareme.removeAt(i) }) { Text("✕") }
                    }
                }
                BoutonSecondaire("+ Ajouter un rang") {
                    val prochain = (bareme.mapNotNull { it.rang.toIntOrNull() }.maxOrNull() ?: 0) + 1
                    bareme.add(BaremeUi(prochain.toString(), "0"))
                }
                Text(
                    "Au-delà du dernier rang défini, les points du dernier palier sont reconduits.",
                    color = Gris, fontSize = 12.sp
                )
            }

            SousTitre("Variables saisissables pour cette épreuve")
            Text(
                "Ex : temps, nbrBalises, poidsSac, distance... (valeurs saisies par les bénévoles)",
                color = Gris, fontSize = 12.sp
            )
            parametres.forEachIndexed { i, p ->
                CarteBloc {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Champ("Nom de la variable (sans espace)", p.nom, { parametres[i] = p.copy(nom = it) })
                        Champ("Libellé affiché", p.label, { parametres[i] = p.copy(label = it) })
                        Champ("Unité", p.unite, { parametres[i] = p.copy(unite = it) })
                        BoutonSecondaire("Retirer cette variable") { parametres.removeAt(i) }
                    }
                }
            }
            BoutonSecondaire("+ Ajouter une variable") {
                parametres.add(ParamUi("variable${parametres.size + 1}", "Nouvelle variable", ""))
            }

            SousTitre("Options")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = afficher, onCheckedChange = { afficher = it })
                Text("Afficher le classement de cette épreuve")
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = compte, onCheckedChange = { compte = it })
                Text("Compte dans le classement général")
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = actif, onCheckedChange = { actif = it })
                Text("Épreuve active (proposée à la saisie)")
            }

            Erreur(erreur)
            BoutonPrincipal("Enregistrer l'épreuve") { enregistrer() }
            if (existante != null) BoutonDanger("Supprimer cette épreuve") { confirmerSuppression = true }
            BoutonSecondaire("Annuler", onClick = onTermine)
        }
    }

    if (confirmerSuppression && existante != null) {
        DialogueConfirmation(
            message = "Supprimer l'épreuve « ${existante.epreuve.nom} » et tous ses résultats ?",
            onConfirmer = {
                confirmerSuppression = false
                scope.launch {
                    try { repo.supprimerEpreuve(existante.epreuve.id); onTermine() }
                    catch (ex: Exception) { erreur = MessageErreur.traduire(ex) }
                }
            },
            onAnnuler = { confirmerSuppression = false }
        )
    }
}
