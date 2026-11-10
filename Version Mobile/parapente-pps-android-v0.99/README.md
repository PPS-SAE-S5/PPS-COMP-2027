# PPS Championnat — version mobile (Android / Kotlin)

Version Android de l'application du Championnat de France Pompiers de Parapente
(Parapente Pays de Sault). Fonctionnalités alignées sur la version PC (JavaFX) :

- connexion par rôle (Administrateur, Responsable de l'épreuve, Bénévole, Pilote, Comité des pilotes)
  — compte par défaut : `admin` / `admin123`
- gestion des pilotes (licence, nom, prénom, caserne, poids, email, année de naissance, **genre**
  — Homme/Femme uniquement)
- épreuves 100 % paramétrables : variables saisissables, calcul par **formule** ou par **barème de rang**,
  options « afficher le classement » et « compte au général »
- saisie des résultats (formulaire généré selon l'épreuve, case « disqualifié / sécurité »)
- classement général + détail par épreuve (règle : pilote inscrit sans résultat = 0 point)
- gestion des comptes (Administrateur) : créer, **activer/désactiver**, **changer le mot de passe**,
  supprimer (sauf le compte `admin`)
- **stockage 100 % local** (SQLite/Room) : fonctionne sans internet
- **synchronisation en ligne avec Supabase** (écran "Synchronisation en ligne", Administrateur et
  Responsable de l'épreuve) — voir plus bas
- messages d'erreur en français

## Nouveautés (alignées sur la version PC mise à jour par un camarade)

- **Planning des journées** : journées > créneaux (matin / après-midi) > ateliers > épreuves, avec horaires,
  lieu, responsable et ordre de passage. Seuls l'administrateur et le responsable de l'épreuve le modifient ;
  les autres rôles (sauf Pilote) le consultent. Synchronisé avec Supabase.
- **Suivi des participants** : recherche d'un pilote, total de points, rang, scores épreuve par épreuve
  (administrateur, responsable, bénévole).
- **Dupliquer une épreuve** (variables et barème inclus, sans les résultats) depuis la liste des épreuves.
- **Compte invité** : bouton « Non, je veux uniquement suivre le classement » sur l'écran de connexion.
  Le compte `invité` (rôle Pilote = classement seulement) est créé automatiquement s'il n'existe pas.
  Un compte de rôle **Pilote** arrive directement sur le classement ; « retour » le déconnecte.
- **Tableau de bord** rangé en trois sections (Préparer / Pendant la compétition / Administration).

### Corrections importantes
- **Données qui « disparaissaient » après une coupure réseau** : l'envoi d'une épreuve supprime puis réécrit
  ses variables et son barème en plusieurs requêtes ; une coupure entre les deux laissait l'épreuve VIDE en
  ligne, et la réception suivante vidait ensuite les listes sur les autres appareils.
  Désormais : (1) le démarrage automatique ne fait que **recevoir** (jamais d'envoi ni de suppression) ;
  (2) une épreuve sans variables en ligne ne vide plus celles de l'appareil ; (3) on n'efface jamais en
  ligne les variables d'une épreuve qui n'en a aucune ici ; (4) test de connexion rapide avant toute
  synchronisation (échec en quelques secondes sans réseau).
- **Modifier une épreuve changeait son identifiant (uuid)** : la synchronisation la prenait pour une autre
  épreuve (doublons / épreuve supprimée qui réapparaît). L'identité est maintenant conservée.
- **Valeurs vides lues « null »** : sur Android, un champ JSON `null` (caserne, genre, email...) était
  enregistré comme le texte « null ». Corrigé (cela excluait aussi des pilotes des classements Hommes/Femmes).
- **Base de données version 3 avec vraie migration** : les données du téléphone sont conservées.

### Scripts Supabase (éditeur SQL)
- `supabase_migration_v5_planning.sql` : tables du planning (inutile si elles existent déjà).
- `supabase_migration_v4_suppression_comptes.sql` : à exécuter si un compte supprimé réapparaît en ligne.

## Lancer le projet

1. Installer **Android Studio** (version récente, JDK 17 inclus).
2. `File > Open` et choisir le dossier `parapente-pps-android`.
3. Attendre la synchronisation Gradle (internet nécessaire la première fois).
4. Brancher un téléphone Android (débogage USB activé) ou créer un émulateur, puis cliquer sur ▶ Run.
5. Pour obtenir un fichier installable : `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

Android 8.0 (API 26) minimum.

La base locale est en **version 3** (ajout du planning). Depuis la version 2, la mise à jour se fait par une
**vraie migration** : les données déjà présentes sur le téléphone sont conservées. (Seule une très vieille base
en version 1 serait recréée.) En cas de doute, faites d'abord « Synchronisation → application → base en ligne »
pour sauvegarder les données en ligne avant d'installer la mise à jour.

## Changer le logo

Remplacer `app/src/main/res/drawable/logo.png` par le logo du club (même nom de fichier,
PNG, idéalement carré et fond transparent). Il sert aussi d'icône de l'application.

## Synchronisation en ligne (Supabase)

Utilise **exactement la même base Supabase que la version PC** : si elle est déjà configurée côté
PC (scripts `supabase_migration*.sql` déjà exécutés), il suffit de renseigner la même URL et la
même clé API publique dans l'écran "Synchronisation en ligne" du téléphone — aucune configuration
Supabase supplémentaire n'est nécessaire côté mobile.

Dans cet écran :
1. Collez l'URL du projet et la clé API publique.
2. "Tester la connexion" pour vérifier.
3. Choisissez le sens : « base en ligne → application » (comme `git pull`), « application → base en ligne »
   (comme `git push`) ou « Fusionner » (ne supprime jamais rien). Les deux premiers rendent les deux bases
   identiques (suppressions comprises, comptes inclus sauf `admin`) et demandent une confirmation.
   Pour supprimer quelque chose partout : le supprimer sur un appareil, puis « application → base en ligne » ;
   sur les autres appareils, « base en ligne → application ».
4. La case « au démarrage » récupère silencieusement les **nouveautés** à chaque ouverture (réception seule :
   rien n'est envoyé ni supprimé ; n'empêche jamais l'ouverture hors ligne).

Les pilotes se fusionnent par numéro de licence, les comptes par identifiant, les inscriptions et
résultats par couple pilote+épreuve : pas de doublon si la même donnée est créée indépendamment sur
plusieurs appareils avant leur première synchronisation.

**Les comptes utilisateurs sont eux aussi synchronisés** (mot de passe haché inclus) — choix
volontaire pour un outil interne au club, identique à la version PC.

## Structure

```
app/src/main/java/com/pps/parapente/
├── MainActivity.kt
├── data/      # Room (entités, DAO, base), Repository = logique métier (calcul formule/barème, classements)
├── sync/      # Synchronisation Supabase (SyncConfig, SupabaseClient, SyncService, RapportSync)
└── ui/        # Jetpack Compose : thème, composants, navigation, et screens/ (un fichier par écran)
```
