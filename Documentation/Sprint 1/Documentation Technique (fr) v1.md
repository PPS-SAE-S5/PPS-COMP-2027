# Documentation Technique

## Table des matières

1. [Aperçu du projet](#1-aperçu-du-projet)
   - 1.1 [Contexte du projet](#11-contexte-du-projet)
   - 1.2 [Objectifs](#12-objectifs)
2. [Exigences](#2-exigences)
   - 2.1 [Exigences fonctionnelles](#21-exigences-fonctionnelles)
   - 2.2 [Exigences non fonctionnelles](#22-exigences-non-fonctionnelles)
   - 2.3 [Utilisateurs et rôles](#23-utilisateurs-et-rôles)
3. [Cas d'utilisation](#3-cas-dutilisation)
   - 3.1 [Diagramme de cas d'utilisation](#31-diagramme-de-cas-dutilisation)
   - 3.2 [Description des cas d'utilisation](#32-description-des-cas-dutilisation)
4. [User Stories et Backlog](#4-user-stories-et-backlog)
   - 4.1 [User Stories](#41-user-stories)
   - 4.2 [Product Backlog](#42-product-backlog)
   - 4.3 [Planification des sprints](#43-planification-des-sprints)
5. [Conception du système](#5-conception-du-système)
   - 5.1 [Architecture](#51-architecture)
   - 5.2 [Conception de la base de données](#52-conception-de-la-base-de-données)
   - 5.3 [API](#53-api)
6. [Technologies](#6-technologies)
   - 6.1 [Frontend](#61-frontend)
   - 6.2 [Backend](#62-backend)
   - 6.3 [Base de données](#63-base-de-données)
7. [Tests](#7-tests)
   - 7.1 [Stratégie de test](#71-stratégie-de-test)
   - 7.2 [Tests](#72-tests)
8. [Déploiement](#8-déploiement)
9. [Améliorations futures](#9-améliorations-futures)

---

## 1. Aperçu du projet

### 1.1 Contexte du projet

Le club de parapente Parapente Pays de Sault (PPS), situé dans l'Aude, organisera à l'été 2027 le Championnat de France des Pompiers de Parapente
Actuellement, l'organisation de cette compétition repose principalement sur l'utilisation de documents papier et de fichiers Excel. Les informations et les résultats sont collectés et traités manuellement, notamment afin de réaliser les classements provisoires à la fin de chaque journée
Afin de moderniser et de simplifier ce processus, le club souhaite mettre en place une solution numérique permettant de centraliser les différentes informations liées à la compétition et de faciliter leur gestion

### 1.2 Objectifs

L'objectif du projet est de concevoir et de développer une application mobile et une application pc et une page web dédiée à la gestion d'une compétition de parapente .

les clients vont pouvoir utiliser l'application molli ou PC selon leur besoin , Les applications marchent avec un serveur et une base de  données en ligne, mais aussi sans internet en utilisant une base de données locale, et les données vont être transmises sur internet lorsque la connexion est établie.

Il y a aussi des rôles différents qui ont des droits différents.

Selon les exigences du client, l’application doit être conçue de manière durable et évolutive afin de pouvoir s’adapter aux besoins futurs. De nouvelles épreuves ou de nouvelles règles pourront être introduites à l’avenir. L’application ne doit donc pas être limitée à un ensemble prédéfini de types d’épreuves.

Le client souhaite que les épreuves soient entièrement paramétrables, afin de pouvoir créer et configurer de nouveaux types d’épreuves en fonction de ses besoins, sans nécessiter de modifications importantes du système.

Par ailleurs, l’application doit être accessible et facile à utiliser par tous les types d’utilisateurs. L’interface doit être claire, lisible et intuitive, et les fonctionnalités doivent être simples à comprendre et à utiliser, même pour des utilisateurs peu familiers avec les outils numériques.

Les principales fonctionnalités de l’application seront les suivantes :





---

## 2. Exigences

### 2.1 Exigences fonctionnelles

Les exigences fonctionnelles seront précisées et validées progressivement avec le client
À ce stade (V1) , les principales fonctionnalités envisagées sont les suivantes :
* **Gestion des épreuves** : création, modification, configuration et gestion des différentes épreuves.
* **Gestion des pilotes** : création et gestion des profils des pilotes participant aux épreuves.
* **Saisie des résultats** : enregistrement et gestion des résultats obtenus par les pilotes lors des différentes épreuves.
* **Génération automatique des classements et des notes** : calcul automatique des résultats, des notes et des classements selon les règles définies pour chaque épreuve , Mise à jour des informations et des classements en     quasi temps réel
* **Gestion des comptes utilisateurs** : création, modification et gestion des comptes ainsi que des droits d’accès des différents utilisateurs.



### 2.2 Exigences non fonctionnelles

- **Multiplateforme** : l'application doit être disponible et pleinement fonctionnelle à la fois sur PC  et sur mobile .

Multiplateforme
- L'application doit être accessible et pleinement fonctionnelle depuis un ordinateur et un smartphone
- L'interface devra s'adapter aux différents formats d'écran afin de garantir une utilisation confortable sur les différents appareils
- Ergonomie et simplicité d'utilisation
- L'application devra être conçue de manière à être facilement utilisable par des personnes non informaticiennes
- Les différentes fonctionnalités devront être accessibles de manière claire et intuitive, sans nécessiter de connaissances techniques particulières
Autres exigences
- Les autres exigences non fonctionnelles, notamment concernant la sécurité, les performances, l'accessibilité et le déploiement, seront précisées avec le client au cours du projet

### 2.3 Utilisateurs et rôles

- Administrateur : toutes les droits
- Bénévoles : ils peuvent se connecter à l’application, gérer les pilotes et saisir les résultats des épreuves.
- Responsables d’épreuve : ils peuvent se connecter à l’application, gérer les pilotes, saisir les résultats et gérer les épreuves dont ils sont responsables.
- Pilotes : ils peuvent se connecter à l’application et consulter les classements ainsi que leurs résultats.
- Comité des pilotes : il peut se connecter à l’application et consulter les classements et les informations relatives aux résultats.
- Consultation des classements : tous les utilisateurs autorisés peuvent consulter les classements générés automatiquement par l’application.
---

## 3. Cas d'utilisation

### 3.1 Diagramme de cas d'utilisation

![Use case v1](./Use%20case%20v1.png)

### 3.2 Description des cas d'utilisation

- Les participants peuvent créer un compte, puis se connecter et s'inscrire
- Les administrateurs peuvent se connecter avec leur compte pour préparer, créer et gérer des épreuves
- Tout le monde peut consulter le classement

---

## 4. User Stories et Backlog

### 4.1 User Stories

| US | Titre de l'US |
|---|---|
| [US 01](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/9) | **En tant que gestionnaire, je veux inscrire un participant avec son nom/prénom** |
| [US 02](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/10) | **En tant que gestionnaire, je veux créer un atelier composé de plusieurs épreuves afin de structurer une journée de compétition** |
| [US 03](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/11) | **En tant que gestionnaire, je veux organiser une journée contenant des ateliers (matin, après-midi, 2e journée) afin de répartir les épreuves dans le temps** |
| [US 04](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/12) | **En tant qu'utilisateur, je veux voir le score total d'un participant ("Participant n°X points") afin de suivre sa performance globale** |
| [US 05](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/13) | **En tant qu'utilisateur, je veux consulter le classement du jour afin de suivre l'évolution de la compétition** |
| [US 06](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/14) | **En tant qu'utilisateur, je veux consulter le classement final afin de connaître les résultats globaux** |
| [US 07](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/15) | **En tant qu'utilisateur, je veux que le classement se rafraîchisse automatiquement afin d'avoir des données à jour sans recharger la page** |
| [US 08](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/16) | **En tant qu'utilisateur, je veux accéder aux classements sans compte (lecture seule) afin de suivre la compétition facilement** |
| [US 09](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/17) | **En tant que gestionnaire, je veux pouvoir créer une épreuve en choisissant son mode de notation (points de temps, nombre de sauts, etc.) afin d'adapter le barème à chaque discipline** |
| [US 10](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/18) | **En tant que gestionnaire, je veux activer/désactiver l'affichage des points dans le classement (switch) afin de ne montrer que le rang si besoin** |
| [US 11](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/19) | **En tant que bénévole, je veux attribuer des points à un participant pour une épreuve donnée afin d'enregistrer sa performance** |
| [US 12](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/20) | **En tant que membre du comité, je veux modifier les points d'un participant (litige, pénalité, retrait) afin de traiter les cas exceptionnels** |
| [US 13](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/21) | **En tant que membre du comité, je veux consulter l'historique des modifications de points afin de garder une traçabilité des litiges** |
| [US 14](https://github.com/PPS-SAE-S5/PPS-COMP-2027/issues/22) | **En tant qu'administrateur, je veux gérer les rôles (gestionnaire, bénévole, comité, utilisateur) afin que chacun n'accède qu'aux fonctionnalités qui le concernent** |


### 4.2 Backlog produit

| Backlog | Période du backlog |
|---|---|
| [Backlog Sprint 0](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/main/Documentation/Sprint%200/Backlog%20Produit%20v0.md) | **01/09/2026 - 20/09/2026** |


### 4.3 Planification des sprints

| Sprint | Période du sprint |
|---|---|
| [Sprint 0](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/1) | **01/09/2026 - 18/09/2026** |
| [Sprint 1](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/2) | **21/09/2026 - 02/10/2026** |
| [Sprint 2](https://github.com/PPS-SAE-S5/PPS-COMP-2027/milestone/3) | **05/10/2026 - 23/10/2026** |
| [Sprint 3](#) | **02/11/2026 - 27/11/2026** |
| [Sprint 4](#) | **30/11/2026 - 18/12/2026** |
| [Sprint 5](#) | **30/11/2026 - 18/12/2026** |
| [Sprint 6](#) | **04/01/2027 - 22/01/2027** |

---

## 5. Conception du système

### 5.1 Architecture

- Application Mobile
- Application Ordinateur
- Page web
- Base de données

### 5.2 Conception de la base de données (en l'état lors du sprint 0)

- Planification pour du SQL (PostgreSQL(?) )
- Tables nécessaires au bon fonctionnement de l'application

### 5.3 API (en l'état lors du sprint 0)

- **Format d'échange** : JSON, utilisé pour les échanges entre le frontend, le backend et la base de données
- L'API est **auto-générée par Supabase** à partir du schéma PostgreSQL (pas de développement manuel d'API REST côté serveur)

*Détails supplémentaires (endpoints spécifiques, règles de sécurité/RLS, authentification, RGESN (num resp), etc.) à ajouter lors des futurs sprints*

---

## 6. Technologies

### 6.1 Frontend

- **Framework** : React (ou Node.js).
- **Style** : Tailwind CSS, pour un rendu responsive sur **PC** et **mobile**
- **Librairies** : utilisation de librairies existantes plutôt que du développement custom

### 6.2 Backend

- **Supabase** : "backend-as-a-service" incluant base de données PostgreSQL, API auto-générée, authentification et temps réel
- Expose une **API au format JSON** pour communiquer avec le frontend

### 6.3 Base de données

- **PostgreSQL** (fourni par Supabase), avec échanges de données au **format JSON**.
- Fonctionnalités temps réel et authentification incluses nativement dans Supabase.

---

## 7. Tests

### 7.1 Stratégie de test

*À mettre en place lors des futurs sprints*

PS : Etant un projet utilisant essentiellement Java (Mobile et Ordinateur), des tests JUnit sont à mettre en place
PPS : Des tests globaux sont à réaliser sur les différents cas d'utilisations pour toutes les plateformes
PPPS : L'objectif sera de faire souffrir le back et observer comment les données réagissent en direct

### 7.2 Tests

*À faire lors des futurs sprints*

---

## 8. Déploiement

- **Frontend** : hébergé sur **Vercel** (?)
- **Backend/Base de données** : hébergés sur **Supabase** (gratuit pour l'usage prévu)
- Les deux plateformes gèrent le déploiement continu à partir du dépôt de code source

---

## 9. Améliorations futures

*To be continued*
