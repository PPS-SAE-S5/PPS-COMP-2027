-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.participants (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  nom character varying NOT NULL,
  prenom character varying NOT NULL,
  email character varying NOT NULL UNIQUE,
  mot_de_passe character varying NOT NULL,
  role character varying NOT NULL DEFAULT 'pilote'::character varying CHECK (role::text = ANY (ARRAY['administrateur'::character varying, 'responsable_epreuve'::character varying, 'benevole'::character varying, 'pilote'::character varying, 'membre_comite'::character varying]::text[])),
  date_creation timestamp without time zone DEFAULT now(),
  CONSTRAINT participants_pkey PRIMARY KEY (id)
);
CREATE TABLE public.infos_participants (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  utilisateur_id uuid NOT NULL UNIQUE,
  numero_licence character varying NOT NULL UNIQUE,
  caserne character varying,
  poids_kg numeric NOT NULL,
  annee_naissance integer NOT NULL CHECK (annee_naissance >= 1930 AND annee_naissance <= EXTRACT(year FROM now())::integer),
  categorie character varying,
  date_creation timestamp without time zone DEFAULT now(),
  CONSTRAINT infos_participants_pkey PRIMARY KEY (id),
  CONSTRAINT profils_pilotes_utilisateur_id_fkey FOREIGN KEY (utilisateur_id) REFERENCES public.participants(id)
);
CREATE TABLE public.competitions (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  nom character varying NOT NULL,
  description text,
  lieu character varying,
  date_debut date NOT NULL,
  date_fin date NOT NULL,
  statut character varying NOT NULL DEFAULT 'planifiee'::character varying CHECK (statut::text = ANY (ARRAY['planifiee'::character varying, 'en_cours'::character varying, 'terminee'::character varying, 'annulee'::character varying]::text[])),
  createur_id uuid,
  date_creation timestamp without time zone DEFAULT now(),
  CONSTRAINT competitions_pkey PRIMARY KEY (id),
  CONSTRAINT competitions_createur_id_fkey FOREIGN KEY (createur_id) REFERENCES public.participants(id)
);
CREATE TABLE public.equipe_organisation (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  competition_id uuid NOT NULL,
  utilisateur_id uuid NOT NULL,
  fonction character varying NOT NULL DEFAULT 'benevole'::character varying CHECK (fonction::text = ANY (ARRAY['administrateur'::character varying, 'responsable_epreuve'::character varying, 'benevole'::character varying, 'membre_comite'::character varying]::text[])),
  date_ajout timestamp without time zone DEFAULT now(),
  CONSTRAINT equipe_organisation_pkey PRIMARY KEY (id),
  CONSTRAINT equipe_organisation_competition_id_fkey FOREIGN KEY (competition_id) REFERENCES public.competitions(id),
  CONSTRAINT equipe_organisation_utilisateur_id_fkey FOREIGN KEY (utilisateur_id) REFERENCES public.participants(id)
);
CREATE TABLE public.types_epreuve (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  code character varying NOT NULL UNIQUE,
  nom character varying NOT NULL,
  description text,
  mode_calcul character varying NOT NULL CHECK (mode_calcul::text = ANY (ARRAY['precision'::character varying, 'distance'::character varying, 'temps'::character varying, 'checkpoint'::character varying]::text[])),
  CONSTRAINT types_epreuve_pkey PRIMARY KEY (id)
);
CREATE TABLE public.parametres_calcul (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  competition_id uuid NOT NULL,
  type_epreuve_id uuid NOT NULL,
  points_max integer DEFAULT 1000,
  parametres_json jsonb,
  date_modification timestamp without time zone DEFAULT now(),
  CONSTRAINT parametres_calcul_pkey PRIMARY KEY (id),
  CONSTRAINT parametres_calcul_competition_id_fkey FOREIGN KEY (competition_id) REFERENCES public.competitions(id),
  CONSTRAINT parametres_calcul_type_epreuve_id_fkey FOREIGN KEY (type_epreuve_id) REFERENCES public.types_epreuve(id)
);
CREATE TABLE public.inscriptions (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  competition_id uuid NOT NULL,
  utilisateur_id uuid NOT NULL,
  numero_dossard integer,
  statut_inscription character varying NOT NULL DEFAULT 'en_attente'::character varying CHECK (statut_inscription::text = ANY (ARRAY['en_attente'::character varying, 'validee'::character varying, 'refusee'::character varying, 'annulee'::character varying]::text[])),
  date_inscription timestamp without time zone DEFAULT now(),
  CONSTRAINT inscriptions_pkey PRIMARY KEY (id),
  CONSTRAINT inscriptions_competition_id_fkey FOREIGN KEY (competition_id) REFERENCES public.competitions(id),
  CONSTRAINT inscriptions_utilisateur_id_fkey FOREIGN KEY (utilisateur_id) REFERENCES public.participants(id)
);
CREATE TABLE public.manches (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  competition_id uuid NOT NULL,
  type_epreuve_id uuid NOT NULL,
  responsable_id uuid,
  numero integer NOT NULL,
  date_manche date,
  statut character varying NOT NULL DEFAULT 'planifiee'::character varying CHECK (statut::text = ANY (ARRAY['planifiee'::character varying, 'en_cours'::character varying, 'validee'::character varying, 'annulee'::character varying]::text[])),
  distance_ref_km numeric,
  date_creation timestamp without time zone DEFAULT now(),
  CONSTRAINT manches_pkey PRIMARY KEY (id),
  CONSTRAINT manches_competition_id_fkey FOREIGN KEY (competition_id) REFERENCES public.competitions(id),
  CONSTRAINT manches_type_epreuve_id_fkey FOREIGN KEY (type_epreuve_id) REFERENCES public.types_epreuve(id),
  CONSTRAINT manches_responsable_id_fkey FOREIGN KEY (responsable_id) REFERENCES public.participants(id)
);
CREATE TABLE public.resultats (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  manche_id uuid NOT NULL,
  inscription_id uuid NOT NULL,
  distance_km numeric,
  temps_vol interval,
  vitesse_kmh numeric,
  ecart_cible_m numeric,
  checkpoints_valides integer,
  mesures_json jsonb,
  points numeric DEFAULT 0,
  statut_validation character varying NOT NULL DEFAULT 'en_attente'::character varying CHECK (statut_validation::text = ANY (ARRAY['en_attente'::character varying, 'validee'::character varying, 'contestee'::character varying, 'invalidee'::character varying]::text[])),
  commentaire_juge text,
  date_saisie timestamp without time zone DEFAULT now(),
  CONSTRAINT resultats_pkey PRIMARY KEY (id),
  CONSTRAINT resultats_manche_id_fkey FOREIGN KEY (manche_id) REFERENCES public.manches(id),
  CONSTRAINT resultats_inscription_id_fkey FOREIGN KEY (inscription_id) REFERENCES public.inscriptions(id)
);
CREATE TABLE public.classement (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  competition_id uuid NOT NULL,
  inscription_id uuid NOT NULL,
  total_points numeric DEFAULT 0,
  rang integer,
  date_calcul timestamp without time zone DEFAULT now(),
  CONSTRAINT classement_pkey PRIMARY KEY (id),
  CONSTRAINT classement_general_competition_id_fkey FOREIGN KEY (competition_id) REFERENCES public.competitions(id),
  CONSTRAINT classement_general_inscription_id_fkey FOREIGN KEY (inscription_id) REFERENCES public.inscriptions(id)
);