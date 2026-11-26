package com.pps.parapente.dao;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.pps.parapente.model.Categorie;
import com.pps.parapente.model.Pilote;

class PiloteDAOTest extends DaoTestBase {

    private final PiloteDAO dao = new PiloteDAO();

    @Test
    void creerAttribueUnIdEtPermetDeRelire() throws SQLException {
        Pilote p = new Pilote();
        p.setNumeroLicence("LIC-001");
        p.setNom("Dupont");
        p.setPrenom("Jean");
        p.setDepartement("31");
        p.setPoids(80.5);
        p.setEmail("jean@exemple.fr");
        p.setAnneeNaissance(1990);
        p.setGenre("H");
        p.setNumeroPilote(7);
        p.setCategorie(Categorie.OPEN);

        dao.creer(p);

        assertTrue(p.getId() > 0);
        Pilote lu = dao.trouverParId(p.getId()).orElseThrow();
        assertEquals("LIC-001", lu.getNumeroLicence());
        assertEquals("Dupont", lu.getNom());
        assertEquals("31", lu.getDepartement());
        assertEquals(80.5, lu.getPoids(), 1e-9);
        assertEquals(Integer.valueOf(1990), lu.getAnneeNaissance());
        assertEquals(Integer.valueOf(7), lu.getNumeroPilote());
        assertEquals(Categorie.OPEN, lu.getCategorie());
        assertNotNull(lu.getDateInscription());
    }

    @Test
    void champsOptionnelsAbsentsSontRelusNull() throws SQLException {
        Pilote p = nouveauPilote("LIC-002", "Martin");

        Pilote lu = dao.trouverParId(p.getId()).orElseThrow();

        assertNull(lu.getPoids());
        assertNull(lu.getAnneeNaissance());
        assertNull(lu.getNumeroPilote());
    }
}