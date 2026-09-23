package com.pps.parapente.service;

import com.pps.parapente.dao.UtilisateurDAO;
import com.pps.parapente.model.Utilisateur;
import com.pps.parapente.util.SecuriteUtil;
import com.pps.parapente.util.SessionManager;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    /**
     * Tente une connexion. Retourne l'utilisateur si les identifiants sont valides.
     */
    public Optional<Utilisateur> seConnecter(String identifiant, String motDePasseClair) throws SQLException {
        Optional<Utilisateur> utilisateur = utilisateurDAO.trouverParIdentifiant(identifiant);
        if (utilisateur.isEmpty()) return Optional.empty();

        String hache = SecuriteUtil.hacher(motDePasseClair);
        if (!hache.equals(utilisateur.get().getMotDePasseHache())) return Optional.empty();

        SessionManager.connecter(utilisateur.get());
        return utilisateur;
    }

    public void seDeconnecter() {
        SessionManager.deconnecter();
    }
}
