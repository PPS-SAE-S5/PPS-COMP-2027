package com.pps.parapente.dao;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void identifiantEnDoubleEstRefuse() throws SQLException {
        utilisateur("alice", true);

        assertThrows(SQLException.class, () -> utilisateur("alice", true));
    }

    @Test
    void supprimerRetireLUtilisateur() throws SQLException {
        Utilisateur u = utilisateur("alice", true);

        dao.supprimer(u.getId());

        assertTrue(dao.trouverParIdentifiant("alice").isEmpty());
        assertTrue(dao.listerTous().stream().noneMatch(x -> x.getId() == u.getId()));
    }

    @Test
    void listerTousTrieParIdentifiant() throws SQLException {
        utilisateur("zoe", true);
        utilisateur("alice", true);
        utilisateur("marc", false);

        List<String> ids = dao.listerTous().stream().map(Utilisateur::getIdentifiant).toList();

        assertTrue(ids.indexOf("alice") < ids.indexOf("marc"));
        assertTrue(ids.indexOf("marc") < ids.indexOf("zoe"));
        assertTrue(ids.contains("marc")); // un compte inactif est bien listé
    }

    @Test
    void definirActifFalseDesactiveLeCompte() throws SQLException {
        Utilisateur u = utilisateur("alice", true);

        dao.definirActif(u.getId(), false);

        assertTrue(dao.trouverParIdentifiant("alice").isEmpty());
    }
}