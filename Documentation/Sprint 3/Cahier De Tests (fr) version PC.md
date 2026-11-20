# Cahier de tests (version PC)

> Ce document décrit **comment** la logique métier de l'application PC est testée : environnement, stratégie, cas de tests et règles encore à confirmer. L'architecture et la logique métier sont décrites dans le [Document technique](https://github.com/PPS-SAE-S5/PPS-COMP-2027/tree/main/Documentation).
>
> **Version 1** : ce cahier couvre pour l'instant les tests unitaires de `CalculService` (application PC, JUnit 5 et Mockito).

## Sommaire

- [Objectif et périmètre](#1-objectif-et-périmètre)
- [Environnement](#2-environnement)
- [Lancer les tests](#3-lancer-les-tests)
- [Stratégie de test](#4-stratégie-de-test)
- [Mise en place et fonctions utilitaires](#5-mise-en-place-et-fonctions-utilitaires)
- Cas de tests
- Règles à confirmer
- Limites connues et suite

---

## 1. Objectif et périmètre

Ce cahier permet de vérifier que les règles de calcul des points fonctionnent et de détecter rapidement les régressions après chaque modification du code.

| | Éléments |
|---|---|
| **Testé** | `CalculService` : `evaluerFormule`, `recalculerBareme`, `calculerEtEnregistrerFormule` |
| **Non testé pour le moment** | `ClassementService`, `enregistrerValeurBareme`, DAO et SQL, contrôleurs JavaFX, synchronisation Supabase |

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

`CalculService` crée ses DAO avec `new` dans ses champs : sans précaution, chaque test ouvrirait la vraie base H2. Les tests utilisent donc des **faux DAO** (Mockito `mockConstruction`), sans modifier le code de production.

| | Détail |
|---|---|
| **Ce que ça prouve** | Si la base renvoie ces résultats, les points sont correctement calculés (tri, rangs, disqualifiés, barème, formules) |
| **Ce que ça ne prouve pas** | Que le SQL des DAO est correct, que le JSON des valeurs est bien relu, que le schéma correspond au code |

Les tests d'intégration sur une base H2 en mémoire compléteront cette approche.

## 5. Mise en place et fonctions utilitaires

| Élément | Rôle |
|---|---|
| `setUp` (`@BeforeEach`) | Active `mockConstruction` sur `ResultatDAO`, `PiloteDAO`, `InscriptionDAO`, **puis** crée un `CalculService` neuf et récupère le faux `ResultatDAO` |
| `tearDown` (`@AfterEach`) | Ferme les interceptions pour ne pas fausser les autres tests |
| `epreuveBareme(cle, sens, points...)` | Construit une épreuve BAREME (id 1). `sens` : DESC (plus grand gagne) ou ASC (plus petit gagne). Le 1er montant est le rang 1 |
| `resultat(piloteId, valeurs, disqualifie)` | Construit le résultat d'un pilote sur l'épreuve 1, points à 0 au départ |
| `epreuveFormule(formule)` | Construit une épreuve FORMULE (id 1) |

Chaque test repart d'un état vierge : les tests sont indépendants.
