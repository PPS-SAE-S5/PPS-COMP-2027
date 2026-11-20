# Cahier de tests (version PC)

> Ce document décrit **comment** la logique métier de l'application PC est testée : environnement, stratégie, cas de tests et règles encore à confirmer. L'architecture et la logique métier sont décrites dans le [Document technique](https://github.com/PPS-SAE-S5/PPS-COMP-2027/tree/main/Documentation).
>
> **Version 1** : ce cahier couvre pour l'instant les tests unitaires de `CalculService` (application PC, JUnit 5 et Mockito).

## Sommaire

- Objectif et périmètre
- Environnement
- Lancer les tests
- Stratégie de test
- Mise en place et fonctions utilitaires
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
