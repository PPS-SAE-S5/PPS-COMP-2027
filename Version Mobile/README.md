# PPS Championnat — version mobile (Android / Kotlin)

Version Android de l'application du Championnat de France Pompiers de Parapente
(Parapente Pays de Sault). Mêmes fonctionnalités que la version PC (JavaFX) :

- connexion par rôle (Administrateur, Responsable de l'épreuve, Bénévole, Pilote, Comité des pilotes)
  — compte par défaut : `admin` / `admin123`
- gestion des pilotes (licence, nom, prénom, caserne, poids, email, année de naissance, catégorie)
- épreuves 100 % paramétrables : variables saisissables, calcul par **formule** ou par **barème de rang**,
  options « afficher le classement » et « compte au général »
- saisie des résultats (formulaire généré selon l'épreuve, case « disqualifié / sécurité »)
- classement général + détail par épreuve (règle : pilote inscrit sans résultat = 0 point)
- gestion des comptes (administrateur)
- **stockage 100 % local** (SQLite/Room) : fonctionne sans internet
- messages d'erreur en français

## Lancer le projet

1. Installer **Android Studio** (version récente, JDK 17 inclus).
2. `File > Open` et choisir le dossier `parapente-pps-android`.
3. Attendre la synchronisation Gradle (internet nécessaire la première fois).
4. Brancher un téléphone Android (débogage USB activé) ou créer un émulateur, puis cliquer sur ▶ Run.
5. Pour obtenir un fichier installable : `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

Android 8.0 (API 26) minimum.

## Changer le logo

Remplacer `app/src/main/res/drawable/logo.png` par le logo du club (même nom de fichier,
PNG, idéalement carré et fond transparent). Il sert aussi d'icône de l'application.

## Structure

```
app/src/main/java/com/pps/parapente/
├── MainActivity.kt
├── data/      # Room (entités, DAO, base), Repository = logique métier (calcul formule/barème, classements)
└── ui/        # Jetpack Compose : thème, composants, navigation, et screens/ (un fichier par écran)
```

## Important : données non partagées avec la version PC

Chaque appareil (PC ou téléphone) a sa **propre base locale**. Les pilotes et résultats saisis sur un
téléphone n'apparaissent pas automatiquement sur le PC, ni sur un autre téléphone. Le partage en temps
réel entre appareils nécessitera un serveur central (question déjà ouverte avec le client).
