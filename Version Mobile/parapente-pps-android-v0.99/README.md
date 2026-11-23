# PPS Championnat - version mobile 2.0 (Android / Kotlin)

Application Android du Championnat de France Pompiers de Parapente, organisé par le club Parapente Pays de Sault.\
Elle reprend les fonctions de la version PC (JavaFX) et partage la même base en ligne Supabase.

## Fonctionnalités

- **Connexion par rôle** : Administrateur, Responsable de l'épreuve, Bénévole, Comité des pilotes. Le compte créé au premier lancement est `admin` / `admin123`, changez son mot de passe avant toute utilisation réelle.
- **Accès invité** : le bouton "Non, je veux uniquement suivre le classement" de l'écran de connexion ouvre directement le classement, en lecture seule. Il utilise le compte `invité` (rôle Pilote), créé automatiquement s'il n'existe pas sur le téléphone. Le bouton de retour le déconnecte.
- **Tableau de bord** en trois sections : Préparer la compétition, Pendant la compétition, Administration. Seules les cartes autorisées pour le rôle s'affichent.
- **Gestion des pilotes** : licence, nom, prénom, département, poids, email, année de naissance, genre (Homme ou Femme).
- **Épreuves paramétrables** : variables à saisir, calcul par formule (exp4j) ou par barème de rang, options "afficher le classement" et "compte au général". Une épreuve peut être dupliquée avec ses variables et son barème, sans ses résultats.
- **Planning des journées** : journée, ateliers du matin ou de l'après-midi, épreuves, avec horaires, lieu, responsable et ordre de passage. L'administrateur et le responsable de l'épreuve le modifient, le bénévole et le comité le consultent.
- **Saisie des résultats** : formulaire généré selon l'épreuve, case "disqualifié / problème de sécurité" (0 point).
- **Classements** : général et détail par épreuve, filtre Mixte / Hommes / Femmes. Un pilote inscrit sans résultat obtient 0 point.
- **Suivi des participants** : recherche d'un pilote, total de points, rang et scores épreuve par épreuve (administrateur, responsable, bénévole).
- **Gestion des comptes** (administrateur) : créer, activer ou désactiver, changer le mot de passe, supprimer. Le compte `admin` ne peut être ni supprimé ni désactivé.
- **Stockage local** (SQLite / Room) : l'application fonctionne sans internet.
- **Synchronisation avec Supabase** (administrateur et responsable de l'épreuve), voir plus bas.
- Messages d'erreur en français.

## Corrections de la version 2.0

- **Épreuves vidées après une coupure réseau.**\
L'envoi d'une épreuve supprime puis réécrit ses variables et son barème en plusieurs requêtes. Une coupure entre ces requêtes laissait l'épreuve vide en ligne, et la réception suivante vidait les listes des autres appareils. Désormais :
  1. la synchronisation au démarrage ne fait que recevoir, sans envoi ni suppression
  2. une épreuve sans variables en ligne ne vide plus celles du téléphone
  3. les variables d'une épreuve ne sont jamais effacées en ligne si le téléphone n'en a aucune
  4. un test de connexion rapide précède chaque synchronisation, qui échoue en quelques secondes sans réseau.
- **Identifiant (uuid) modifié à chaque édition d'une épreuve.**\
La synchronisation prenait l'épreuve modifiée pour une autre, ce qui créait des doublons ou faisait réapparaître des épreuves supprimées. L'identifiant est maintenant conservé.
- **Champs vides enregistrés comme le texte "null".**\
Sous Android, un champ JSON `null` (département, genre, email...) devenait la chaîne "null", ce qui excluait certains pilotes des classements Hommes et Femmes. Corrigé.
- **Migrations de la base locale.**\
Les mises à jour conservent les données du téléphone (voir plus bas).

## Lancer le projet

1. Installer **Android Studio** (version récente, JDK 17 inclus)
2. `File > Open`, puis choisir le dossier `parapente-pps-android-v0.99`
3. Attendre la synchronisation Gradle (internet nécessaire la première fois)
4. Brancher un téléphone Android avec le débogage USB activé, ou créer un émulateur, puis cliquer sur ▶ Run
5. Pour obtenir un fichier installable : `Build > Build Bundle(s) / APK(s) > Build APK(s)`

Configuration : Android 8.0 (API 26) minimum, cible API 34, `versionName` 2.0

La base locale est en **version 4**.\
Les données du téléphone sont conservées, seule une base en version 1 est recréée donc par précaution, lancez "Application → base en ligne" avant d'installer une mise à jour, pour avoir une copie des données en ligne.

## Synchronisation en ligne (Supabase)

L'application utilise la même base Supabase que la version PC. Il suffit de saisir la même URL de projet et la même clé API publique dans l'écran "Synchronisation en ligne" du téléphone.

Dans cet écran :
1. Collez l'URL du projet et la clé API publique, puis enregistrez.
2. Cliquez sur "Tester la connexion"
3. Choisissez le sens :
   - "Base en ligne → application" (comme `git pull`) : le téléphone devient identique à la base en ligne
   - "Application → base en ligne" (comme `git push`) : la base en ligne devient identique au téléphone
   - "Fusionner" : échange dans les deux sens, sans rien supprimer

   Les deux premiers modes suppriment ce qui n'existe plus de l'autre côté, comptes compris (sauf `admin`), et demandent une confirmation.\
   Pour supprimer une donnée partout : supprimez-la sur un appareil et lancez "Application → base en ligne", puis "Base en ligne → application" sur les autres appareils.
4. La case "au démarrage" récupère les nouveautés à chaque ouverture. Elle ne fait que recevoir et n'empêche pas l'ouverture hors-ligne

Les pilotes sont rapprochés par numéro de licence, les comptes par identifiant, les inscriptions et les résultats par couple pilote + épreuve : une même donnée créée sur deux appareils avant leur première synchronisation ne produit pas de doublon.\
En cas de conflit, la modification la plus récente est conservée.

Les comptes utilisateurs sont synchronisés avec leur mot de passe haché (SHA-256), comme sur la version PC.\
C'est un choix volontaire pour un outil interne au club.

## Structure

```
app/src/main/java/com/pps/parapente/
├── MainActivity.kt
|
├── data/      Room (entités, DAO, base, migrations), Repository : logique métier (calculs, classements, planning, comptes)
|
├── sync/      Synchronisation Supabase (SyncConfig, SupabaseClient, SyncService, RapportSync)
|
└── ui/        Jetpack Compose : thème, composants, navigation, et screens/ (un fichier par écran)
```

## État par rapport à la version PC

L'application mobile est en attente de synchronisation avec le build le plus récent de la version PC.\
Ces fonctions de la version PC n'y sont pas encore :
- numéro de pilote et catégorie pompier / open, avec le filtre de classement par catégorie
- filtre "Général" à la place de "Mixte"
- champs obligatoires d'un atelier (seul le nom l'est sur mobile)
- export PDF du planning d'une journée
- logo définitif