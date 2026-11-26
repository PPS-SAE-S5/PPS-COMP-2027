## Table `utilisateurs`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int4` | Primary Identity |
| `identifiant` | `varchar` |  Unique |
| `mot_de_passe` | `varchar` |  |
| `role` | `varchar` |  |
| `pilote_id` | `int4` |  Nullable |
| `actif` | `bool` |  |
| `updated_at` | `timestamptz` |  |

## Table `pilotes`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int4` | Primary Identity |
| `numero_licence` | `varchar` |  Unique |
| `nom` | `varchar` |  |
| `prenom` | `varchar` |  |
| `departement` | `varchar` |  Nullable |
| `poids` | `float8` |  Nullable |
| `email` | `varchar` |  Nullable |
| `annee_naissance` | `int4` |  Nullable |
| `genre` | `varchar` |  Nullable |
| `date_inscription` | `timestamptz` |  Nullable |
| `uuid` | `uuid` |  |
| `updated_at` | `timestamptz` |  |
| `numero_pilote` | `int4` |  Nullable |
| `categorie` | `varchar` |  |

## Table `epreuves`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int4` | Primary Identity |
| `nom` | `varchar` |  |
| `description` | `varchar` |  Nullable |
| `mode_calcul` | `varchar` |  |
| `formule` | `varchar` |  Nullable |
| `valeur_cle` | `varchar` |  Nullable |
| `sens_classement` | `varchar` |  Nullable |
| `afficher_classement` | `bool` |  |
| `compte_dans_general` | `bool` |  |
| `actif` | `bool` |  |
| `ordre` | `int4` |  Nullable |
| `date_creation` | `timestamptz` |  Nullable |
| `uuid` | `uuid` |  Unique |
| `updated_at` | `timestamptz` |  |

## Table `epreuve_parametres`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int4` | Primary Identity |
| `epreuve_id` | `int4` |  |
| `nom_variable` | `varchar` |  |
| `label` | `varchar` |  |
| `unite` | `varchar` |  Nullable |
| `obligatoire` | `bool` |  Nullable |
| `ordre` | `int4` |  Nullable |
| `uuid` | `uuid` |  |
| `updated_at` | `timestamptz` |  |

## Table `bareme_points`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int4` | Primary Identity |
| `epreuve_id` | `int4` |  |
| `rang` | `int4` |  |
| `points` | `float8` |  |
| `uuid` | `uuid` |  |
| `updated_at` | `timestamptz` |  |

## Table `inscriptions_epreuve`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int4` | Primary Identity |
| `pilote_id` | `int4` |  |
| `epreuve_id` | `int4` |  |
| `uuid` | `uuid` |  |
| `updated_at` | `timestamptz` |  |

## Table `resultats`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int4` | Primary Identity |
| `epreuve_id` | `int4` |  |
| `pilote_id` | `int4` |  |
| `valeurs_json` | `varchar` |  Nullable |
| `points` | `float8` |  Nullable |
| `disqualifie` | `bool` |  Nullable |
| `saisi_par` | `varchar` |  Nullable |
| `date_saisie` | `timestamptz` |  Nullable |
| `uuid` | `uuid` |  |
| `updated_at` | `timestamptz` |  |

## Table `journees`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `uuid` | `uuid` |  Unique |
| `nom` | `text` |  |
| `date_journee` | `date` |  Nullable |
| `ordre` | `int4` |  |
| `updated_at` | `timestamptz` |  |

## Table `ateliers`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `uuid` | `uuid` |  Unique |
| `journee_id` | `int8` |  |
| `nom` | `text` |  |
| `creneau` | `text` |  |
| `heure_debut` | `text` |  Nullable |
| `heure_fin` | `text` |  Nullable |
| `lieu` | `text` |  Nullable |
| `responsable` | `text` |  Nullable |
| `ordre` | `int4` |  |
| `updated_at` | `timestamptz` |  |

## Table `atelier_epreuves`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `atelier_id` | `int8` |  |
| `epreuve_id` | `int8` |  Unique |
| `ordre` | `int4` |  |

## RLS Policies

### `utilisateurs`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |
| `acces_total_utilisateurs` | ALL | public | PERMISSIVE | `true` | `true` |

### `epreuves`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |
| `acces_total_epreuves` | ALL | public | PERMISSIVE | `true` | `true` |

### `epreuve_parametres`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |
| `acces_total_epreuve_parametres` | ALL | public | PERMISSIVE | `true` | `true` |

### `bareme_points`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |
| `acces_total_bareme_points` | ALL | public | PERMISSIVE | `true` | `true` |

### `inscriptions_epreuve`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |
| `acces_total_inscriptions_epreuve` | ALL | public | PERMISSIVE | `true` | `true` |

### `resultats`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |
| `acces_total_resultats` | ALL | public | PERMISSIVE | `true` | `true` |

### `pilotes`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |
| `acces_total_pilotes` | ALL | public | PERMISSIVE | `true` | `true` |

### `journees`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `pps_all_journees` | ALL | anon, authenticated | PERMISSIVE | `true` | `true` |

### `ateliers`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |
| `pps_all_ateliers` | ALL | anon, authenticated | PERMISSIVE | `true` | `true` |

### `atelier_epreuves`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `Enable read access for all users` | SELECT | public | PERMISSIVE | `true` | — |
| `pps_all_atelier_epreuves` | ALL | anon, authenticated | PERMISSIVE | `true` | `true` |

