# Cahier de tests (version PC)

> Ce document décrit **comment** la logique métier de l'application PC est testée : environnement, stratégie, cas de tests et règles encore à confirmer. L'architecture et la logique métier sont décrites dans le [Document technique](https://github.com/PPS-SAE-S5/PPS-COMP-2027/tree/main/Documentation).
>
> **Version 2** : ce cahier couvre les tests unitaires des services de l'application PC (JUnit 5 et Mockito).

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

Ce cahier permet de vérifier que les règles de calcul des points fonctionnent et de détecter rapidement les régressions après chaque modification du code.

| | Éléments |
|---|---|
| **Testé** | `CalculService`, `ClassementService`, `EpreuveService`, `PiloteService`, `PlanningService`, `SuiviParticipantService`, `AuthService`, `ExportPdfService` |
| **Non testé pour le moment** | DAO et SQL, package `sync` (synchronisation Supabase), `SecuriteUtil`, contrôleurs JavaFX, application mobile |

## 2. Environnement

| Outil | Version | Rôle |
|---|---|---|
| Java | release 17 (JDK 24) | Langage |
| Maven | 3.x | Compilation et lancement des tests |
| JUnit Jupiter | 5.10.2 | Écriture et exécution des tests |
| Mockito | 5.18.0 | Faux objets (DAO simulés) |
| Maven Surefire | 3.5.4 | Plugin d'exécution des tests |

> Mockito 5.18.0 est nécessaire avec le JDK 24 : la version 5.11.0 échoue avec l'erreur « Java 24 (68) is not supported ».

## 3. Lancer les tests

Depuis le dossier `Version PC/parapente-pps` (celui qui contient le `pom.xml`) :

```
mvn test                                          # tous les tests
mvn -Dtest=CalculServiceTest test                 # une seule classe
mvn -Dtest=CalculServiceTest#formuleSimple test   # une seule méthode
```

Résultat attendu : `BUILD SUCCESS`, `Failures: 0, Errors: 0`. Les avertissements Mockito sur l'agent Java sont sans conséquence.

Les tests sont dans `src/test/java/com/pps/parapente/service/`, dans le même package que la classe testée, et ne sont jamais inclus dans l'application livrée.

## 4. Stratégie de test

Les services créent leurs DAO avec `new` dans leurs champs : sans précaution, chaque test ouvrirait la vraie base H2. Les tests utilisent donc des **faux DAO** (Mockito `mockConstruction`), sans modifier le code de production.

| Technique | Utilisée pour |
|---|---|
| `mockConstruction` | Remplacer les DAO créés avec `new` à l'intérieur des services |
| `mockStatic` | Remplacer les classes statiques (`SecuriteUtil`, `SessionManager`) dans `AuthServiceTest` |
| `ArgumentCaptor` et `verify` | Contrôler ce qui est réellement envoyé au DAO (bénévole, pilote, ordre des appels) |
| Aucun mock | Méthodes statiques de `PlanningService` (validation des horaires) et `ExportPdfService`, qui génère de vrais PDF vérifiés en mémoire et dans un dossier temporaire (`@TempDir`) |

| | Détail |
|---|---|
| **Ce que ça prouve** | Si la base renvoie ces données, la logique métier produit le bon résultat (calculs, validations, tris, formats) |
| **Ce que ça ne prouve pas** | Que le SQL des DAO est correct, que le JSON des valeurs est bien relu, que le schéma correspond au code |

Les tests d'intégration sur une base H2 en mémoire compléteront cette approche (voir [Limites connues et suite](#8-limites-connues-et-suite)).

## 5. Mise en place et fonctions utilitaires

| Élément | Rôle |
|---|---|
| `setUp` (`@BeforeEach`) | Active `mockConstruction` sur `ResultatDAO`, `PiloteDAO`, `InscriptionDAO`, **puis** crée un `CalculService` neuf et récupère le faux `ResultatDAO` |
| `tearDown` (`@AfterEach`) | Ferme les interceptions pour ne pas fausser les autres tests |
| `epreuveBareme(cle, sens, points...)` | Construit une épreuve BAREME (id 1). `sens` : DESC (plus grand gagne) ou ASC (plus petit gagne). Le 1er montant est le rang 1 |
| `resultat(piloteId, valeurs, disqualifie)` | Construit le résultat d'un pilote sur l'épreuve 1, points à 0 au départ |
| `epreuveFormule(formule)` | Construit une épreuve FORMULE (id 1) |

Chaque test repart d'un état vierge : les tests sont indépendants.

## 6. Synthèse par classe

| Classe testée | Fichier de test | Tests | Ce qui est vérifié |
|---|---|---|---|
| `CalculService` | `CalculServiceTest` | 16 | Formules, barème (tri, rangs, cas limites), enregistrement |
| `ClassementService` | `ClassementServiceTest` | 12 | Classement général et par épreuve, règle « inscrit sans résultat = 0 point », filtres genre et catégorie |
| `EpreuveService` | `EpreuveServiceTest` | 18 | Validation, création, mise à jour, duplication |
| `PiloteService` | `PiloteServiceTest` | 18 | Validation de la fiche pilote, inscription aux épreuves actives |
| `PlanningService` | `PlanningServiceTest` | 36 | Horaires, champs obligatoires, journées, ateliers (1 test désactivé : bug connu) |
| `SuiviParticipantService` | `SuiviParticipantServiceTest` | 24 | Détail par pilote : statuts, total, rang, formatage des valeurs |
| `AuthService` | `AuthServiceTest` | 5 | Connexion, déconnexion, erreurs |
| `ExportPdfService` | `ExportPdfServiceTest` | 18 | Nom de fichier, génération et pagination du PDF, écriture du fichier |

Nombres issus de la dernière exécution de `mvn test`, à mettre à jour après chaque ajout.

## 7. Cas de tests

### Classe `AuthServiceTest`

| ID | Fonction | Objectif | Entrée | Résultat attendu | Statut |
|---|---|---|---|---|---|
| CS-01 | `connexionReussie` | Identifiants valides | Identifiant `alice`, mot de passe `secret123` | Session ouverte | Réussi |
| CS-02 | `mauvaisIdentifiant` | Identifiant inconnu | Identifiant `inconnu`, mot de passe `secret123` | Pas d'ouverture de session | Réussi |
| CS-03 | `mauvaisMotdePasse` | Mot de passe erroné| Identifiant `alice`, mot de passe `mauvais` | N'ouvre pas de session | Réussi |
| CS-04 | `erreurSQL` | Erreur SQL | Identifiant `alice`, mot de passe `secret123`, DAO déclenchant une `SQLException` | SQLException, pas de session | Réussi |
| CS-05 | `seDeconnecter` | Deconnexion | Appel de `seDeconnecter()` | Appelle `SessionManager.deconnecter()` une seule fois | Réussi |


### Classe `CalculService`

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

## 8. Limites connues et suite

**Limites actuelles**

- Les DAO sont simulés : le SQL et le schéma ne sont pas vérifiés.
- CS-03 attend une `Exception` générique : à préciser avec l'exception réelle d'exp4j.
- Les tests de barème vérifient l'état des objets, pas les appels à `resultatDAO.enregistrer` (pas de `verify`).
- `mockConstruction` est un contournement : l'injection des DAO par le constructeur serait plus propre.

**À traiter ensuite**

- Tests de `enregistrerValeurBareme` et de `ClassementService` (règle « pilote inscrit sans résultat = 0 point »).
- Tests d'intégration des DAO avec une base H2 en mémoire.
- Tests de synchronisation Supabase et de l'application mobile.
