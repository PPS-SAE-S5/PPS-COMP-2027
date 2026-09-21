# Base de données - PPS-COMP-2027

Structure de la base de données pour la plateforme de gestion de compétitions de parapente.

## Fichiers

- `schema.sql` : script de création des tables (PostgreSQL)

## Aperçu des tables

| Table | Rôle |
|---|---|
| `utilisateurs` | Comptes (pilotes, organisateurs, juges, admin) |
| `competitions` | Une compétition organisée (dates, lieu, statut) |
| `competition_organisateurs` | Lien entre une compétition et son équipe d'organisation |
| `parametres_calcul` | Règles et coefficients de calcul des scores, par compétition |
| `inscriptions` | Inscription d'un pilote à une compétition (dossard, catégorie, statut) |
| `manches` | Les épreuves/manches d'une compétition |
| `resultats` | Résultat d'un pilote sur une manche (distance, vitesse, points) |
| `classement_general` | Classement cumulé, recalculé après chaque manche validée |

## Diagramme entité-relation

```mermaid
erDiagram
    UTILISATEURS ||--o{ INSCRIPTIONS : "s'inscrit"
    UTILISATEURS ||--o{ COMPETITION_ORGANISATEURS : "organise"
    COMPETITIONS ||--o{ COMPETITION_ORGANISATEURS : "a pour"
    COMPETITIONS ||--o{ INSCRIPTIONS : "reçoit"
    COMPETITIONS ||--o{ MANCHES : "contient"
    COMPETITIONS ||--o| PARAMETRES_CALCUL : "definit"
    COMPETITIONS ||--o{ CLASSEMENT_GENERAL : "produit"
    MANCHES ||--o{ RESULTATS : "genere"
    INSCRIPTIONS ||--o{ RESULTATS : "obtient"
    INSCRIPTIONS ||--o{ CLASSEMENT_GENERAL : "figure dans"

    UTILISATEURS {
        uuid id PK
        string nom
        string prenom
        string email
        string role
    }
    COMPETITIONS {
        uuid id PK
        string nom
        date date_debut
        date date_fin
        string statut
    }
    INSCRIPTIONS {
        uuid id PK
        uuid competition_id FK
        uuid utilisateur_id FK
        int numero_dossard
        string statut_inscription
    }
    MANCHES {
        uuid id PK
        uuid competition_id FK
        int numero
        string statut
    }
    RESULTATS {
        uuid id PK
        uuid manche_id FK
        uuid inscription_id FK
        numeric distance_km
        numeric points
    }
    PARAMETRES_CALCUL {
        uuid id PK
        uuid competition_id FK
        string type_formule
    }
    CLASSEMENT_GENERAL {
        uuid id PK
        uuid competition_id FK
        uuid inscription_id FK
        numeric total_points
        int rang
    }
    COMPETITION_ORGANISATEURS {
        uuid id PK
        uuid competition_id FK
        uuid utilisateur_id FK
        string fonction
    }
```

## Notes

- Le champ `parametres_json` de `parametres_calcul` permet de stocker des réglages spécifiques à une formule de calcul (ex. type GAP de la FAI) sans modifier le schéma.
- `classement_general` est une table de "cache" : elle est recalculée par le backend après chaque validation de résultats, plutôt que calculée à la volée à chaque requête.
