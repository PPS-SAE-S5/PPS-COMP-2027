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
