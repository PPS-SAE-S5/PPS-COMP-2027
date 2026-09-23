# DOCUMENTATION UTILISATEUR

> **/!\ Cette documentation est une ébauche réalisée dans le cadre du Sprint 0 du projet. Il s'agit donc d'une v0.**
>
> À ce stade, l'application est encore en phase de conception et de développement. La documentation utilisateur complète, incluant les procédures détaillées, les captures d'écran et les guides d'utilisation, sera disponible à partir du Sprint 2.

---

# Table des matières

1. [Aperçu du projet](#1-aperçu-du-projet)

   * [1.1 Contexte du projet](#11-contexte-du-projet)
   * [1.2 Présentation de l'application](#12-présentation-de-lapplication)
   * [1.3 Objectifs](#13-objectifs)
   * [1.4 Utilisateurs concernés](#14-utilisateurs-concernés)

2. [Exigences](#2-exigences)

   * [2.1 Exigences fonctionnelles](#21-exigences-fonctionnelles)
   * [2.2 Exigences d'ergonomie et d'accessibilité](#22-exigences-dergonomie-et-daccessibilité)
   * [2.3 Évolutivité et paramétrage des épreuves](#23-évolutivité-et-paramétrage-des-épreuves)
   * [2.4 Gestion des droits et des accès](#24-gestion-des-droits-et-des-accès)

3. [Prise en main](#3-prise-en-main)

   * [3.1 Accéder à l'application](#31-accéder-à-lapplication)
   * [3.2 Présentation de l'interface](#32-présentation-de-linterface)
   * [3.3 Navigation](#33-navigation)

4. [Authentification et comptes](#4-authentification-et-comptes)

   * [4.1 Se connecter](#41-se-connecter)
   * [4.2 Se déconnecter](#42-se-déconnecter)
   * [4.3 Gérer son compte](#43-gérer-son-compte)

5. [Gestion des pilotes](#5-gestion-des-pilotes)

   * [5.1 Ajouter un pilote](#51-ajouter-un-pilote)
   * [5.2 Consulter les pilotes](#52-consulter-les-pilotes)
   * [5.3 Modifier un pilote](#53-modifier-un-pilote)
   * [5.4 Supprimer un pilote](#54-supprimer-un-pilote)

6. [Gestion des épreuves](#6-gestion-des-épreuves)

   * [6.1 Consulter les épreuves](#61-consulter-les-épreuves)
   * [6.2 Créer une épreuve](#62-créer-une-épreuve)
   * [6.3 Paramétrer une épreuve](#63-paramétrer-une-épreuve)
   * [6.4 Modifier une épreuve](#64-modifier-une-épreuve)
   * [6.5 Supprimer une épreuve](#65-supprimer-une-épreuve)
   * [6.6 Configurer les règles d'une épreuve](#66-configurer-les-règles-dune-épreuve)

7. [Saisie et gestion des résultats](#7-saisie-et-gestion-des-résultats)

   * [7.1 Saisir les résultats](#71-saisir-les-résultats)
   * [7.2 Consulter les résultats](#72-consulter-les-résultats)
   * [7.3 Modifier les résultats](#73-modifier-les-résultats)
   * [7.4 Valider les résultats](#74-valider-les-résultats)

8. [Classements et notes](#8-classements-et-notes)

   * [8.1 Calcul automatique des notes](#81-calcul-automatique-des-notes)
   * [8.2 Génération automatique des classements](#82-génération-automatique-des-classements)
   * [8.3 Consulter les classements](#83-consulter-les-classements)
   * [8.4 Consulter les notes](#84-consulter-les-notes)

9. [Gestion des utilisateurs](#9-gestion-des-utilisateurs)

   * [9.1 Consulter les utilisateurs](#91-consulter-les-utilisateurs)
   * [9.2 Ajouter un utilisateur](#92-ajouter-un-utilisateur)
   * [9.3 Modifier un utilisateur](#93-modifier-un-utilisateur)
   * [9.4 Supprimer un utilisateur](#94-supprimer-un-utilisateur)

10. [Droits et fonctionnalités](#10-droits-et-fonctionnalités)

    * [10.1 Types d'utilisateurs](#101-types-dutilisateurs)
    * [10.2 Fonctionnalités disponibles](#102-fonctionnalités-disponibles)


11. [Annexes](#11-annexes)

    * [12.1 Glossaire](#121-glossaire)
    * [12.2 Informations complémentaires](#122-informations-complémentaires)

---

# 1. Aperçu du projet

## 1.1 Contexte du projet

L'application a pour objectif de permettre la gestion des épreuves et des pilotes, ainsi que la saisie et le traitement des résultats.

Le projet doit répondre aux besoins actuels du client tout en restant suffisamment flexible pour s'adapter à de nouveaux besoins dans le futur.

De nouvelles épreuves ou de nouvelles règles pouvant apparaître, l'application doit être conçue pour permettre la création et la configuration de nouveaux types d'épreuves sans nécessiter une modification importante de l'application.

## 1.2 Présentation de l'application

L'application permet de centraliser la gestion des épreuves, des pilotes et des résultats.

Elle permet notamment de :

* gérer les pilotes ;
* créer et gérer les épreuves ;
* configurer les règles des épreuves ;
* saisir les résultats ;
* calculer automatiquement les notes ;
* générer automatiquement les classements ;
* consulter les résultats et les classements ;
* gérer les comptes utilisateurs.

L'objectif est de proposer une application simple à utiliser, tout en permettant une évolution dans le temps.

## 1.3 Objectifs

Les principaux objectifs de l'application sont :

* simplifier la gestion des épreuves ;
* faciliter la gestion des pilotes ;
* réduire les erreurs lors de la saisie des résultats ;
* automatiser le calcul des notes et des classements ;
* permettre la création de nouvelles épreuves ;
* rendre les règles des épreuves paramétrables ;
* proposer une interface claire et facile à utiliser ;
* permettre à l'application d'évoluer avec les besoins du client.

## 1.4 Utilisateurs concernés

L'application est destinée à différents types d'utilisateurs, notamment :

* les bénévoles ;
* les responsables d'épreuves ;
* les pilotes ;
* le comité des pilotes.

Les fonctionnalités accessibles pourront être définies en fonction des besoins du projet.

---

# 2. Exigences

## 2.1 Exigences fonctionnelles

Les principales fonctionnalités attendues sont :

* gestion des pilotes ;
* gestion des épreuves ;
* saisie et modification des résultats ;
* calcul automatique des notes ;
* génération automatique des classements ;
* gestion des comptes utilisateurs ;
* consultation des résultats et des classements.

## 2.2 Exigences d'ergonomie et d'accessibilité

L'application doit être utilisable par différents types d'utilisateurs, y compris des personnes peu familières avec les outils numériques.

L'interface doit donc être :

* claire ;
* lisible ;
* intuitive ;
* cohérente ;
* simple à prendre en main.

Les fonctionnalités doivent être facilement identifiables et accessibles.

## 2.3 Évolutivité et paramétrage des épreuves

L'application doit être conçue pour pouvoir évoluer dans le temps.

Elle ne doit pas être limitée à une liste fixe de types d'épreuves. Le client doit pouvoir créer de nouvelles épreuves en fonction de ses besoins.

Les caractéristiques et les règles d'une épreuve doivent donc être suffisamment paramétrables pour permettre l'ajout de nouvelles épreuves sans devoir modifier profondément l'application.

## 2.4 Gestion des droits et des accès

Les utilisateurs doivent pouvoir se connecter à l'application à l'aide de leur compte.

Les droits d'accès pourront être adaptés selon les besoins du projet et les responsabilités des utilisateurs.

---

# 3. Prise en main

## 3.1 Accéder à l'application

L'utilisateur accède à l'application depuis son navigateur.

La procédure détaillée d'accès à l'application sera ajoutée dans une prochaine version de la documentation.

> **À compléter au Sprint 2 :**
>
> * adresse de l'application ;
> * prérequis ;
> * navigateurs compatibles ;
> * captures d'écran.

## 3.2 Présentation de l'interface

L'interface de l'application doit permettre à l'utilisateur d'accéder facilement aux principales fonctionnalités.

Les principales fonctionnalités seront accessibles depuis la navigation principale :

* pilotes ;
* épreuves ;
* résultats ;
* classements ;
* comptes utilisateurs.

> **À compléter au Sprint 2 avec des captures d'écran de l'interface.**

## 3.3 Navigation

La navigation doit être simple et intuitive.

L'utilisateur doit pouvoir accéder rapidement aux différentes fonctionnalités sans avoir à effectuer un nombre important d'actions.

---

# 4. Authentification et comptes

## 4.1 Se connecter

L'utilisateur doit disposer d'un compte pour accéder à l'application.

La connexion se fait à l'aide des informations d'authentification demandées par l'application.

> **Procédure détaillée à compléter au Sprint 2.**

## 4.2 Se déconnecter

L'utilisateur peut se déconnecter de l'application afin de terminer sa session.

> **Procédure détaillée à compléter au Sprint 2.**

## 4.3 Gérer son compte

L'utilisateur peut consulter et, selon les fonctionnalités disponibles, modifier les informations associées à son compte.

---

# 5. Gestion des pilotes

## 5.1 Ajouter un pilote

Un utilisateur autorisé peut ajouter un nouveau pilote dans l'application.

Les informations nécessaires devront être renseignées dans le formulaire prévu à cet effet.

> **À compléter avec les champs du formulaire et une capture d'écran.**

## 5.2 Consulter les pilotes

La liste des pilotes enregistrés dans l'application peut être consultée depuis la section dédiée.

L'utilisateur peut rechercher ou consulter les informations disponibles pour chaque pilote.

## 5.3 Modifier un pilote

Les informations d'un pilote peuvent être modifiées lorsque cela est nécessaire.

## 5.4 Supprimer un pilote

Un pilote peut être supprimé de l'application lorsque cette action est autorisée.

Une confirmation pourra être demandée afin d'éviter une suppression accidentelle.

---

# 6. Gestion des épreuves

## 6.1 Consulter les épreuves

L'utilisateur peut consulter les épreuves disponibles dans l'application.

Les informations affichées peuvent notamment comprendre le nom de l'épreuve, sa configuration et son état.

## 6.2 Créer une épreuve

La création d'une épreuve permet d'ajouter un nouveau type d'épreuve à l'application.

L'utilisateur doit renseigner les informations nécessaires à sa configuration.

## 6.3 Paramétrer une épreuve

Une épreuve doit pouvoir être configurée en fonction de ses caractéristiques et de ses règles.

Le paramétrage doit permettre de définir les éléments nécessaires au calcul des résultats, des notes et du classement.

## 6.4 Modifier une épreuve

Les paramètres d'une épreuve peuvent être modifiés afin de prendre en compte une évolution des besoins ou des règles.

## 6.5 Supprimer une épreuve

Une épreuve peut être supprimée lorsque cette action est autorisée et qu'elle ne compromet pas les données déjà enregistrées.

## 6.6 Configurer les règles d'une épreuve

Les règles d'une épreuve doivent être paramétrables afin de permettre la création de nouveaux types d'épreuves.

Cette fonctionnalité constitue un élément important de l'évolutivité de l'application.

---

# 7. Saisie et gestion des résultats

## 7.1 Saisir les résultats

Les résultats des pilotes peuvent être saisis après ou pendant une épreuve, selon le fonctionnement défini par l'application.

La saisie doit permettre d'associer les résultats au pilote et à l'épreuve concernés.

## 7.2 Consulter les résultats

Les résultats enregistrés peuvent être consultés afin de vérifier les performances des pilotes.

## 7.3 Modifier les résultats

En cas d'erreur lors de la saisie, les résultats peuvent être modifiés par un utilisateur autorisé.

## 7.4 Valider les résultats

Une étape de validation peut être utilisée afin de confirmer les résultats avant la génération définitive des notes et des classements.

---

# 8. Classements et notes

## 8.1 Calcul automatique des notes

L'application doit calculer automatiquement les notes à partir des résultats saisis et des règles configurées pour l'épreuve.

Le calcul doit respecter les paramètres définis pour chaque type d'épreuve.

## 8.2 Génération automatique des classements

Une fois les résultats enregistrés, l'application doit pouvoir générer automatiquement les classements.

Les classements sont calculés à partir des résultats et des règles définies pour l'épreuve.

## 8.3 Consulter les classements

Les utilisateurs peuvent consulter les classements générés par l'application.

## 8.4 Consulter les notes

Les notes obtenues par les pilotes peuvent être consultées à partir des résultats de l'épreuve.

---

# 9. Gestion des utilisateurs

## 9.1 Consulter les utilisateurs

Les utilisateurs enregistrés dans l'application peuvent être consultés depuis la section dédiée à la gestion des comptes.

## 9.2 Ajouter un utilisateur

Un nouvel utilisateur peut être ajouté en renseignant les informations nécessaires à la création de son compte.

## 9.3 Modifier un utilisateur

Les informations associées à un compte utilisateur peuvent être modifiées lorsque cela est nécessaire.

## 9.4 Supprimer un utilisateur

Un compte utilisateur peut être supprimé lorsque cette action est autorisée.

---

# 10. Droits et fonctionnalités

## 10.1 Types d'utilisateurs

L'application peut être utilisée par plusieurs profils :

* **Bénévoles** : gestion des pilotes et saisie des résultats ;
* **Responsables d'épreuve** : gestion des pilotes, des résultats et des épreuves ;
* **Pilotes** : consultation de leurs informations, résultats et classements ;
* **Comité des pilotes** : consultation des résultats et classements.

Tous les utilisateurs disposent d'un compte leur permettant de se connecter à l'application.

## 10.2 Fonctionnalités disponibles

Les fonctionnalités accessibles dépendent du rôle et des responsabilités définis dans l'application.

Les principales fonctionnalités sont :

| Fonctionnalité            | Bénévole  | Responsable d'épreuve | Pilote    | Comité des pilotes |
| ------------------------- | --------- | --------------------- | --------- | ------------------ |
| Se connecter              | ✓         | ✓                     | ✓         | ✓                  |
| Gérer les pilotes         | ✓         | ✓                     | À définir | À définir          |
| Gérer les épreuves        | À définir | ✓                     | À définir | À définir          |
| Saisir les résultats      | ✓         | ✓                     | À définir | À définir          |
| Consulter les résultats   | ✓         | ✓                     | ✓         | ✓                  |
| Consulter les classements | ✓         | ✓                     | ✓         | ✓                  |
| Consulter les notes       | ✓         | ✓                     | ✓         | ✓                  |
| Gérer les comptes         | À définir | À définir             | À définir | À définir          |

> **Remarque :** ce tableau sera précisé lorsque les règles de gestion des droits auront été définitivement définies.

---


---

# 11. Annexes

## 11.1 Glossaire

| Terme           | Définition                                                               |
| --------------- | ------------------------------------------------------------------------ |
| **Épreuve**     | Activité ou compétition pour laquelle des résultats sont enregistrés.    |
| **Pilote**      | Participant prenant part à une épreuve.                                  |
| **Résultat**    | Donnée enregistrée pour un pilote lors d'une épreuve.                    |
| **Note**        | Valeur calculée à partir des résultats et des règles de l'épreuve.       |
| **Classement**  | Ordre des pilotes obtenu à partir des résultats et des règles de calcul. |
| **Paramétrage** | Configuration des caractéristiques et règles d'une épreuve.              |

## 11.2 Informations complémentaires

Cette documentation sera mise à jour au fur et à mesure de l'avancement du projet.


