package com.pps.parapente.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pps.parapente.R

/** Page avec barre du haut bleue (et logo optionnel). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    titre: String,
    onBack: (() -> Unit)?,
    logo: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    contenu: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = Fond,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (logo) {
                            Image(painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.size(34.dp))
                            Text("  ")
                        }
                        Text(titre, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (onBack != null) TextButton(onClick = onBack) { Text("←", color = Color.White, fontSize = 22.sp) }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Bleu, titleContentColor = Color.White, actionIconContentColor = Color.White
                )
            )
        },
        content = contenu
    )
}

@Composable
fun CarteBloc(onClick: (() -> Unit)? = null, contenu: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = contenu)
    }
}

@Composable
fun Champ(
    label: String, valeur: String, onChange: (String) -> Unit,
    motDePasse: Boolean = false, numerique: Boolean = false, modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = valeur,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        visualTransformation = if (motDePasse) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = when {
                motDePasse -> KeyboardType.Password
                numerique -> KeyboardType.Decimal
                else -> KeyboardType.Text
            }
        )
    )
}

@Composable
fun BoutonPrincipal(texte: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
        Text(texte, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BoutonSecondaire(texte: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) { Text(texte) }
}

@Composable
fun BoutonDanger(texte: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = Rouge)) {
        Text(texte)
    }
}

@Composable
fun Erreur(message: String?) {
    if (message != null) Text(message, color = Rouge, fontWeight = FontWeight.Bold)
}

@Composable
fun Info(message: String?) {
    if (message != null) Text(message, color = Vert, fontWeight = FontWeight.Bold)
}

@Composable
fun SousTitre(texte: String) {
    Text(texte, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Bleu)
}

/** Liste déroulante générique (ex : choix de l'épreuve, du pilote, du rôle...). */
@Composable
fun <T> Deroulant(
    label: String, elements: List<T>, selection: T?, libelle: (T) -> String,
    onSelection: (T) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()
) {
    var ouvert by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, fontSize = 12.sp, color = Gris)
        Box {
            OutlinedButton(onClick = { ouvert = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    selection?.let(libelle) ?: "Choisir…",
                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text("▾")
            }
            DropdownMenu(expanded = ouvert, onDismissRequest = { ouvert = false }) {
                elements.forEach { element ->
                    DropdownMenuItem(
                        text = { Text(libelle(element)) },
                        onClick = { ouvert = false; onSelection(element) }
                    )
                }
            }
        }
    }
}

@Composable
fun DialogueConfirmation(message: String, onConfirmer: () -> Unit, onAnnuler: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Confirmation") },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirmer) { Text("Oui, supprimer", color = Rouge) } },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

/** Cellule de tableau à largeur fixe (tableaux défilants horizontalement). */
@Composable
fun Cellule(texte: String, largeur: Dp, gras: Boolean = false, couleur: Color = Color.Unspecified) {
    Text(
        texte,
        modifier = Modifier.width(largeur).padding(horizontal = 4.dp, vertical = 6.dp),
        fontWeight = if (gras) FontWeight.Bold else FontWeight.Normal,
        color = couleur,
        fontSize = 13.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun LigneTableau(entete: Boolean = false, contenu: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .background(if (entete) Bleu else Color.White)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = contenu
    )
}
