-- ============================================================
-- PPS-COMP-2027 - Schéma de base de données
-- Plateforme de gestion de compétitions de parapente
-- Compatible PostgreSQL (Supabase)
-- ============================================================

-- ============================================================
-- 1. UTILISATEURS
-- ============================================================
create table utilisateurs (
   id            uuid primary key default gen_random_uuid(),
   nom           varchar(100) not null,
   prenom        varchar(100) not null,
   email         varchar(255) unique not null,
   telephone     varchar(20),
   mot_de_passe  varchar(255) not null, -- hash (géré par Supabase Auth si utilisé)
   role          varchar(20) not null default 'pilote', -- pilote, organisateur, juge, admin
   date_creation timestamp default now(),
   constraint chk_role
      check ( role in ( 'pilote',
                        'organisateur',
                        'juge',
                        'admin' ) )
);

-- ============================================================
-- 2. COMPÉTITIONS
-- ============================================================
create table competitions (
   id            uuid primary key default gen_random_uuid(),
   nom           varchar(150) not null,
   description   text,
   lieu          varchar(150),
   date_debut    date not null,
   date_fin      date not null,
   statut        varchar(20) not null default 'planifiee', -- planifiee, en_cours, terminee, annulee
   createur_id   uuid
      references utilisateurs ( id ),
   date_creation timestamp default now(),
   constraint chk_statut_competition
      check ( statut in ( 'planifiee',
                          'en_cours',
                          'terminee',
                          'annulee' ) )
);

-- Organisateurs / juges rattachés à une compétition (plusieurs personnes possibles)
create table competition_organisateurs (
   id             uuid primary key default gen_random_uuid(),
   competition_id uuid not null
      references competitions ( id )
         on delete cascade,
   utilisateur_id uuid not null
      references utilisateurs ( id )
         on delete cascade,
   fonction       varchar(50) default 'organisateur', -- organisateur, juge, meteo, secretaire
   unique ( competition_id,
            utilisateur_id )
);

-- ============================================================
-- 3. PARAMÉTRAGE DES RÈGLES DE CALCUL (par compétition)
-- ============================================================
create table parametres_calcul (
   id                   uuid primary key default gen_random_uuid(),
   competition_id       uuid not null
      references competitions ( id )
         on delete cascade,
   type_formule         varchar(50) not null default 'GAP', -- ex : GAP (FAI), personnalisée
   coefficient_distance numeric default 1,
   coefficient_vitesse  numeric default 1,
   coefficient_arrivee  numeric default 1,
   points_max_manche    integer default 1000,
   parametres_json      jsonb, -- paramètres additionnels flexibles selon la formule
   date_modification    timestamp default now()
);

-- ============================================================
-- 4. INSCRIPTIONS (participants à une compétition)
-- ============================================================
create table inscriptions (
   id                 uuid primary key default gen_random_uuid(),
   competition_id     uuid not null
      references competitions ( id )
         on delete cascade,
   utilisateur_id     uuid not null
      references utilisateurs ( id )
         on delete cascade,
   numero_dossard     integer,
   categorie          varchar(50), -- ex : open, sport, féminine...
   statut_inscription varchar(20) not null default 'en_attente', -- en_attente, validee, refusee, annulee
   date_inscription   timestamp default now(),
   unique ( competition_id,
            numero_dossard ),
   unique ( competition_id,
            utilisateur_id ),
   constraint chk_statut_inscription
      check ( statut_inscription in ( 'en_attente',
                                      'validee',
                                      'refusee',
                                      'annulee' ) )
);

-- ============================================================
-- 5. MANCHES / ÉPREUVES
-- ============================================================
create table manches (
   id              uuid primary key default gen_random_uuid(),
   competition_id  uuid not null
      references competitions ( id )
         on delete cascade,
   numero          integer not null,
   date_manche     date,
   type_epreuve    varchar(50), -- ex : distance libre, parcours imposé
   statut          varchar(20) not null default 'planifiee', -- planifiee, en_cours, validee, annulee
   distance_ref_km numeric,
   date_creation   timestamp default now(),
   unique ( competition_id,
            numero ),
   constraint chk_statut_manche
      check ( statut in ( 'planifiee',
                          'en_cours',
                          'validee',
                          'annulee' ) )
);

-- ============================================================
-- 6. RÉSULTATS (par participant, par manche)
-- ============================================================
create table resultats (
   id                uuid primary key default gen_random_uuid(),
   manche_id         uuid not null
      references manches ( id )
         on delete cascade,
   inscription_id    uuid not null
      references inscriptions ( id )
         on delete cascade,
   distance_km       numeric default 0,
   temps_vol         interval,
   vitesse_kmh       numeric,
   points            numeric default 0,
   statut_validation varchar(20) not null default 'en_attente', -- en_attente, validee, contestee, invalidee
   commentaire_juge  text,
   date_saisie       timestamp default now(),
   unique ( manche_id,
            inscription_id ),
   constraint chk_statut_validation
      check ( statut_validation in ( 'en_attente',
                                     'validee',
                                     'contestee',
                                     'invalidee' ) )
);

-- ============================================================
-- 7. CLASSEMENT GÉNÉRAL (cumul des points par compétition)
-- Table de cache, recalculée après chaque validation de manche
-- ============================================================
create table classement_general (
   id             uuid primary key default gen_random_uuid(),
   competition_id uuid not null
      references competitions ( id )
         on delete cascade,
   inscription_id uuid not null
      references inscriptions ( id )
         on delete cascade,
   total_points   numeric default 0,
   rang           integer,
   date_calcul    timestamp default now(),
   unique ( competition_id,
            inscription_id )
);

-- ============================================================
-- INDEX UTILES
-- ============================================================
create index idx_inscriptions_competition on
   inscriptions (
      competition_id
   );
create index idx_manches_competition on
   manches (
      competition_id
   );
create index idx_resultats_manche on
   resultats (
      manche_id
   );
create index idx_classement_competition on
   classement_general (
      competition_id
   );