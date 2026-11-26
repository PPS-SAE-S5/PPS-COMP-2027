package com.pps.parapente.dao;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Pilote;

class InscriptionDAOTest extends DaoTestBase {

    private final InscriptionDAO dao = new InscriptionDAO();

    @Test
    void inscrireDeuxFoisNeCreePasDeDoublon() throws SQLException {
        Pilote p = nouveauPilote("L1", "Dupont");
        Epreuve e = nouvelleEpreuve("Vitesse");

        dao.inscrire(p.getId(), e.getId());
        dao.inscrire(p.getId(), e.getId());

        assertEquals(List.of(p.getId()), dao.listerPilotesInscrits(e.getId()));
    }

    @Test
    void desinscrireRetireLInscription() throws SQLException {
        Pilote p = nouveauPilote("L1", "Dupont");
        Epreuve e = nouvelleEpreuve("Vitesse");
        dao.inscrire(p.getId(), e.getId());

        dao.desinscrire(p.getId(), e.getId());

        assertTrue(dao.listerPilotesInscrits(e.getId()).isEmpty());
    }

    @Test
    void inscrireTousLesPilotesSansDoubler() throws SQLException {
        Pilote a = nouveauPilote("L1", "Dupont");
        Pilote b = nouveauPilote("L2", "Martin");
        Epreuve e = nouvelleEpreuve("Vitesse");
        dao.inscrire(a.getId(), e.getId()); // déjà inscrit

        dao.inscrireTousLesPilotes(e.getId());

        List<Integer> inscrits = dao.listerPilotesInscrits(e.getId());
        assertEquals(2, inscrits.size());
        assertTrue(inscrits.containsAll(List.of(a.getId(), b.getId())));
    }

    @Test
    void inscrirePiloteSeulementAuxEpreuvesActives() throws SQLException {
        Epreuve active = nouvelleEpreuve("Active");
        Epreuve inactive = nouvelleEpreuve("Inactive");
        inactive.setActif(false);
        new EpreuveDAO().mettreAJour(inactive);
        Pilote p = nouveauPilote("L1", "Dupont");

        dao.inscrirePiloteAToutesLesEpreuvesActives(p.getId());

        assertEquals(List.of(active.getId()), dao.listerEpreuvesDuPilote(p.getId()));
    }

    @Test
    void supprimerLePiloteRetireSesInscriptionsOuEstRefuse() throws SQLException {
        Pilote p = nouveauPilote("L1", "Dupont");
        Epreuve e = nouvelleEpreuve("Vitesse");
        dao.inscrire(p.getId(), e.getId());

        try {
            new PiloteDAO().supprimer(p.getId());
            assertTrue(dao.listerEpreuvesDuPilote(p.getId()).isEmpty()); // cascade
        } catch (SQLException refus) {
            assertEquals(1, dao.listerEpreuvesDuPilote(p.getId()).size()); // pas de cascade
        }
    }

    @Test
    void listerEpreuvesDuPiloteRetourneSesEpreuves() throws SQLException {
        Pilote p = nouveauPilote("L1", "Dupont");
        Epreuve e1 = nouvelleEpreuve("E1");
        Epreuve e2 = nouvelleEpreuve("E2");
        dao.inscrire(p.getId(), e1.getId());
        dao.inscrire(p.getId(), e2.getId());

        List<Integer> epreuves = dao.listerEpreuvesDuPilote(p.getId());

        assertEquals(2, epreuves.size());
        assertTrue(epreuves.containsAll(List.of(e1.getId(), e2.getId())));
    }

    @Test
    void inscrireTousLesPilotesSansPiloteNeFaitRien() throws SQLException {
        Epreuve e = nouvelleEpreuve("E1");

        dao.inscrireTousLesPilotes(e.getId());

        assertTrue(dao.listerPilotesInscrits(e.getId()).isEmpty());
    }

    @Test
    void inscrirePiloteDeuxFoisNeDoublePasLesInscriptions() throws SQLException {
        Epreuve e = nouvelleEpreuve("E1");
        Pilote p = nouveauPilote("L1", "Dupont");

        dao.inscrirePiloteAToutesLesEpreuvesActives(p.getId());
        dao.inscrirePiloteAToutesLesEpreuvesActives(p.getId());

        assertEquals(List.of(e.getId()), dao.listerEpreuvesDuPilote(p.getId()));
    }
}