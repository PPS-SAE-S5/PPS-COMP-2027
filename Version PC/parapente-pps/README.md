# PPS Championnat - version ordinateur (Java / JavaFX)

Application de gestion de compétition version ordinateur du Championnat de France Pompiers de Parapente, organisé par le club Parapente Pays de Sault.

Documentation : [Documentation utilisateur (PC)](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/main/Documentation/Sprint%203/Documentation%20Utilisateur(v3)(fr)%20version%20PC.md), [Document technique](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/main/Documentation/Sprint%203/Documentation%20Technique(v3)(fr).md), [Cahier des charges](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/main/Documentation/Sprint%203/CahierDesCharges(FR)v3.md).

## Sommaire des nouveautés

| Version | Release | Sprint |
|---|---|---|
| [Version 1.2](#nouveautés-version-12) | Non publiée (développement en cours) | [Sprint 3](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/4) |
| [Version 1.1](#nouveautés-version-11) | [Release 2.1](https://github.com/PPS-SAE-S5/PPS-COMP-2027/releases/tag/v2.1) (10/11/2026) | [Sprint 3](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/4) |
| [Version 1.0](#nouveautés-version-10) | [Release 2.0](https://github.com/PPS-SAE-S5/PPS-COMP-2027/releases/tag/v2.0) (02/11/2026) | [Sprint 2](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/3) |
| [Version 0.6](#nouveautés-version-06) | | [Sprint 2](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/3) |
| [Version 0.5](#nouveautés-version-05) | | [Sprint 2](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/3) |
| [Version 0.4](#nouveautés-version-04) | | [Sprint 2](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/3) |
| [Version 0.3](#nouveautés-version-03) | | [Sprint 2](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/3) |
| [Version 0.2](#nouveautés-version-02) | | [Sprint 1](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/2) |

## Nouveautés version 1.2

Modifications réalisées depuis la release 2.1, sur la branche `Dev-ordinateur-S3`.

1. **Champs obligatoires d'un atelier** ([issue #45](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/45)) : un atelier ne peut plus être enregistré sans nom, journée, créneau, heure de début, heure de fin, lieu et responsable. Les heures doivent être au format `HH:mm` et la fin doit suivre le début. Le message d'erreur liste les champs manquants.
2. **Logo définitif** ([issue #52](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/52)) : le logo du club remplace l'image provisoire sur l'écran de connexion et le tableau de bord.
3. **Export PDF du planning d'une journée** ([issue #50](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/50)) : bouton "Exporter en PDF" dans l'écran "Planning des journées". Le PDF (format A4) reprend le logo, la journée et ses ateliers par créneau avec horaires, lieu, responsable et épreuves. Il est produit par `ExportPdfService` et `PdfSimple`, sans bibliothèque externe. Les rôles qui consultent le planning en lecture seule peuvent aussi l'exporter.
4. **Catégories pompier / open** ([issue #46](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/46)) : la fiche pilote reçoit un numéro de pilote (facultatif, unique) et une catégorie, "Pompier" (championnat) ou "Open" (invité, hors championnat). La catégorie est proposée à partir du numéro, selon le fichier `data/categories.properties` créé au premier lancement (exemple : `numeros.open = 46, 100-199`). Elle reste modifiable sur la fiche. L'écran "Classements" ajoute un filtre "Catégorie" : Toutes catégories, Pompiers ou Open.
5. **Classement "Général"** ([issue #46](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/46)) : le choix "Mixte" s'appelle maintenant "Général (hommes + femmes)".
6. **Compte invité créé automatiquement** au premier lancement, par le script de la base locale. Ce script est maintenant lu en UTF-8, sinon le nom "invité" était mal lu sous Windows avec Java 17.
7. **Tests unitaires** : 169 tests JUnit dans 9 classes (`PlanningServiceTest`, `SuiviParticipantServiceTest`, `CategorieConfigTest`, `PiloteServiceTest`, `ExportPdfServiceTest`, `EpreuveServiceTest`, `CalculServiceTest`, `ClassementServiceTest`, `AuthServiceTest`). Lancement : `mvn test`.
8. **Relecture de la Javadoc** du contrôleur d'atelier.

## Nouveautés version 1.1

Contenu de la [release 2.1](https://github.com/PPS-SAE-S5/PPS-COMP-2027/releases/tag/v2.1) (fichier `PPS-COMP.v2.1-SNAPSHOT.jar`).

1. **Suivre le classement sans compte** ([US 08, issue #16](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/16), [issue #33](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/33)) : le bouton "Non, je veux uniquement suivre le classement" de l'écran de connexion ouvre directement les classements, en lecture seule. Il utilise le compte interne `invité`. Le bouton "Déconnexion" de l'écran des classements ramène à la connexion.
2. **"Caserne" devient "Département"** sur la fiche pilote, dans les classements et dans la base locale (colonne `departement`).
3. **Page web du classement** ([issue #15](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/15)) : le classement est publié en lecture seule sur [pps-classement.vercel.app](https://pps-classement.vercel.app/), à partir de la base en ligne. La page se rafraîchit toutes les 60 secondes. Elle remplace la page de test `lookerBD.html`, qui servait à vérifier le contenu de la base en ligne et a été supprimée.
4. **Tests unitaires** : ajout de JUnit 5 et de Mockito dans `pom.xml`, et premiers tests de `CalculService` (calcul par barème).
5. **Construction du `.jar`** : configuration Maven corrigée pour `mvn clean package` et pour l'exécution des tests.

## Nouveautés version 1.0

Contenu de la [release 2.0](https://github.com/PPS-SAE-S5/PPS-COMP-2027/releases/tag/v2.0), fin du [Sprint 2](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/3).

1. **Planning des journées** ([US 02, issue #10](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/10), [US 03, issue #11](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/11)) : nouvel écran pour organiser la compétition en journées, ateliers du matin ou de l'après-midi, et épreuves. Chaque atelier a des horaires, un lieu, un responsable et un ordre de passage des épreuves. Les flèches `‹` et `›` passent d'une journée à l'autre. Seuls l'administrateur et le responsable de l'épreuve modifient le planning. Nouvelles tables locales : `journees`, `ateliers`, `atelier_epreuves`.
2. **Suivi des participants** ([US 04, issue #12](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/12), [issue #30](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/30)) : recherche d'un pilote par nom, prénom, licence ou département, puis affichage de son total de points, de son rang au général et de ses scores épreuve par épreuve.
3. **Dupliquer une épreuve** : copie d'une épreuve avec ses variables et son barème, sans ses résultats.
4. **Tableau de bord en trois sections** : Préparer la compétition, Pendant la compétition, Administration.
5. **Écran de connexion revu**. L'indication du compte par défaut n'y est plus affichée.
6. **Fichier `.jar` exécutable** : la classe `Launcher` et le plugin `maven-shade-plugin` produisent un `.jar` qui contient JavaFX et toutes les dépendances.

## Nouveautés version 0.6

1. **Classements Mixte / Hommes / Femmes** (écran "Classements") : une liste déroulante "Classement" affiche le classement mixte (hommes + femmes, comme avant), seulement les hommes ou seulement les femmes. Le choix s'applique au classement général et au détail par épreuve, et les rangs sont recalculés parmi les pilotes retenus. Une colonne "Genre" a été ajoutée. Un pilote dont le genre n'est pas renseigné n'apparaît que dans le classement mixte.
2. **Synchronisation dans un sens précis** (écran "Synchronisation en ligne") :
   - "base en ligne → application" (comme `git pull`) : l'application devient identique à la base en ligne
   - "application → base en ligne" (comme `git push`) : la base en ligne devient identique à l'application
   - "Fusionner" : ancien comportement, ne supprime jamais rien

   Les deux boutons "sens unique" **suppriment** ce qui n'existe pas de l'autre côté (pilotes, épreuves, inscriptions, résultats **et comptes utilisateurs**) et demandent une confirmation. Le compte `admin` n'est jamais supprimé, ni le compte connecté. Garde-fou : une base vide en face d'une base remplie est refusée.
   **Pour supprimer quelque chose partout** : supprimez-le sur un appareil, puis lancez "application → base en ligne". Sur les autres appareils, lancez ensuite "base en ligne → application".
3. **Synchronisation automatique au démarrage en réception seule** : elle récupère les nouveautés sans rien envoyer ni supprimer. Avant, un ordinateur resté en retard pouvait renvoyer en ligne des données supprimées entre-temps et les faire réapparaître partout.
4. **Fenêtre** : la fenêtre ne change plus de taille ni de position à chaque changement d'écran (une seule scène, dont seul le contenu est remplacé).
5. **Gestion des comptes** : confirmation avant suppression, impossible de supprimer ou de désactiver le compte avec lequel on est connecté, identifiants uniques sans tenir compte des majuscules.
6. **Messages d'erreur de synchronisation** plus clairs : pas de connexion, URL invalide, délai dépassé, erreur renvoyée par Supabase avec son code.

## Nouveautés version 0.5

1. **Les comptes utilisateurs sont synchronisés** (identifiant, mot de passe haché, rôle, actif ou inactif), fusionnés par identifiant (comme "admin"). Choix assumé pour un outil interne au club : le mot de passe haché transite donc, lui aussi, par Supabase.
2. **Correction** : le script `schema.sql` plantait au deuxième lancement de l'application, à cause d'une ligne qui lisait encore l'ancienne colonne "catégorie" après sa suppression. La ligne a été retirée.
3. **Correction fenêtre** : le redimensionnement au changement d'écran est appliqué après l'affichage, et non juste avant, ce qui le rend fiable y compris en plein écran.
4. **Correction écran de saisie** : la touche Entrée dans un champ de saisie passe au champ suivant au lieu de valider et recharger l'écran à chaque champ. Seul le dernier champ valide, en une fois. Le tableau des résultats déjà saisis ne revient plus en haut à chaque actualisation.

## Nouveautés version 0.4

1. **"Catégorie" (texte libre) remplacée par "Genre"** (Homme ou Femme uniquement, liste déroulante non modifiable) sur la fiche pilote.
2. **Gestion des comptes complétée** (écran "Gestion des comptes") : en sélectionnant un compte dans le tableau, vous pouvez :
   - l'**activer ou le désactiver** (un compte désactivé ne peut plus se connecter, sans avoir à le supprimer ni perdre son historique)
   - lui **changer son mot de passe**

   Le compte `admin` par défaut ne peut toujours être ni supprimé, ni désactivé.

## Nouveautés version 0.3

**Synchronisation en ligne (Supabase).** L'application reste **100 % utilisable hors ligne** : tout fonctionne en local par défaut, et rien ne change si la synchronisation n'est pas configurée.

Un écran **"Synchronisation en ligne"** (visible pour l'administrateur et le responsable de l'épreuve) permet, quand un appareil retrouve internet, d'envoyer et de recevoir les données avec une base Supabase partagée entre plusieurs ordinateurs.

Dans cet écran :
1. Collez l'URL du projet et la clé API publique.
2. Cliquez sur "Tester la connexion" pour vérifier.
3. Choisissez le sens : "base en ligne → application", "application → base en ligne" ou "Fusionner" (voir version 0.6). Un résumé indique combien de lignes ont été reçues, envoyées ou supprimées par table.
4. Cochez "synchronisation automatique au démarrage" pour qu'une synchronisation soit tentée en arrière-plan à chaque ouverture de l'application. Elle est silencieuse et n'empêche jamais l'ouverture si l'appareil est hors ligne.

**Fonctionnement (pour information, rien à faire au quotidien) :**
- Chaque pilote, épreuve, variable, barème, inscription et résultat reçoit un identifiant stable (uuid) qui fait correspondre les mêmes données entre plusieurs appareils. L'id local, qui recommence à 1 sur chaque ordinateur, ne peut pas servir à ça.
- Si la même donnée a été modifiée sur deux appareils avant une synchronisation, la modification la plus récente (par date) est conservée.
- Les pilotes sont fusionnés par numéro de licence, les inscriptions et résultats par couple pilote + épreuve : pas de doublon si deux bénévoles inscrivent le même pilote sur deux ordinateurs avant leur première synchronisation.
- **Les comptes de connexion sont synchronisés** depuis la version 0.5 : le mot de passe haché transite par Supabase, choix assumé pour un outil interne au club. Depuis la version 0.6, leur suppression l'est aussi (boutons "sens unique").
- Avertissement de sécurité : la clé API utilisée est la clé publique ("anon"), enregistrée dans la configuration de l'application. C'est une simplification volontaire pour un outil interne au club. Une personne ayant accès au fichier `data/sync.properties` pourrait lire ou modifier la base en ligne. Une sécurité renforcée (accès restreint par rôle côté serveur) demandera un serveur intermédiaire plutôt que d'exposer la clé directement.

## Nouveautés version 0.2

Corrections apportées après les premiers tests, sans modification des fonctionnalités déjà validées :

1. **Messages d'erreur en français compréhensible** : plus aucun message technique brut (SQL, exceptions Java) n'est affiché à l'écran. Voir `util/MessageErreurUtil.java`.
2. **Liste déroulante des pilotes** (écran "Saisie des résultats") : elle affiche le nom complet et le numéro de licence du pilote au lieu du nom technique de l'objet Java.
3. **Création d'épreuve en mode Barème** : le nom d'une variable saisi ou modifié dans le tableau des variables apparaît immédiatement dans la liste "Variable de classement", sans avoir à ajouter une deuxième variable.
4. **Taille de la fenêtre stabilisée** : elle ne change plus de taille et ne "rétrécit" plus en changeant d'écran, y compris quand la fenêtre est agrandie.
5. **Emplacement pour le logo du club** : une image provisoire (`view/logo.png`) apparaît sur l'écran de connexion et en haut du tableau de bord. Pour changer le logo, il suffit de remplacer le fichier `src/main/resources/com/pps/parapente/view/logo.png` par une autre image portant exactement ce nom (idéalement carrée, fond transparent, en PNG), sans modifier le code.

</br> </br> </br> </br> </br> </br>

---


## **Ce que fait cette version (1.2)**

- **Connexion par rôle** : Administrateur, Responsable de l'épreuve, Bénévole, Comité des pilotes. Le compte créé au premier lancement est `admin` / `admin123`. Changez son mot de passe avant toute utilisation réelle.
- **Accès invité** : le bouton "Non, je veux uniquement suivre le classement" ouvre les classements en lecture seule, sans identifiant.
- **Tableau de bord** en trois sections (Préparer la compétition, Pendant la compétition, Administration), qui n'affiche que les écrans autorisés pour le rôle :

  | Rôle | Écrans |
  |---|---|
  | Administrateur | Tous |
  | Responsable de l'épreuve | Tous sauf la gestion des comptes |
  | Bénévole | Planning (lecture), pilotes, saisie des résultats, classements, suivi des participants |
  | Comité des pilotes | Planning (lecture), classements |

- **Gestion des pilotes** : numéro de pilote, catégorie (Pompier ou Open), n° de licence, nom, prénom, département, poids, email, année de naissance, genre. Un nouveau pilote est inscrit automatiquement à toutes les épreuves actives.
- **Gestion des épreuves, 100 % paramétrable** : la liste donnée par le club (atterrissage de précision, marche & vol, cross, checkpoint...) n'est qu'un **exemple**. Le responsable de l'épreuve crée librement :
  - le nom et la description de l'épreuve
  - les **variables à saisir** (ex : temps, nombre de balises, poids du sac...)
  - le **mode de calcul des points**, au choix :
    - **formule mathématique libre** (ex : `100 - temps*2 + nbrBalises*10 - poidsSac*0.5`), avec accès automatique à `age` et `poidsPilote` du pilote
    - **barème par rang** (ex : 1er = 100 pts, 2e = 90 pts...), calculé en classant les pilotes sur la variable de son choix (temps, distance...)
  - les options "afficher le classement", "compte dans le classement général" et "active"

  Une épreuve peut être dupliquée.
- **Planning des journées** : journées, ateliers du matin ou de l'après-midi (tous les champs obligatoires), épreuves dans l'ordre de passage, export PDF d'une journée.
- **Saisie des résultats** : formulaire généré selon les variables de l'épreuve, avec une case "disqualifié / sécurité" (0 point automatique).
- **Classements** : classement général (une colonne par épreuve) et détail par épreuve, filtres Général / Hommes / Femmes et Toutes catégories / Pompiers / Open. Règle du club : un pilote inscrit sans résultat sur une épreuve obtient 0 point.
- **Suivi des participants** : recherche d'un pilote, total de points, rang, scores épreuve par épreuve.
- **Gestion des comptes** (administrateur) : créer, activer ou désactiver, changer le mot de passe, supprimer.
- **Stockage local** : base de données fichier **H2** dans le dossier `data/`. **Aucune connexion internet n'est nécessaire** pour utiliser l'application, ce qui répond à l'exigence du club en cas d'absence de réseau sur le terrain.
- **Synchronisation en ligne** avec la base Supabase partagée avec la version mobile et la page web du classement.

## Prérequis

- **Java 17 ou plus récent**
- Pour lancer depuis le code source : **Maven** et une connexion internet la première fois, pour télécharger les dépendances (JavaFX, H2, exp4j, json, JUnit, Mockito). Ensuite l'application fonctionne hors-ligne.

## Lancer l'application

Sans compiler : téléchargez le fichier `.jar` de la dernière [release](https://github.com/PPS-SAE-S5/PPS-COMP-2027/releases), puis lancez-le par un double-clic ou avec `java -jar <fichier>.jar`.

Depuis le code source :

```bash
cd parapente-pps
mvn clean javafx:run
```

Au premier lancement, un dossier `data/` est créé à côté du point de lancement. Il contient :
- `pps_championnat.mv.db` : toute la base de données
- `categories.properties` : numéros de pilote réservés aux participants open
- `sync.properties` : configuration de la synchronisation (à ne pas partager publiquement)

Il suffit de conserver ce dossier (ou de le copier sur une clé USB) pour garder toutes les données du championnat.

## Construire un exécutable (.jar) à distribuer

```bash
mvn clean package
```

Le fichier `target/parapente-pps-2.0-SNAPSHOT.jar` contient JavaFX et toutes les dépendances.

## Lancer les tests

```bash
mvn test
```

## Structure du projet

```
parapente-pps/
├── pom.xml                         Dépendances Maven (JavaFX, H2, exp4j, json, JUnit, Mockito)
|
├── src/main/java/com/pps/parapente/
│   ├── MainApp.java                Application JavaFX
│   ├── Launcher.java               Point d'entrée du .jar exécutable
│   ├── model/                      Classes métier (Pilote, Epreuve, Resultat, Journee, Atelier...)
│   ├── dao/                        Accès à la base H2 (SQL brut, JDBC)
│   ├── service/                    Logique métier (calcul des points, classements, planning, suivi, export PDF, authentification)
│   ├── controller/                 Contrôleurs JavaFX (un par écran)
│   ├── sync/                       Synchronisation avec Supabase
│   └── util/                       Navigation, session, sécurité, messages d'erreur, catégories, écriture PDF
├── src/main/resources/
│   ├── db/schema.sql               Schéma SQL de la base locale (rejoué à chaque lancement)
│   └── com/pps/parapente/view/     Écrans FXML, style.css et logo.png
└── src/test/java/com/pps/parapente/
    ├── service/                    Tests des services
    └── util/                       Tests de CategorieConfig
```

## Modèle de données (résumé)

- `pilotes` : fiche d'inscription du pilote, avec son numéro et sa catégorie
- `epreuves` : une épreuve, avec son mode de calcul (`FORMULE` ou `BAREME`)
- `epreuve_parametres` : les variables à saisir définies pour une épreuve
- `bareme_points` : la table rang → points (mode `BAREME`)
- `inscriptions_epreuve` : qui doit passer quelle épreuve
- `resultats` : les valeurs saisies et les points calculés, par pilote et par épreuve
- `journees` : les journées de compétition
- `ateliers` : les ateliers d'une journée (créneau, horaires, lieu, responsable)
- `atelier_epreuves` : les épreuves de chaque atelier et leur ordre de passage
- `utilisateurs` : comptes de connexion et leur rôle

## Pistes d'évolution

Issues encore ouvertes :

- [Sprint 3](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/4) :
  - [#21](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/21) : historique des modifications de points (US 13)
  - [#39](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/39) : journal (logs) de l'ajout et du retrait de points
- [Sprint 4](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/5) :
  - [#43](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/43) : tests de l'application PC
  - [#44](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/44) : tests de l'application mobile
  - [#48](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/48) : import des pilotes depuis un fichier Excel
  - [#49](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/49) : export des classements en Excel
  - [#51](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/51) : limites des ateliers configurables dans un fichier de configuration
  - [#53](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/53) : affichage des sponsors (CSV) sur la page de connexion
- [Sprint 5](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/6) :
  - [#54](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/54) : dépôt de la version finale pour la maintenance