package com.pps.parapente.util;

import com.pps.parapente.model.Utilisateur;

/** Garde en mémoire l'utilisateur actuellement connecté (session applicative simple, mono-poste). */
public final class SessionManager {

    private static Utilisateur utilisateurConnecte;

    private SessionManager() {}

    public static void connecter(Utilisateur u) { utilisateurConnecte = u; }

    public static void deconnecter() { utilisateurConnecte = null; }

    public static Utilisateur getUtilisateurConnecte() { return utilisateurConnecte; }

    public static boolean estConnecte() { return utilisateurConnecte != null; }
}
