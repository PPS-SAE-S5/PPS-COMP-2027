package com.pps.parapente.dao;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.pps.parapente.model.Role;
import com.pps.parapente.model.Utilisateur;

class UtilisateurDAOTest extends DaoTestBase {

    private final UtilisateurDAO dao = new UtilisateurDAO();

    private Utilisateur utilisateur(String identifiant, boolean actif) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdentifiant(identifiant);
        u.setMotDePasseHache("hash-1");
        u.setRole(Role.values()[0]);
        u.setActif(actif);
        dao.creer(u);
        return u;
    }

    @Test
    void creerPuisTrouverParIdentifiant() throws SQLException {
        Utilisateur u = utilisateur("alice", true);

        Utilisateur lu = dao.trouverParIdentifiant("alice").orElseThrow();

        assertTrue(u.getId() > 0);
        assertEquals("hash-1", lu.getMotDePasseHache());
        assertEquals(Role.values()[0], lu.getRole());
        assertNull(lu.getPiloteId());
    }

    @Test
    void compteInactifNEstPasTrouveALaConnexion() throws SQLException {
        utilisateur("bob", false);

        assertTrue(dao.trouverParIdentifiant("bob").isEmpty());
    }

    @Test
    void definirActifReactiveLeCompte() throws SQLException {
        Utilisateur u = utilisateur("bob", false);

        dao.definirActif(u.getId(), true);

        assertTrue(dao.trouverParIdentifiant("bob").isPresent());
    }

    @Test
    void mettreAJourMotDePasse() throws SQLException {
        Utilisateur u = utilisateur("alice", true);

        dao.mettreAJourMotDePasse(u.getId(), "hash-2");

        assertEquals("hash-2", dao.trouverParIdentifiant("alice").orElseThrow().getMotDePasseHache());
    }
}