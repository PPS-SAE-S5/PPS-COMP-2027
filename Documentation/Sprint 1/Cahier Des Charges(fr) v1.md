# Cahier des charges fonctionnel

> Ce document décrit **ce que l'application doit faire** et **pourquoi**. Les choix techniques (architecture, technologies, base de données, tests, déploiement) sont détaillés dans le [Document technique](./Document_technique.md).

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
4. [Périmètre et contraintes](#4-périmètre-et-contraintes)
   - 4.1 [Périmètre de la V1](#41-périmètre-de-la-v1)
   - 4.2 [Contraintes](#42-contraintes)
5. [Livrables](#5-livrables)
6. [User Stories et Backlog](#6-user-stories-et-backlog)
   - 6.1 [User Stories](#61-user-stories)
   - 6.2 [Backlog produit](#62-backlog-produit)
   - 6.3 [Planification des sprints](#63-planification-des-sprints)
7. [Organisation du projet](#7-organisation-du-projet)
   - 7.1 [Parties prenantes](#71-parties-prenantes)
   - 7.2 [Fonctionnement et communication](#72-fonctionnement-et-communication)
   - 7.3 [Budget](#73-budget)
8. [Recette et validation](#8-recette-et-validation)
9. [Améliorations futures](#9-améliorations-futures)

---

## 1. Aperçu du projet

### 1.1 Contexte du projet

Le club de parapente Parapente Pays de Sault (PPS), situé dans l'Aude, organisera à l'été 2027 le Championnat de France des Pompiers de Parapente.

Actuellement, l'organisation de cette compétition repose principalement sur l'utilisation de documents papier et de fichiers Excel. Les informations et les résultats sont collectés et traités manuellement, notamment afin de réaliser les classements provisoires à la fin de chaque journée.

Afin de moderniser et de simplifier ce processus, le club souhaite mettre en place une solution numérique permettant de centraliser les différentes informations liées à la compétition et de faciliter leur gestion.

### 1.2 Objectifs

L'objectif du projet est de concevoir et de développer une application mobile, une application PC et une page web dédiées à la gestion d'une compétition de parapente.

Les utilisateurs pourront utiliser l'application mobile ou PC selon leur besoin. Les applications fonctionnent avec un serveur et une base de données en ligne, mais aussi sans internet grâce à une base de données locale ; les données sont transmises sur internet dès que la connexion est rétablie.

Il existe également différents rôles, qui ont des droits différents.

Selon les exigences du client, l'application doit être conçue de manière durable et évolutive afin de pouvoir s'adapter aux besoins futurs. De nouvelles épreuves ou de nouvelles règles pourront être introduites à l'avenir. L'application ne doit donc pas être limitée à un ensemble prédéfini de types d'épreuves.

Le client souhaite que les épreuves soient entièrement paramétrables, afin de pouvoir créer et configurer de nouveaux types d'épreuves en fonction de ses besoins, sans nécessiter de modifications importantes du système.

Par ailleurs, l'application doit être accessible et facile à utiliser par tous les types d'utilisateurs. L'interface doit être claire, lisible et intuitive, et les fonctionnalités doivent être simples à comprendre et à utiliser, même pour des utilisateurs peu familiers avec les outils numériques.

Les principales fonctionnalités de l'application sont détaillées en [section 2.1](#21-exigences-fonctionnelles).

---

## 2. Exigences

### 2.1 Exigences fonctionnelles

Les exigences fonctionnelles seront précisées et validées progressivement avec le client.
À ce stade (V1), les principales fonctionnalités envisagées sont les suivantes :

* **Gestion des épreuves** : création, modification, configuration et gestion des différentes épreuves.
* **Gestion des pilotes** : création et gestion des profils des pilotes participant aux épreuves.
* **Saisie des résultats** : enregistrement et gestion des résultats obtenus par les pilotes lors des différentes épreuves.
* **Génération automatique des classements et des notes** : calcul automatique des résultats, des notes et des classements selon les règles définies pour chaque épreuve. Mise à jour des informations et des classements en quasi temps réel.
* **Gestion des comptes utilisateurs** : création, modification et gestion des comptes ainsi que des droits d'accès des différents utilisateurs.

### 2.2 Exigences non fonctionnelles

**Multiplateforme**
- L'application doit être accessible et pleinement fonctionnelle depuis un ordinateur et un smartphone.
- L'interface devra s'adapter aux différents formats d'écran afin de garantir une utilisation confortable sur les différents appareils.

**Ergonomie et simplicité d'utilisation**
- L'application devra être conçue de manière à être facilement utilisable par des personnes non informaticiennes.
- Les différentes fonctionnalités devront être accessibles de manière claire et intuitive, sans nécessiter de connaissances techniques particulières.

**Autres exigences**
- Les autres exigences non fonctionnelles, notamment concernant la sécurité, les performances, l'accessibilité et le déploiement, seront précisées avec le client au cours du projet.

### 2.3 Utilisateurs et rôles

- **Administrateur** : tous les droits.
- **Bénévoles** : ils peuvent se connecter à l'application, gérer les pilotes et saisir les résultats des épreuves.
- **Responsables d'épreuve** : ils peuvent se connecter à l'application, gérer les pilotes, saisir les résultats et gérer les épreuves dont ils sont responsables.
- **Pilotes** : ils peuvent se connecter à l'application et consulter les classements ainsi que leurs résultats.
- **Comité des pilotes** : il peut se connecter à l'application et consulter les classements et les informations relatives aux résultats.
- **Consultation des classements** : tous les utilisateurs autorisés peuvent consulter les classements générés automatiquement par l'application.

---

## 3. Cas d'utilisation

### 3.1 Diagramme de cas d'utilisation

![Use case v1](./Use%20case%20V1.png)

### 3.2 Description des cas d'utilisation

Les rôles et leurs droits sont décrits en [section 2.3](#23-utilisateurs-et-rôles). Cette section décrit ce que chaque acteur fait concrètement dans l'application.

| N° | Cas d'utilisation | Acteur | Préconditions | Scénario principal | Résultat | US |
|---|---|---|---|---|---|---|
| CU 01 | Se connecter | Utilisateur avec compte | Le compte existe | L'utilisateur saisit ses identifiants | Il accède aux fonctionnalités de son rôle | US 14 |
| CU 02 | Consulter les classements | Tout utilisateur, y compris sans compte | Aucune | L'utilisateur ouvre l'application ou la page web, puis choisit le classement du jour ou le classement final | Le classement s'affiche en lecture seule et se rafraîchit automatiquement | US 05, 06, 07, 08 |
| CU 03 | Consulter le score d'un participant | Utilisateur | Le participant a au moins un résultat | L'utilisateur sélectionne un participant | Le score total du participant s'affiche | US 04 |
| CU 04 | Inscrire un participant | Gestionnaire | Connecté | Le gestionnaire saisit le nom et le prénom du participant | Le participant est créé | US 01 |
| CU 05 | Créer une épreuve | Gestionnaire | Connecté | Le gestionnaire crée l'épreuve, choisit son mode de notation (points de temps, nombre de sauts, etc.) et règle l'affichage ou non des points dans le classement | L'épreuve est disponible pour la saisie des résultats | US 09, 10 |
| CU 06 | Organiser la compétition | Gestionnaire | Des épreuves existent | Le gestionnaire regroupe des épreuves en ateliers, puis répartit les ateliers dans des journées (matin, après-midi, 2e journée) | La structure de la compétition est définie | US 02, 03 |
| CU 07 | Saisir les points d'un participant | Bénévole | Connecté, le participant et l'épreuve existent | Le bénévole sélectionne l'épreuve et le participant, puis saisit les points | Le résultat est enregistré (y compris hors-ligne, synchronisé ensuite) et les classements sont mis à jour | US 11 |
| CU 08 | Modifier les points d'un participant | Membre du comité | Connecté, un résultat existe | Le membre du comité modifie les points en cas de litige, de pénalité ou de retrait | Les points sont modifiés et les classements recalculés | US 12 |
| CU 09 | Consulter l'historique des modifications | Membre du comité | Connecté | Le membre du comité ouvre l'historique des modifications de points | La liste des modifications s'affiche, ce qui garantit la traçabilité des litiges | US 13 |
| CU 10 | Gérer les rôles | Administrateur | Connecté | L'administrateur attribue ou modifie le rôle d'un utilisateur | L'utilisateur n'accède qu'aux fonctionnalités de son rôle | US 14 |

> **À valider** : les user stories parlent de « gestionnaire » et de « membre du comité », alors que la section 2.3 parle de « Responsable d'épreuve » et de « Comité des pilotes ». Il faudra harmoniser les noms des rôles.

---

## 4. Périmètre et contraintes

### 4.1 Périmètre de la V1

**Inclus dans la V1**
- Gestion des épreuves (paramétrables), des ateliers et des journées de compétition.
- Gestion des pilotes et saisie des résultats.
- Calcul automatique des notes et des classements (du jour et final), mis à jour en quasi temps réel.
- Gestion des comptes et des rôles.
- Consultation des classements en lecture seule, sans compte.
- Fonctionnement hors-ligne avec synchronisation au retour de la connexion.
- Application mobile, application PC et page web.

**Hors périmètre de la V1** *(à valider avec le client)*
- À compléter (ex. : paiement des inscriptions, gestion de l'hébergement ou de la logistique, etc.).

### 4.2 Contraintes

- **Délai** : l'application doit être opérationnelle pour le Championnat de France des Pompiers de Parapente, à l'été 2027.
- **Conditions d'utilisation** : usage sur le terrain, avec un réseau potentiellement faible ou absent (d'où le mode hors-ligne).
- **Public** : utilisateurs peu familiers avec les outils numériques.
- **Évolutivité** : de nouvelles épreuves et de nouvelles règles pourront être ajoutées sans modification importante du système.
- **Budget et hébergement** : solution privilégiant des services gratuits ou peu coûteux *(à confirmer, voir 7.3)*.
- **Sécurité et données personnelles** : les données des pilotes doivent être protégées *(exigences à préciser avec le client, voir 2.2)*.

---

## 5. Livrables

| Livrable | Description | Statut |
|---|---|---|
| Application mobile | Application utilisable sur smartphone, avec mode hors-ligne | À livrer |
| Application PC | Application utilisable sur ordinateur, avec mode hors-ligne | À livrer |
| Page web | Consultation des classements en lecture seule, sans compte | À livrer |
| Base de données | Base en ligne et base locale, avec schéma documenté | À livrer |
| Code source | Dépôt GitHub (PPS-SAE-S5/PPS-COMP-2027) | À livrer |
| Cahier des charges | Le présent document | En cours |
| Document technique | Architecture, base de données, API, tests, déploiement | En cours |
| Backlog produit et user stories | Suivi des besoins et des sprints | En cours |
| Plan de tests et rapport de recette | Cas de test et résultats de validation | À livrer |
| Manuel utilisateur | Guide simple pour les bénévoles, responsables d'épreuve et pilotes | À livrer *(à confirmer)* |

**Conditions de livraison** *(à valider avec le client)* : format, lieu de dépôt, qui héberge l'application après la livraison, qui assure la maintenance, licence du code et propriété des données.

---

## 6. User Stories et Backlog

### 6.1 User Stories

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

### 6.2 Backlog produit

| Backlog | Période du backlog |
|---|---|
| [Backlog Sprint 0](https://github.com/PPS-SAE-S5/PPS-COMP-2027/blob/main/Documentation/Sprint%200/Backlog%20Produit%20v0.md) | **01/09/2026 - 20/09/2026** |

### 6.3 Planification des sprints

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

## 7. Organisation du projet

### 7.1 Parties prenantes

| Rôle | Description |
|---|---|
| Client | Club Parapente Pays de Sault (PPS) — *référent à préciser* |
| Équipe projet | À compléter (noms et rôles) |
| Validation | À préciser (qui valide les besoins et les livrables côté client) |

### 7.2 Fonctionnement et communication

- Méthode de travail : **agile**, par sprints (voir 6.3).
- Suivi : issues et milestones GitHub.
- Revue de sprint avec le client : *fréquence à définir*.
- Les exigences sont précisées et validées progressivement avec le client.

### 7.3 Budget

- Outils et hébergement : services gratuits pour l'usage prévu (Supabase, et Vercel *à confirmer*).
- Budget global : *à préciser avec le client*.

---

## 8. Recette et validation

- La recette est réalisée avec le client à la fin des sprints concernés, puis avant la compétition.
- Chaque livrable est validé au regard des exigences (section 2) et des user stories (section 6) : une user story est validée lorsque son comportement a été vérifié par le client.
- Des tests sont réalisés sur les différents cas d'utilisation, sur toutes les plateformes, y compris en mode hors-ligne puis resynchronisé (stratégie détaillée dans le [Document technique](./Document_technique.md)).
- Une répétition en conditions réelles avant la compétition est recommandée *(à valider avec le client)*.
- Critères d'acceptation globaux : *à définir avec le client*.

---

## 9. Améliorations futures

*To be continued*
