# Document technique

> Ce document décrit **comment** l'application est conçue et réalisée : architecture, base de données, API, technologies, tests et déploiement. Les besoins et objectifs sont décrits dans le [Cahier des charges](./Cahier_des_charges.md).

## Table des matières

1. [Architecture](#1-architecture)
2. [Conception de la base de données](#2-conception-de-la-base-de-données)
3. [API](#3-api)
4. [Technologies](#4-technologies)
   - 4.1 [Frontend](#41-frontend)
   - 4.2 [Backend](#42-backend)
   - 4.3 [Base de données](#43-base-de-données)
5. [Tests](#5-tests)
   - 5.1 [Stratégie de test](#51-stratégie-de-test)
   - 5.2 [Tests](#52-tests)
6. [Déploiement](#6-déploiement)

---

## 1. Architecture

- Application Mobile
- Application Ordinateur
- Page web
- Base de données

## 2. Conception de la base de données

*(en l'état lors du sprint 0)*

- Planification pour du SQL (PostgreSQL(?))
- Tables nécessaires au bon fonctionnement de l'application

## 3. API

*(en l'état lors du sprint 0)*

- **Format d'échange** : JSON, utilisé pour les échanges entre le frontend, le backend et la base de données.
- L'API est **auto-générée par Supabase** à partir du schéma PostgreSQL (pas de développement manuel d'API REST côté serveur).

*Détails supplémentaires (endpoints spécifiques, règles de sécurité/RLS, authentification, RGESN (num resp), etc.) à ajouter lors des futurs sprints.*

---

## 4. Technologies

### 4.1 Frontend

- **Framework** : React (ou Node.js).
- **Style** : Tailwind CSS, pour un rendu responsive sur **PC** et **mobile**.
- **Librairies** : utilisation de librairies existantes plutôt que du développement custom.

### 4.2 Backend

- **Supabase** : « backend-as-a-service » incluant base de données PostgreSQL, API auto-générée, authentification et temps réel.
- Expose une **API au format JSON** pour communiquer avec le frontend.

### 4.3 Base de données

- **PostgreSQL** (fourni par Supabase), avec échanges de données au **format JSON**.
- Fonctionnalités temps réel et authentification incluses nativement dans Supabase.

---

## 5. Tests

### 5.1 Stratégie de test

*À mettre en place lors des futurs sprints.*

- Le projet utilisant essentiellement Java (Mobile et Ordinateur), des tests JUnit sont à mettre en place.
- Des tests globaux sont à réaliser sur les différents cas d'utilisation pour toutes les plateformes.
- L'objectif sera de faire souffrir le back et d'observer comment les données réagissent en direct.

### 5.2 Tests

*À faire lors des futurs sprints.*

---

## 6. Déploiement

- **Frontend** : hébergé sur **Vercel** (?)
- **Backend / Base de données** : hébergés sur **Supabase** (gratuit pour l'usage prévu).
- Les deux plateformes gèrent le déploiement continu à partir du dépôt de code source.
