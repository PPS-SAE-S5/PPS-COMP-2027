## Table `participants`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `nom` | `varchar` |  |
| `prenom` | `varchar` |  |
| `email` | `varchar` |  Unique |
| `mot_de_passe` | `varchar` |  |
| `role` | `varchar` |  |
| `date_creation` | `timestamp` |  Nullable |

## Table `infos_participants`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `utilisateur_id` | `uuid` |  Unique |
| `numero_licence` | `varchar` |  Unique |
| `caserne` | `varchar` |  Nullable |
| `poids_kg` | `numeric` |  |
| `annee_naissance` | `int4` |  |
| `categorie` | `varchar` |  Nullable |
| `date_creation` | `timestamp` |  Nullable |

## Table `competitions`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `nom` | `varchar` |  |
| `description` | `text` |  Nullable |
| `lieu` | `varchar` |  Nullable |
| `date_debut` | `date` |  |
| `date_fin` | `date` |  |
| `statut` | `varchar` |  |
| `createur_id` | `uuid` |  Nullable |
| `date_creation` | `timestamp` |  Nullable |

## Table `equipe_organisation`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `competition_id` | `uuid` |  |
| `utilisateur_id` | `uuid` |  |
| `fonction` | `varchar` |  |
| `date_ajout` | `timestamp` |  Nullable |

## Table `types_epreuve`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `code` | `varchar` |  Unique |
| `nom` | `varchar` |  |
| `description` | `text` |  Nullable |
| `mode_calcul` | `varchar` |  |

## Table `parametres_calcul`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `competition_id` | `uuid` |  |
| `type_epreuve_id` | `uuid` |  |
| `points_max` | `int4` |  Nullable |
| `parametres_json` | `jsonb` |  Nullable |
| `date_modification` | `timestamp` |  Nullable |

## Table `inscriptions`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `competition_id` | `uuid` |  |
| `utilisateur_id` | `uuid` |  |
| `numero_dossard` | `int4` |  Nullable |
| `statut_inscription` | `varchar` |  |
| `date_inscription` | `timestamp` |  Nullable |

## Table `manches`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `competition_id` | `uuid` |  |
| `type_epreuve_id` | `uuid` |  |
| `responsable_id` | `uuid` |  Nullable |
| `numero` | `int4` |  |
| `date_manche` | `date` |  Nullable |
| `statut` | `varchar` |  |
| `distance_ref_km` | `numeric` |  Nullable |
| `date_creation` | `timestamp` |  Nullable |

## Table `resultats`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `manche_id` | `uuid` |  |
| `inscription_id` | `uuid` |  |
| `distance_km` | `numeric` |  Nullable |
| `temps_vol` | `interval` |  Nullable |
| `vitesse_kmh` | `numeric` |  Nullable |
| `ecart_cible_m` | `numeric` |  Nullable |
| `checkpoints_valides` | `int4` |  Nullable |
| `mesures_json` | `jsonb` |  Nullable |
| `points` | `numeric` |  Nullable |
| `statut_validation` | `varchar` |  |
| `commentaire_juge` | `text` |  Nullable |
| `date_saisie` | `timestamp` |  Nullable |

## Table `classement`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `competition_id` | `uuid` |  |
| `inscription_id` | `uuid` |  |
| `total_points` | `numeric` |  Nullable |
| `rang` | `int4` |  Nullable |
| `date_calcul` | `timestamp` |  Nullable |

## RLS Policies

### `participants`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |

### `classement`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |

