# Cahier de tests (version PC)

> Ce document décrit **comment** la logique métier de l'application PC est testée : environnement, stratégie, cas de tests et limites connues. L'architecture et la logique métier sont décrites dans le [Document technique](https://github.com/PPS-SAE-S5/PPS-COMP-2027/tree/main/Documentation).
>
> **Version 2 (Sprint 4)** : ce cahier couvre les tests unitaires des services, les tests des DAO sur une base H2 en mémoire et les tests des classes utilitaires de l'application PC (JUnit 5 et Mockito).

## Sommaire

- [Objectif et périmètre](#1-objectif-et-périmètre)
- [Environnement](#2-environnement)
- [Lancer les tests](#3-lancer-les-tests)
- [Stratégie de test](#4-stratégie-de-test)
- [Mise en place et fonctions utilitaires](#5-mise-en-place-et-fonctions-utilitaires)
- [Synthèse par classe](#6-synthèse-par-classe)
- [Cas de tests](#7-cas-de-tests)
- [Limites connues et suite](#8-limites-connues-et-suite)

---

## 1. Objectif et périmètre

Ce cahier permet de vérifier que les règles de calcul des points, les validations, les classements, le planning, l'export PDF et la persistance en base fonctionnent, et de détecter rapidement les régressions après chaque modification du code.

| | Éléments |
|---|---|
| **Testé (services)** | `CalculService`, `ClassementService`, `EpreuveService`, `PiloteService`, `PlanningService`, `SuiviParticipantService`, `AuthService`, `ExportPdfService` |
| **Testé (DAO)** | `EpreuveDAO`, `InscriptionDAO`, `PiloteDAO`, `PlanningDAO`, `ResultatDAO`, `UtilisateurDAO`, `DatabaseManager` (dossier de données) |
| **Testé (utilitaires)** | `CategorieConfig`, `MessageErreurUtil` |
| **Non testé pour le moment** | Package `sync` (synchronisation Supabase), `SecuriteUtil`, contrôleurs JavaFX, application mobile |

## 2. Environnement

| Outil | Version | Rôle |
|---|---|---|
| Java | release 17 (JDK 24) | Langage |
| Maven | 3.x | Compilation et lancement des tests |
| JUnit Jupiter | 5.10.2 | Écriture et exécution des tests |
| Mockito | 5.18.0 | Faux objets (DAO simulés, classes statiques) |
| H2 | version du `pom.xml` | Base en mémoire pour les tests de DAO |
| Maven Surefire | 3.5.4 | Plugin d'exécution des tests |

> Mockito 5.18.0 est nécessaire avec le JDK 24 : la version 5.11.0 échoue avec l'erreur « Java 24 (68) is not supported ».

## 3. Lancer les tests

Depuis le dossier `Version PC/parapente-pps` (celui qui contient le `pom.xml`) :

```
mvn test                                          # tous les tests
mvn -Dtest=CalculServiceTest test                 # une seule classe
mvn -Dtest=CalculServiceTest#formuleSimple test   # une seule méthode
mvn -Dtest=*DAOTest test                          # tous les tests de DAO
```

Résultat attendu : `BUILD SUCCESS`, `Failures: 0, Errors: 0` (1 test est ignoré : voir PL-12). Les avertissements Mockito sur l'agent Java sont sans conséquence.

Les tests sont dans `src/test/java/com/pps/parapente/`, dans le même package que la classe testée (`service`, `dao`, `util`), et ne sont jamais inclus dans l'application livrée.

## 4. Stratégie de test

Les services créent leurs DAO avec `new` dans leurs champs : sans précaution, chaque test ouvrirait la vraie base H2. Les tests de services utilisent donc des **faux DAO** (Mockito `mockConstruction`), sans modifier le code de production. Les DAO, eux, sont testés séparément sur une vraie base H2 en mémoire.

| Technique | Utilisée pour |
|---|---|
| `mockConstruction` | Remplacer les DAO (et `ClassementService`) créés avec `new` à l'intérieur des services |
| `mockStatic` | Remplacer les classes statiques (`SecuriteUtil`, `SessionManager`) dans `AuthServiceTest` ; rediriger `DatabaseManager.getConnection()` vers la base en mémoire dans les tests de DAO |
| Base H2 en mémoire | Tests de DAO : chaque test travaille sur une base neuve créée avec le vrai `schema.sql` (classe `DaoTestBase`). Le dossier `data/` n'est jamais touché |
| `ArgumentCaptor` et `verify` | Contrôler ce qui est réellement envoyé au DAO (bénévole, pilote, ordre des appels) |
| Faux objets du modèle (`mock(Pilote.class)`, etc.) | Construire rapidement des données dans `ClassementServiceTest`, `SuiviParticipantServiceTest` et `ExportPdfServiceTest` |
| `@TempDir` | Écrire de vrais fichiers PDF dans un dossier temporaire (`ExportPdfServiceTest`) |
| `@Disabled` | Mettre de côté un test qui décrit un bug connu (PL-12) |
| Aucun mock | Méthodes statiques de `PlanningService` (validation des horaires), `CategorieConfig`, `MessageErreurUtil` et génération réelle des PDF de `ExportPdfService` |

| | Détail |
|---|---|
| **Ce que ça prouve** | Services : si la base renvoie ces données, la logique métier produit le bon résultat (calculs, validations, tris, formats). DAO : le SQL et le schéma réels fonctionnent (contraintes d'unicité, relecture des données, JSON des valeurs, cascades) |
| **Ce que ça ne prouve pas** | Que les services et les DAO fonctionnent bien ensemble (les services sont testés avec de faux DAO), ni le comportement de l'interface JavaFX, ni la synchronisation Supabase |

## 5. Mise en place et fonctions utilitaires

### Tests de services

| Élément | Rôle |
|---|---|
| `setUp` (`@BeforeEach`) | Active `mockConstruction` sur les DAO du service testé, **puis** crée le service et récupère le faux DAO (`constructed().get(0)`) |
| `tearDown` (`@AfterEach`) | Ferme les interceptions pour ne pas fausser les autres tests |
| `epreuveBareme(cle, sens, points...)` | `CalculServiceTest` : construit une épreuve BAREME (id 1). `sens` : DESC (plus grand gagne) ou ASC (plus petit gagne). Le 1er montant est le rang 1 |
| `resultat(piloteId, valeurs, disqualifie)` | `CalculServiceTest` : construit le résultat d'un pilote sur l'épreuve 1, points à 0 au départ |
| `epreuveFormule(formule)` | `CalculServiceTest` : construit une épreuve FORMULE (id 1) |
| `pilote`, `epreuve`, `resultat`, `inscrits`, `resultats` | `ClassementServiceTest` : remplissent les listes lues par les faux DAO (pilotes, épreuves, résultats, inscriptions) |
| `filtreGenreQuiAccepte(pilotes...)` | `ClassementServiceTest` : faux filtre de genre qui n'accepte que les pilotes donnés |
| `pilote`, `epreuve`, `resultat`, `valeurs`, `ligneClassement`, `inscrit` | `SuiviParticipantServiceTest` : construisent les données lues par les faux DAO et par le faux `ClassementService` |
| Méthodes « accesseurs » | `SuiviParticipantServiceTest` : regroupent les appels aux getters de `SuiviParticipant` et `LigneSuivi`, pour n'avoir qu'un endroit à adapter si ces classes changent |
| `atelierValide()`, `journee(nom)` | `PlanningServiceTest` : atelier complet (matin, 08:00-10:30) et journée simple |
| `journee`, `atelier`, `autreCreneau`, `nombrePages`, `assertPdfValide` | `ExportPdfServiceTest` : construisent les données et vérifient le PDF (en-tête `%PDF-`, fin `%%EOF`, nombre de pages) |

### Tests de DAO

| Élément | Rôle |
|---|---|
| `ouvrirBaseEnMemoire` (`@BeforeEach`) | `DaoTestBase` : ouvre une base H2 en mémoire au nom unique, redirige `DatabaseManager.getConnection()` vers elle, puis crée le schéma |
| `creerSchema` | Exécute le vrai `/db/schema.sql` ; les données initiales (comptes par défaut) sont facultatives |
| `fermerBase` (`@AfterEach`) | Ferme l'interception et la connexion |
| `nouveauPilote(licence, nom)` | Crée et enregistre un pilote (prénom Jean, genre H, catégorie POMPIER) |
| `nouvelleEpreuve(nom)` | Crée et enregistre une épreuve FORMULE (formule `x`, DESC, active) |

### Tests d'utilitaires

| Élément | Rôle |
|---|---|
| `reinitialiser` (`@AfterEach`) | `CategorieConfigTest` : remet la règle statique à vide pour ne pas polluer les autres tests |
| `definirRegle(...)` | `CategorieConfigTest` : injecte la règle sans lire (ni créer) le fichier `data/categories.properties` |

Chaque test repart d'un état vierge : les tests sont indépendants.

## 6. Synthèse par classe

| Classe testée | Fichier de test | Tests | Ce qui est vérifié |
|---|---|---|---|
| `AuthService` | `AuthServiceTest` | 5 | Connexion, déconnexion, erreurs |
| `CalculService` | `CalculServiceTest` | 16 | Formules, barème (tri, rangs, cas limites), enregistrement |
| `ClassementService` | `ClassementServiceTest` | 12 | Classement général et par épreuve, règle « inscrit sans résultat = 0 point », filtres genre et catégorie |
| `EpreuveService` | `EpreuveServiceTest` | 18 | Validation, création, mise à jour, duplication |
| `PiloteService` | `PiloteServiceTest` | 18 | Validation de la fiche pilote, inscription aux épreuves actives |
| `PlanningService` | `PlanningServiceTest` | 36 | Horaires, champs obligatoires, journées, ateliers (1 test désactivé : bug connu) |
| `SuiviParticipantService` | `SuiviParticipantServiceTest` | 24 | Détail par pilote : statuts, total, rang, formatage des valeurs |
| `ExportPdfService` | `ExportPdfServiceTest` | 18 | Nom de fichier, génération et pagination du PDF, écriture du fichier |
| `EpreuveDAO` | `EpreuveDAOTest` | 9 | Enregistrement et relecture, paramètres et barème, ordre, annulation en cas d'erreur |
| `InscriptionDAO` | `InscriptionDAOTest` | 8 | Inscription sans doublon, épreuves actives, lecture par pilote ou par épreuve |
| `PiloteDAO` | `PiloteDAOTest` | 10 | Création, relecture, unicité licence et numéro, tri |
| `PlanningDAO` | `PlanningDAOTest` | 12 | Journées, ateliers, affectation des épreuves, suppressions |
| `ResultatDAO` | `ResultatDAOTest` | 8 | Enregistrement, mise à jour sans doublon, relecture des valeurs JSON |
| `UtilisateurDAO` | `UtilisateurDAOTest` | 10 | Comptes, activation, mot de passe, unicité, lien avec un pilote |
| `DatabaseManager` | `DatabaseManagerTest` | 1 | Dossier de données |
| `CategorieConfig` | `CategorieConfigTest` | 22 | Lecture de la règle, déduction de la catégorie, description |
| `MessageErreurUtil` | `MessageErreurUtilTest` | 29 | Traduction des erreurs techniques en messages lisibles, ordre des règles |
| **Total** | | **256** | dont 1 désactivé |

Nombres issus de la dernière exécution de `mvn test`, à mettre à jour après chaque ajout.

## 7. Cas de tests

Les identifiants sont uniques dans tout le cahier, avec un préfixe par classe : AU, CS, CL, ES, PS, PL, SP, EX (services) ; DE, DI, DP, DL, DR, DU, DM (DAO) ; CC, ME (utilitaires).

### Classe `AuthServiceTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| AU-01 | `seConnecter` | Identifiants valides | Identifiant `alice`, mot de passe `secret123` | Utilisateur retourné, session ouverte une seule fois | Réussi |
| AU-02 | `seConnecter` | Identifiant inconnu | Identifiant `inconnu`, mot de passe `secret123` | Optional vide, pas de hachage, pas de session | Réussi |
| AU-03 | `seConnecter` | Mot de passe erroné | Identifiant `alice`, mot de passe `mauvais` | Optional vide, pas d'ouverture de session | Réussi |
| AU-04 | `seConnecter` | Erreur SQL | Identifiant `alice`, DAO déclenchant une `SQLException` | `SQLException` propagée, pas de session | Réussi |
| AU-05 | `seDeconnecter` | Déconnexion | Appel de `seDeconnecter()` | Appelle `SessionManager.deconnecter()` une seule fois | Réussi |

### Classe `CalculServiceTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| CS-01 | `evaluerFormule` | Formule valide | `100 - temps*2 + nbrBalises*10`, temps=5, nbrBalises=3 | 120 | Réussi |
| CS-02 | `evaluerFormule` | Formule vide ou null | `"  "`, `null` | `IllegalArgumentException` | Réussi |
| CS-03 | `evaluerFormule` | Variable inconnue | `x + y`, seul x fourni | Exception | Réussi |
| CS-04 | `recalculerBareme` | Tri DESC | barème 100/80/60, distances 10, 30, 20 | 30→100, 20→80, 10→60 | Réussi |
| CS-05 | `recalculerBareme` | Tri ASC | barème 100/80, temps 50 et 20 | 20→100, 50→80 | Réussi |
| CS-06 | `recalculerBareme` | Disqualifié sans rang | disqualifié à 99, normal à 10 | 0 et 100 | Réussi |
| CS-07 | `recalculerBareme` | Résultat sans valeur clé | un avec `temps` seul, un avec `distance` | 0 et 100 | Réussi |
| CS-08 | `recalculerBareme` | Au-delà du barème | barème 100/50, 3 pilotes | 3e pilote : 50 | Réussi |
| CS-09 | `recalculerBareme` | Barème vide | aucun palier | 0 point | Réussi |
| CS-10 | `recalculerBareme` | Valeur clé vide ou null | `" "`, `null` | `IllegalStateException` | Réussi |
| CS-11 | `recalculerBareme` | Mauvais mode | épreuve FORMULE | `IllegalStateException` | Réussi |
| CS-12 | `calculerEtEnregistrerFormule` | `age` et `poidsPilote` | `distance + age + poidsPilote`, 30 ans, 80 kg, distance 10 | 120, pilote et bénévole enregistrés | Réussi |
| CS-13 | `calculerEtEnregistrerFormule` | Pilote sans poids ni année | pilote vide, distance 10 | 10 | Réussi |
| CS-14 | `calculerEtEnregistrerFormule` | Disqualifié sans évaluer | formule invalide, disqualifié | 0 point, pas d'exception | Réussi |
| CS-15 | `calculerEtEnregistrerFormule` | Mauvais mode | épreuve BAREME | `IllegalStateException` | Réussi |
| CS-16 | `enregistrerValeurBareme` | Enregistrer puis recalculer | pilote 7, distance 12 | saisie à 0 point, puis relecture des résultats | Réussi |

### Classe `ClassementServiceTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| CL-01 | `calculerClassementGeneral` | Somme, tri et rangs | 3 pilotes, 2 épreuves : E1 a=10, b=30, c=20 ; E2 a=5, b=10, c=40 | c=60 (rang 1), b=40 (rang 2), a=15 (rang 3) | Réussi |
| CL-02 | `calculerClassementGeneral` | Inscrit sans résultat | a a 25 points, b est inscrit sans résultat | b : total 0 et 0 point pour l'épreuve ; a est 1er | Réussi |
| CL-03 | `calculerClassementGeneral` | Pilote non inscrit | b non inscrit mais a un résultat de 99 | total 0, aucune épreuve comptée pour b | Réussi |
| CL-04 | `calculerClassementGeneral` | Épreuves ignorées | une épreuve inactive, une hors général, une normale (10 points chacune) | total 10, seule l'épreuve normale est comptée | Réussi |
| CL-05 | `calculerClassementGeneral` | Inscrit disparu | inscrits 1 et 999, le pilote 999 n'existe plus | 999 ignoré sans erreur, 1 ligne | Réussi |
| CL-06 | `calculerClassementGeneral` | Aucune donnée | aucun pilote, aucune épreuve | liste vide | Réussi |
| CL-07 | `calculerClassementGeneral` | Filtre de genre | 1 homme (100), 2 femmes (20 et 30), filtre « femmes » | 2 lignes, la femme à 30 est rang 1, l'autre rang 2 | Réussi |
| CL-08 | `calculerClassementGeneral` | Filtre de catégorie | pompier (10) et open (20), filtre acceptant le pompier | 1 seule ligne : le pompier | Réussi |
| CL-09 | `calculerClassementGeneral` | Erreur SQL | `PiloteDAO` qui lève une `SQLException` | `SQLException` propagée | Réussi |
| CL-10 | `calculerClassementEpreuve` | Tri et pilote associé | a=10, b=30, c=20 | ordre b, c, a ; pilote rattaché à chaque résultat | Réussi |
| CL-11 | `calculerClassementEpreuve` | Filtre de genre | homme (50), femme (10), filtre « femmes » | seul le résultat de la femme | Réussi |
| CL-12 | `calculerClassementEpreuve` | Épreuve sans résultat | aucun résultat | liste vide | Réussi |

### Classe `EpreuveServiceTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| ES-01 | `listerToutes` | Délégation au DAO | 2 épreuves renvoyées par le DAO | la même liste | Réussi |
| ES-02 | `supprimer` | Délégation au DAO | id 7 | `EpreuveDAO.supprimer(7)` appelé | Réussi |
| ES-03 | `creer` | Épreuve Formule valide | « Vitesse », FORMULE, `x * 2` | enregistrée, puis tous les pilotes inscrits | Réussi |
| ES-04 | `creer` | Épreuve Barème valide | « Précision », BAREME, clé `temps`, barème 10/8 | enregistrée, puis tous les pilotes inscrits | Réussi |
| ES-05 | `creer` | Nom null | nom `null` | `IllegalArgumentException`, ni enregistrement ni inscription | Réussi |
| ES-06 | `creer` | Nom blanc | nom `"   "` | `IllegalArgumentException`, rien enregistré | Réussi |
| ES-07 | `creer` | Formule manquante | mode Formule, formule `" "` | `IllegalArgumentException` dont le message contient « formule » | Réussi |
| ES-08 | `creer` | Valeur clé manquante | mode Barème, valeur clé `null` | `IllegalArgumentException`, rien enregistré | Réussi |
| ES-09 | `creer` | Barème vide | mode Barème, aucun palier | `IllegalArgumentException`, rien enregistré | Réussi |
| ES-10 | `mettreAJour` | Mise à jour valide | épreuve Formule valide | délégué au DAO, aucune nouvelle inscription | Réussi |
| ES-11 | `mettreAJour` | Mise à jour invalide | nom vide | `IllegalArgumentException`, DAO non appelé | Réussi |
| ES-12 | `dupliquer` | Épreuve introuvable | id 99 inexistant | `IllegalStateException`, rien créé | Réussi |
| ES-13 | `dupliquer` | Copie des champs | épreuve « Précision » | copie « Précision (copie) » avec mêmes description, mode, formule, clé, sens et indicateurs ; passe par `creer` et inscrit les pilotes | Réussi |
| ES-14 | `dupliquer` | Ordre de la copie | ordres existants 1, 5 et 3 | ordre de la copie = 6 | Réussi |
| ES-15 | `dupliquer` | Copie en profondeur | un paramètre `temps` (obligatoire, ordre 3) et un barème de 2 paliers | nouvelles instances, mêmes valeurs | Réussi |
| ES-16 | `dupliquer` | Nom déjà pris | « Précision (copie) » existe, puis « Précision (copie 2) » aussi | « Précision (copie 2) », puis « Précision (copie 3) » | Réussi |
| ES-17 | `dupliquer` | Casse ignorée | « PRÉCISION (COPIE) » existe | « Précision (copie 2) » | Réussi |
| ES-18 | `dupliquer` | Erreur SQL | DAO qui lève une `SQLException` | `SQLException` propagée | Réussi |

### Classe `PiloteServiceTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| PS-01 | `listerTous` | Délégation au DAO | un pilote renvoyé par le DAO | la même liste | Réussi |
| PS-02 | `supprimer` | Délégation au DAO | id 5 | `PiloteDAO.supprimer(5)` appelé | Réussi |
| PS-03 | `creer` | Pilote valide | licence `LIC-001`, Dupont, Jean, numéro 7 | enregistré, puis inscrit aux épreuves actives (dans cet ordre) | Réussi |
| PS-04 | `creer` | Numéro de pilote absent | numéro `null` | accepté | Réussi |
| PS-05 | `creer` | Borne basse du numéro | numéro 1 | accepté | Réussi |
| PS-06 | `creer` | Licence null | licence `null` | `IllegalArgumentException` (message « licence »), DAO non appelé | Réussi |
| PS-07 | `creer` | Licence blanche | licence `"   "` | `IllegalArgumentException` (message « licence ») | Réussi |
| PS-08 | `creer` | Nom null | nom `null` | `IllegalArgumentException` (message « nom ») | Réussi |
| PS-09 | `creer` | Nom vide | nom `""` | `IllegalArgumentException` (message « nom ») | Réussi |
| PS-10 | `creer` | Prénom null | prénom `null` | `IllegalArgumentException` (message « prénom ») | Réussi |
| PS-11 | `creer` | Prénom blanc | prénom espace + tabulation | `IllegalArgumentException` (message « prénom ») | Réussi |
| PS-12 | `creer` | Numéro de pilote 0 | numéro 0 | `IllegalArgumentException` (message « numéro de pilote ») | Réussi |
| PS-13 | `creer` | Numéro de pilote négatif | numéro -3 | `IllegalArgumentException` (message « numéro de pilote ») | Réussi |
| PS-14 | `creer` | Échec de l'enregistrement | DAO qui lève une `SQLException` | exception propagée, aucune inscription | Réussi |
| PS-15 | `creer` | Échec de l'inscription | inscription qui lève une `SQLException` | exception propagée | Réussi |
| PS-16 | `mettreAJour` | Mise à jour valide | pilote valide | délégué au DAO, sans nouvelle inscription | Réussi |
| PS-17 | `mettreAJour` | Pilote invalide | nom vide | `IllegalArgumentException`, DAO non appelé | Réussi |
| PS-18 | `mettreAJour` | Numéro négatif | numéro -1 | `IllegalArgumentException`, DAO non appelé | Réussi |

### Classe `PlanningServiceTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| PL-01 | `heureValide` | Formats valides | `00:00`, `08:00`, `12:30`, `19:59`, `23:59` | vrai | Réussi |
| PL-02 | `heureValide` | Espaces ignorés | `"  08:00 "` | vrai | Réussi |
| PL-03 | `heureValide` | Formats invalides | `null`, vide, blanc, `8:00`, `24:00`, `08:60`, `0800`, `08h00`, `ab:cd`, `08:00:00` | faux | Réussi |
| PL-04 | `erreursHoraires` | Horaires cohérents | `08:00` et `10:30` | aucune erreur | Réussi |
| PL-05 | `erreursHoraires` | Espaces tolérés | `" 08:00 "` et `" 10:30 "` | aucune erreur | Réussi |
| PL-06 | `erreursHoraires` | Début invalide | `8h` et `10:30` | 1 erreur citant « début » et la valeur `8h` | Réussi |
| PL-07 | `erreursHoraires` | Fin invalide | `08:00` et `25:00` | 1 erreur citant « fin » et la valeur `25:00` | Réussi |
| PL-08 | `erreursHoraires` | Deux heures invalides | `xx` et `yy` | 2 erreurs, pas d'erreur d'ordre | Réussi |
| PL-09 | `erreursHoraires` | Fin égale au début | `09:00` et `09:00` | erreur d'ordre des horaires | Réussi |
| PL-10 | `erreursHoraires` | Fin avant le début | `14:00` et `09:30` | erreur d'ordre des horaires | Réussi |
| PL-11 | `erreursHoraires` | Une minute d'écart | `09:00` et `09:01` | aucune erreur | Réussi |
| PL-12 | `erreursHoraires` | Valeur null | `null` et `10:30` | 1 erreur de format, sans exception (aujourd'hui : `NullPointerException`) | Désactivé (bug connu) |
| PL-13 | `champsObligatoiresManquants` | Atelier complet | atelier valide | liste vide | Réussi |
| PL-14 | `champsObligatoiresManquants` | Atelier vide | `new Atelier()` | les 7 champs, dans l'ordre du formulaire (nom, journée, créneau, début, fin, lieu, responsable) | Réussi |
| PL-15 | `champsObligatoiresManquants` | Nom blanc | nom `"   "` | seul le nom est signalé | Réussi |
| PL-16 | `champsObligatoiresManquants` | Journée invalide | identifiant de journée 0, puis -1 | seule la journée est signalée | Réussi |
| PL-17 | `champsObligatoiresManquants` | Créneau null | créneau `null` | seul le créneau est signalé | Réussi |
| PL-18 | `champsObligatoiresManquants` | Horaires, lieu, responsable | début `null`, fin vide, lieu blanc, responsable `null` | début, fin, lieu et responsable signalés | Réussi |
| PL-19 | `messageChampsManquants` | Un seul champ | `lieu` | « L'atelier ne peut pas être enregistré : le champ « lieu » est obligatoire. » | Réussi |
| PL-20 | `messageChampsManquants` | Plusieurs champs | `lieu`, `responsable` | phrase d'introduction suivie d'une liste à puces | Réussi |
| PL-21 | `creerJournee` | Journée valide | nom `"  Finale  "` | nom nettoyé (« Finale »), puis DAO appelé | Réussi |
| PL-22 | `creerJournee` | Nom invalide | nom `null`, puis `"   "` | `IllegalArgumentException`, DAO non appelé | Réussi |
| PL-23 | `mettreAJourJournee` | Mise à jour valide | nom `" Qualifs "` | nom nettoyé (« Qualifs »), puis délégation | Réussi |
| PL-24 | `mettreAJourJournee` | Nom vide | nom `""` | `IllegalArgumentException`, DAO non appelé | Réussi |
| PL-25 | `creerJournee` | Erreur SQL | DAO qui lève une `SQLException` | `SQLException` propagée | Réussi |
| PL-26 | `creerAtelier` | Atelier valide avec espaces | nom, heures, lieu et responsable entourés d'espaces | tous les champs nettoyés, puis DAO appelé | Réussi |
| PL-27 | `creerAtelier` | Un champ manquant | lieu vide | `IllegalArgumentException` avec le message dédié au lieu, DAO non appelé | Réussi |
| PL-28 | `creerAtelier` | Plusieurs champs manquants | `new Atelier()` | message contenant tous les champs (nom, responsable...), DAO non appelé | Réussi |
| PL-29 | `creerAtelier` | Heure invalide | début `8h` | message d'erreur contenant `8h`, DAO non appelé | Réussi |
| PL-30 | `creerAtelier` | Fin avant le début | début `14:00`, fin `09:00` | message d'ordre des horaires, DAO non appelé | Réussi |
| PL-31 | `creerAtelier` | Deux heures invalides | début `xx`, fin `yy` | message de 2 lignes (une erreur par ligne) | Réussi |
| PL-32 | `mettreAJourAtelier` | Mise à jour valide | nom `"  Précision "` | nom nettoyé, puis délégation | Réussi |
| PL-33 | `mettreAJourAtelier` | Atelier invalide | créneau `null` | `IllegalArgumentException`, DAO non appelé | Réussi |
| PL-34 | `validerAtelier` | Atelier valide | atelier complet | pas d'exception | Réussi |
| PL-35 | `listerJournees`, `listerAteliers`, `listerEpreuvesNonAffectees`, `listerEpreuvesDisponibles`, `compterEpreuvesActives`, `compterAteliers` | Délégations | valeurs simulées dans le DAO | mêmes valeurs renvoyées | Réussi |
| PL-36 | `supprimerJournee`, `supprimerAtelier` | Délégations | journée 8, atelier 9 | DAO appelé avec 8 et 9 | Réussi |

### Classe `SuiviParticipantServiceTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| SP-01 | `listerPilotes` | Tri nom puis prénom | « martin Paul », « Dupont Zoé », « DUPONT alice » | DUPONT alice, Dupont Zoé, martin Paul (casse ignorée) | Réussi |
| SP-02 | `listerPilotes` | Nom null | un pilote sans nom, un pilote « Abel » | le pilote sans nom en premier, sans exception | Réussi |
| SP-03 | `listerPilotes` | Liste du DAO intacte | 2 pilotes non triés | la liste d'origine n'est pas modifiée | Réussi |
| SP-04 | `listerPilotes` | Aucun pilote | liste vide | liste vide | Réussi |
| SP-05 | `detailler` | Inscrit sans résultat | épreuve active, pilote inscrit, aucun résultat | statut « pas encore saisi », points `null`, valeurs vides, 1 épreuve attendue | Réussi |
| SP-06 | `detailler` | Résultat saisi | résultat de 42,5 points | statut « saisi », points 42,5 | Réussi |
| SP-07 | `detailler` | Pilote disqualifié | résultat disqualifié | statut « disqualifié » | Réussi |
| SP-08 | `detailler` | Épreuve inactive sans résultat | épreuve inactive | ignorée (0 ligne, 0 attendue) | Réussi |
| SP-09 | `detailler` | Épreuve inactive avec résultat | épreuve inactive, résultat de 15 | conservée, statut « saisi » | Réussi |
| SP-10 | `detailler` | Non inscrit sans résultat | épreuve active sans inscription | ignorée (0 ligne, 0 attendue) | Réussi |
| SP-11 | `detailler` | Non inscrit avec résultat | résultat de 20 sans inscription | conservée, statut « saisi » | Réussi |
| SP-12 | `detailler` | Résultats d'un autre pilote | résultat du pilote 2 uniquement | statut « pas encore saisi » pour le pilote 1 | Réussi |
| SP-13 | `detailler` | Ordre et « compte dans le général » | 3 épreuves, la 2e hors général | lignes dans l'ordre des épreuves, indicateurs vrai/faux/vrai, 3 attendues | Réussi |
| SP-14 | `detailler` | Épreuves attendues | A inscrit avec résultat, B inscrit sans résultat, C non inscrit, D inactive | 2 lignes, 2 épreuves attendues | Réussi |
| SP-15 | `detailler` | Total et rang | classement : p2 = 80 (rang 1), p1 = 55,5 (rang 2), p3 = 10 | pour p1 : total 55,5 et rang 2 | Réussi |
| SP-16 | `detailler` | Pilote absent du classement | pilote non classé | total 0, rang 0 | Réussi |
| SP-17 | `detailler` | Valeur entière avec unité | paramètre « Temps » (s), valeur 12 | `Temps : 12 s` | Réussi |
| SP-18 | `detailler` | Valeur décimale | valeur 3,5 | `Temps : 3.5 s` | Réussi |
| SP-19 | `detailler` | Plusieurs paramètres | « Temps de marche » (min) = 45, « Balises » = 8 | `Temps de marche : 45 min ; Balises : 8` | Réussi |
| SP-20 | `detailler` | Libellé avec parenthèse | libellé « Temps (s) », unité `s` | `Temps (s) : 12` (unité non répétée) | Réussi |
| SP-21 | `detailler` | Unité blanche | unité `"  "` | `Balises : 8` | Réussi |
| SP-22 | `detailler` | Paramètre inconnu | variable `mystere` non déclarée | `mystere : 7` | Réussi |
| SP-23 | `detailler` | Résultat sans valeur | aucune valeur saisie | chaîne vide | Réussi |
| SP-24 | `detailler` | Erreur SQL | `EpreuveDAO` qui lève une `SQLException` | `SQLException` propagée | Réussi |

### Classe `ExportPdfServiceTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| EX-01 | `nomFichierParDefaut` | Nom et date | journée « Finale », 15/06/2026 | `Planning - Finale - 15-06-2026.pdf` | Réussi |
| EX-02 | `nomFichierParDefaut` | Sans date | journée « Finale » sans date | `Planning - Finale.pdf` | Réussi |
| EX-03 | `nomFichierParDefaut` | Nom null | nom `null` | `Planning - journée.pdf` | Réussi |
| EX-04 | `nomFichierParDefaut` | Espaces autour du nom | `"  Finale  "` | `Planning - Finale.pdf` | Réussi |
| EX-05 | `nomFichierParDefaut` | Caractères interdits | `Jour 1/2: test?` | `Planning - Jour 1-2- test-.pdf` | Réussi |
| EX-06 | `genererJournee` | Journée standard | 2 ateliers (matin avec 3 épreuves, autre créneau avec 2) | PDF valide d'au moins une page | Réussi |
| EX-07 | `genererJournee` | Aucun atelier | liste vide | PDF valide d'une seule page | Réussi |
| EX-08 | `genererJournee` | Date absente | journée sans date | pas d'exception, PDF valide | Réussi |
| EX-09 | `genererJournee` | Atelier vide | aucune épreuve, lieu et responsable vides | PDF valide | Réussi |
| EX-10 | `genererJournee` | Lieu et responsable null | `null` et `null` | pas d'exception, PDF valide | Réussi |
| EX-11 | `genererJournee` | Textes très longs | nom, lieu, responsable et journée de plus de 300 caractères | PDF valide (textes découpés ou tronqués) | Réussi |
| EX-12 | `genererJournee` | Beaucoup d'ateliers | 25 ateliers de 4 épreuves | PDF de plusieurs pages | Réussi |
| EX-13 | `genererJournee` | Atelier géant | 1 atelier de 120 épreuves | PDF d'au moins 3 pages, la carte continue sur les pages suivantes | Réussi |
| EX-14 | `genererJournee` | Pages selon le contenu | 2 épreuves, puis 80 épreuves | plus de pages avec 80 épreuves | Réussi |
| EX-15 | `genererJournee` | Deux exports de suite | même journée exportée 2 fois | 2 PDF valides (cache du logo statique) | Réussi |
| EX-16 | `exporterJournee` | Écriture du fichier | fichier `planning.pdf` dans un dossier temporaire | fichier créé, contenant un PDF valide | Réussi |
| EX-17 | `exporterJournee` | Dossier inexistant | fichier dans un sous-dossier absent | `IOException` | Réussi |
| EX-18 | `exporterJournee` | Fichier existant | fichier contenant « ancien contenu » | fichier écrasé par un PDF valide | Réussi |

### Classe `EpreuveDAOTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| DE-01 | `creer`, `trouverParId` | Enregistrer puis relire | « Précision », BAREME, clé `temps`, ASC, 1 paramètre obligatoire, barème 100/80 | id attribué (> 0) ; tous les champs, le paramètre et les 2 paliers relus identiques | Réussi |
| DE-02 | `mettreAJour` | Remplacer paramètres et barème | épreuve renommée « Vitesse 2 », nouveau barème de 2 paliers, plus de paramètre | nom modifié, 2 paliers, 0 paramètre | Réussi |
| DE-03 | `listerToutes` | Tri par ordre | « B » (ordre 2) créée avant « A » (ordre 1) | A puis B | Réussi |
| DE-04 | `supprimer` | Suppression | épreuve existante | `trouverParId` renvoie vide | Réussi |
| DE-05 | `creer` | Création invalide | nom `null` (refusé par le schéma) | `SQLException`, base restée vide, auto-commit rétabli | Réussi |
| DE-06 | `trouverParId` | Id inconnu | id 9999 | vide | Réussi |
| DE-07 | `listerToutes` | Chargement complet | épreuve avec 2 paliers et 1 paramètre | paramètres et barème chargés | Réussi |
| DE-08 | `trouverParId` | Barème dans l'ordre des rangs | paliers saisis dans l'ordre 3, 1, 2 | relus dans l'ordre 1, 2, 3 | Réussi |
| DE-09 | `mettreAJour` | Indicateurs booléens | afficher classement vrai, compte dans le général faux, actif faux | valeurs conservées à la relecture | Réussi |

### Classe `InscriptionDAOTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| DI-01 | `inscrire` | Pas de doublon | même pilote inscrit 2 fois à la même épreuve | 1 seule inscription | Réussi |
| DI-02 | `desinscrire` | Retirer une inscription | pilote inscrit puis désinscrit | plus aucun inscrit | Réussi |
| DI-03 | `inscrireTousLesPilotes` | Inscrire tout le monde sans doubler | 2 pilotes dont 1 déjà inscrit | 2 inscrits | Réussi |
| DI-04 | `inscrirePiloteAToutesLesEpreuvesActives` | Épreuves actives seulement | 1 épreuve active, 1 inactive | inscrit uniquement à l'épreuve active | Réussi |
| DI-05 | `PiloteDAO.supprimer` | Suppression d'un pilote inscrit | pilote inscrit à une épreuve | soit ses inscriptions disparaissent (cascade), soit la suppression est refusée et l'inscription reste | Réussi |
| DI-06 | `listerEpreuvesDuPilote` | Épreuves d'un pilote | pilote inscrit à E1 et E2 | les 2 épreuves | Réussi |
| DI-07 | `inscrireTousLesPilotes` | Aucun pilote | épreuve sans pilote en base | aucun inscrit | Réussi |
| DI-08 | `inscrirePiloteAToutesLesEpreuvesActives` | Appel répété | même appel 2 fois | pas de doublon | Réussi |

### Classe `PiloteDAOTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| DP-01 | `creer`, `trouverParId` | Enregistrer puis relire | licence `LIC-001`, Dupont, Jean, département 31, 80,5 kg, né en 1990, numéro 7, catégorie OPEN | id attribué ; tous les champs relus identiques, date d'inscription renseignée | Réussi |
| DP-02 | `trouverParId` | Champs optionnels absents | pilote sans poids, année ni numéro | relus `null` | Réussi |
| DP-03 | `trouverParId` | Id inconnu | id 9999 | vide | Réussi |
| DP-04 | `creer` | Licence en double | 2 pilotes avec la licence `LIC-001` | `SQLException` | Réussi |
| DP-05 | `mettreAJour` | Numéro de pilote en double | 2 pilotes avec le numéro 7 | `SQLException` | Réussi |
| DP-06 | `mettreAJour` | Modification des champs | nom « Durand », 75 kg, catégorie OPEN | valeurs relues modifiées | Réussi |
| DP-07 | `supprimer` | Suppression | pilote existant | `trouverParId` renvoie vide | Réussi |
| DP-08 | `listerTous` | Tri nom puis prénom | Martin, Dupont, Bernard | Bernard, Dupont, Martin | Réussi |
| DP-09 | `mettreAJour` | Catégorie conservée | catégorie passée à OPEN | OPEN à la relecture | Réussi |
| DP-10 | `creer` | Pilotes sans numéro | 2 pilotes sans numéro de pilote | acceptés (2 pilotes en base) | Réussi |

### Classe `PlanningDAOTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| DL-01 | `creerJournee`, `listerJournees` | Id, ordre et date | journées « J1 » et « J2 », date 15/06/2026 | id attribué, ordre croissant, date relue | Réussi |
| DL-02 | `creerAtelier` | Ordre des épreuves | épreuves ajoutées dans l'ordre E3, E1, E2 | relues dans le même ordre | Réussi |
| DL-03 | `creerAtelier` | Une épreuve, un seul atelier | même épreuve ajoutée à un 2e atelier | le 1er atelier perd l'épreuve, 1 seule affectation au total | Réussi |
| DL-04 | `listerEpreuvesNonAffectees`, `listerEpreuvesDisponibles` | Épreuves à placer | 1 épreuve placée, 1 libre | non affectées : « Libre » ; disponibles pour l'atelier : 2 (libre + déjà dans l'atelier) | Réussi |
| DL-05 | `mettreAJourAtelier` | Champs vides | lieu `"   "`, responsable `""` | enregistrés comme `null` | Réussi |
| DL-06 | `supprimerJournee` | Suppression en cascade | journée avec un atelier et une épreuve | journée et atelier supprimés, épreuve conservée et de nouveau « à placer » | Réussi |
| DL-07 | `compterAteliers` | Comptage par journée | J1 avec 2 ateliers, J2 sans atelier | 2 et 0 | Réussi |
| DL-08 | `compterEpreuvesActives` | Épreuves inactives ignorées | 1 active, 1 inactive | 1 | Réussi |
| DL-09 | `supprimerAtelier` | Libérer les épreuves | atelier contenant une épreuve | atelier supprimé, épreuve de nouveau non affectée | Réussi |
| DL-10 | `mettreAJourAtelier` | Remplacer les épreuves | atelier avec E1 remplacée par E2 | l'atelier contient E2, E1 redevient non affectée | Réussi |
| DL-11 | `mettreAJourJournee` | Nom et date | nom « Finale », date 02/01/2027 | valeurs relues modifiées | Réussi |
| DL-12 | `creerJournee` | Journée sans date | date non renseignée | relue `null` | Réussi |

### Classe `ResultatDAOTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| DR-01 | `enregistrer`, `trouver` | Enregistrer puis relire | valeur `distance` = 12,5, 42 points, saisi par `benevole1` | valeurs JSON relues, points, non disqualifié, bénévole et date de saisie renseignés | Réussi |
| DR-02 | `enregistrer` | Mise à jour sans doublon | résultat enregistré à 10 points, puis à 55 points et disqualifié | 1 seul résultat, 55 points, disqualifié | Réussi |
| DR-03 | `enregistrer` | Pilote inexistant | pilote d'id 9999 | `SQLException` | Réussi |
| DR-04 | `listerParEpreuve`, `listerTous` | Listes | 1 pilote, 2 épreuves avec un résultat chacune | 1 résultat pour E1, 2 au total | Réussi |
| DR-05 | `supprimer` | Suppression | résultat existant | `trouver` renvoie vide | Réussi |
| DR-06 | `trouver` | Résultat inconnu | aucun résultat saisi | vide | Réussi |
| DR-07 | `enregistrer` | Plusieurs valeurs | `distance`, `temps` = 3,25, `balises` = 4 | 3 valeurs relues | Réussi |
| DR-08 | `enregistrer` | Deux pilotes sur une épreuve | pilotes à 10 et 20 points | 2 résultats distincts, points corrects pour chacun | Réussi |

### Classe `UtilisateurDAOTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| DU-01 | `creer`, `trouverParIdentifiant` | Créer puis retrouver | compte `alice` actif | id attribué, mot de passe haché et rôle relus, pas de pilote lié | Réussi |
| DU-02 | `trouverParIdentifiant` | Compte inactif | compte `bob` inactif | vide (pas de connexion possible) | Réussi |
| DU-03 | `definirActif` | Réactiver un compte | `bob` inactif, puis activé | retrouvé | Réussi |
| DU-04 | `mettreAJourMotDePasse` | Changer le mot de passe | nouveau hachage `hash-2` | `hash-2` relu | Réussi |
| DU-05 | `creer` | Identifiant en double | 2 comptes `alice` | `SQLException` | Réussi |
| DU-06 | `supprimer` | Suppression | compte `alice` | introuvable et absent de la liste | Réussi |
| DU-07 | `listerTous` | Tri par identifiant | `zoe`, `alice`, `marc` (inactif) | alice, marc, zoe ; le compte inactif est bien listé | Réussi |
| DU-08 | `definirActif` | Désactiver un compte | `alice` actif, puis désactivé | introuvable à la connexion | Réussi |
| DU-09 | `creer` | Compte lié à un pilote | utilisateur `jean` lié au pilote Dupont | identifiant du pilote conservé | Réussi |
| DU-10 | `trouverParIdentifiant` | Identifiant inconnu | `personne` | vide | Réussi |

### Classe `DatabaseManagerTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| DM-01 | `getDataDirectory` | Dossier de données | appel sans argument | chemin absolu, se terminant par `data` | Réussi |

### Classe `CategorieConfigTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| CC-01 | `analyser` | Règle vide | `null`, `""`, `"   "` | aucune plage | Réussi |
| CC-02 | `analyser` | Numéro seul | `46` | 1 plage [46, 46] | Réussi |
| CC-03 | `analyser` | Plage | `100-199` | 1 plage [100, 199] | Réussi |
| CC-04 | `analyser` | Numéro et plage | `46, 100-199` | 2 plages [46, 46] et [100, 199] | Réussi |
| CC-05 | `analyser` | Point-virgule | `5; 10-12, 20` | 3 plages | Réussi |
| CC-06 | `analyser` | Plage inversée | `199-100` | [100, 199] | Réussi |
| CC-07 | `analyser` | Espaces tolérés | `"  7 - 9 ,  12  "` | [7, 9] et [12, 12] | Réussi |
| CC-08 | `analyser` | Séparateurs en trop | `",,46,, ,"` | 1 plage [46, 46] | Réussi |
| CC-09 | `analyser` | Morceaux invalides | `abc, 12, 5-x, -, 3.5, 99999999999` | seule la plage [12, 12] est conservée | Réussi |
| CC-10 | `analyser` | Doublons | `5, 5` | 2 plages (non fusionnées) | Réussi |
| CC-11 | `deduire` | Numéro null | règle `1-1000`, numéro `null` | POMPIER | Réussi |
| CC-12 | `deduire` | Aucune règle | règle vide, numéros 46 et 150 | POMPIER pour les deux | Réussi |
| CC-13 | `deduire` | Numéro isolé | règle `46`, numéros 45, 46, 47 | OPEN seulement pour 46 | Réussi |
| CC-14 | `deduire` | Bornes comprises | règle `100-199`, numéros 99, 100, 150, 199, 200 | POMPIER, OPEN, OPEN, OPEN, POMPIER | Réussi |
| CC-15 | `deduire` | Plusieurs règles | règle `46, 100-199`, numéros 46, 120, 47, 1 | OPEN, OPEN, POMPIER, POMPIER | Réussi |
| CC-16 | `deduire` | Plage inversée | règle `199-100`, numéro 150 | OPEN | Réussi |
| CC-17 | `deduire` | Règle invalide | règle `n'importe quoi`, numéro 46 | POMPIER | Réussi |
| CC-18 | `definirRegle` | Remplacement de la règle | règle `46` puis `50` | 46 devient POMPIER, 50 devient OPEN | Réussi |
| CC-19 | `descriptionNumerosOpen` | Numéros et plages | règle `46, 100-199` | `46, 100-199` | Réussi |
| CC-20 | `descriptionNumerosOpen` | Plage d'un seul numéro | règle `46-46` | `46` | Réussi |
| CC-21 | `descriptionNumerosOpen` | Normalisation | règle `199-100; 5` | `100-199, 5` | Réussi |
| CC-22 | `descriptionNumerosOpen` | Aucune règle | règle vide | chaîne vide | Réussi |

### Classe `MessageErreurUtilTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| ME-01 | `traduire` | Erreur null | `null` | « Une erreur inconnue est survenue. » | Réussi |
| ME-02 | `traduire` | Exception inconnue | `RuntimeException` avec un message technique | message technique générique, sans fuite du message brut | Réussi |
| ME-03 | `traduire` | Exception sans message | `RuntimeException` sans message | message technique générique | Réussi |
| ME-04 | `traduire` | Timeout HTTP | `HttpTimeoutException` | message commençant par « Supabase ne répond pas » | Réussi |
| ME-05 | `traduire` | Timeout socket | `SocketTimeoutException` | message commençant par « Supabase ne répond pas » | Réussi |
| ME-06 | `traduire` | Timeout dans la cause | `RuntimeException` enveloppant un `SocketTimeoutException` | « Supabase ne répond pas » | Réussi |
| ME-07 | `traduire` | Connexion impossible | `ConnectException`, `UnknownHostException`, `NoRouteToHostException`, `UnresolvedAddressException` | « Impossible de joindre Supabase » pour chacune | Réussi |
| ME-08 | `traduire` | Connexion impossible dans la cause | `IOException` enveloppant un `UnknownHostException` | « Impossible de joindre Supabase » | Réussi |
| ME-09 | `traduire` | URL Supabase invalide | 3 `IllegalArgumentException` (caractère interdit, schéma attendu, URL non absolue) | « L'adresse (URL) Supabase n'est pas valide » | Réussi |
| ME-10 | `traduire` | Message Supabase en français | « Supabase a refusé la requête (HTTP 401)... » | message renvoyé tel quel | Réussi |
| ME-11 | `traduire` | Message de synchronisation | « La synchronisation a échoué : 3 pilotes non envoyés. » | message renvoyé tel quel | Réussi |
| ME-12 | `traduire` | Priorité Supabase sur doublon | message Supabase contenant « duplicate key » | renvoyé tel quel, pas pris pour un doublon local | Réussi |
| ME-13 | `traduire` | Doublon de numéro de pilote | `SQLException` H2 sur `NUMERO_PILOTE` | « Ce numéro de pilote est déjà attribué à un autre pilote. » | Réussi |
| ME-14 | `traduire` | Doublon de licence | `SQLException` H2 sur `NUMERO_LICENCE` | « Ce numéro de licence est déjà utilisé par un autre pilote. » | Réussi |
| ME-15 | `traduire` | Doublon sur la table pilotes | « Duplicate entry in table PILOTES » (sans colonne) | message sur la licence | Réussi |
| ME-16 | `traduire` | Doublon d'identifiant | doublon sur `IDENTIFIANT`, puis sur la table `UTILISATEURS` | « Cet identifiant de compte est déjà utilisé. Choisissez-en un autre. » | Réussi |
| ME-17 | `traduire` | Autre doublon | doublon sur `EPREUVES(NOM)` | message générique de doublon | Réussi |
| ME-18 | `traduire` | Doublon hors SQL | `RuntimeException("Duplicate key")` | message générique de doublon | Réussi |
| ME-19 | `traduire` | Clé étrangère | « Referential integrity constraint violation... », « violates foreign key constraint » | « Impossible de supprimer cet élément : il est encore utilisé ailleurs (résultats, inscriptions...). » | Réussi |
| ME-20 | `traduire` | Champ obligatoire | « NULL not allowed for column "NOM" », « violates not-null constraint » | « Un champ obligatoire n'a pas été renseigné. Merci de vérifier votre saisie. » | Réussi |
| ME-21 | `traduire` | Erreur de conversion | 2 `SQLException` de conversion, `NumberFormatException("abc")` | « Une valeur saisie n'est pas au bon format (un nombre est attendu). » | Réussi |
| ME-22 | `traduire` | `NumberFormatException` sans message | `new NumberFormatException()` | « Une valeur numérique attendue n'est pas valide. » | Réussi |
| ME-23 | `traduire` | Autre `SQLException` | « Database may be already in use » | message « Impossible d'accéder à la base de données locale », sans fuite du texte brut | Réussi |
| ME-24 | `traduire` | Variable de formule inconnue | 3 formulations (« is undefined », « not found », « Unknown variable ») | « La formule utilise un nom de variable inconnu » | Réussi |
| ME-25 | `traduire` | Priorité variable inconnue | `IllegalArgumentException("Variable 'temps' not found")` | message dédié à la formule, pas le message brut | Réussi |
| ME-26 | `traduire` | `IllegalArgumentException` en français | « Le nom de l'épreuve est obligatoire » | message renvoyé tel quel | Réussi |
| ME-27 | `traduire` | `IllegalStateException` en français | « L'épreuve à dupliquer n'existe plus. » | message renvoyé tel quel | Réussi |
| ME-28 | `traduire` | Exception sans message utile | `IllegalArgumentException()`, `IllegalStateException("   ")` | « La demande n'a pas pu être traitée. » | Réussi |
| ME-29 | `traduire` | Message multi-lignes | message d'atelier invalide avec puces | message conservé tel quel (sauts de ligne compris) | Réussi |

## 8. Limites connues et suite

**Limites actuelles**

- Les services sont testés avec de faux DAO, et les DAO avec une vraie base : aucun test ne vérifie encore les deux ensemble.
- Le test PL-12 est désactivé : `erreursHoraires(null, ...)` lève aujourd'hui une `NullPointerException`. À réactiver une fois le bug corrigé.
- CS-03 attend une `Exception` générique : à préciser avec l'exception réelle d'exp4j.
- Les tests de barème vérifient l'état des objets, pas les appels à `resultatDAO.enregistrer` (pas de `verify`).
- DI-05 accepte deux comportements (suppression en cascade ou refus) : le comportement attendu du schéma reste à fixer.
- Les tests de `ExportPdfService` comptent les pages en cherchant `/Type /Page` : cela suppose un PDF non compressé. Ils ne vérifient pas le texte affiché dans le PDF.
- `UtilisateurDAOTest` utilise le premier rôle de l'énumération `Role` : les rôles ne sont pas testés séparément.
- `DatabaseManager` : seul le dossier de données est testé (pas l'ouverture réelle de la base ni la création du schéma).
- `CategorieConfig` : la lecture du fichier `data/categories.properties` n'est pas testée (la règle est injectée avec `definirRegle`).
- `mockConstruction` est un contournement : l'injection des DAO par le constructeur serait plus propre.

**À traiter ensuite**

- Corriger le bug de `erreursHoraires` puis réactiver PL-12.
- Tests de synchronisation Supabase (package `sync`) et de l'application mobile.
- Tests de `SecuriteUtil` (hachage des mots de passe).
- Tests d'intégration services + DAO sur la base H2 en mémoire.
