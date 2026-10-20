-- ============================================================
-- Migration v4 : autoriser la SUPPRESSION de comptes via l'application
-- ============================================================
-- À exécuter UNE SEULE FOIS dans Supabase (SQL Editor -> New query -> coller -> Run),
-- UNIQUEMENT si, après « Synchroniser : application -> base en ligne », un compte supprimé
-- dans l'application est toujours présent en ligne (l'application affiche alors un avertissement).
--
-- Cause : avec la sécurité par lignes (RLS) de Supabase, une suppression non autorisée n'est pas
-- une erreur, elle est simplement ignorée. Ce script autorise la suppression sur la table utilisateurs.
-- Sans danger si la RLS n'est pas activée (il ne change alors rien).
-- ============================================================

grant delete on public.utilisateurs to anon, authenticated;

drop policy if exists "pps_delete_utilisateurs" on public.utilisateurs;
create policy "pps_delete_utilisateurs"
    on public.utilisateurs
    for delete
    to anon, authenticated
    using (true);
