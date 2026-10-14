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

## Lancer le projet

1. Installer **Android Studio** (version récente, JDK 17 inclus).
2. `File > Open` et choisir le dossier `parapente-pps-android`.
3. Attendre la synchronisation Gradle (internet nécessaire la première fois).
4. Brancher un téléphone Android (débogage USB activé) ou créer un émulateur, puis cliquer sur ▶ Run.
5. Pour obtenir un fichier installable : `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

Android 8.0 (API 26) minimum.

⚠️ La base locale passe en version 2 (ajout du genre et des champs de synchronisation). Pour un
appareil ayant déjà la version précédente installée, la base est **recréée automatiquement**
(`fallbackToDestructiveMigration`) : acceptable en phase de test, mais cela efface les données
locales existantes sur cet appareil au premier lancement de cette mise à jour.

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
3. "Synchroniser maintenant" pour envoyer/recevoir les données (résumé affiché à l'écran).
4. Cochez "synchroniser automatiquement au démarrage" pour une tentative silencieuse en arrière-plan
   à chaque ouverture de l'application (n'empêche jamais l'ouverture hors ligne).

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
