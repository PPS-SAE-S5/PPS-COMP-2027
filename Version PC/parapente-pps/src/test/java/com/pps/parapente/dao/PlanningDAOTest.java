package com.pps.parapente.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.pps.parapente.model.Atelier;
import com.pps.parapente.model.Creneau;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Journee;

class PlanningDAOTest extends DaoTestBase {

    private final PlanningDAO dao = new PlanningDAO();

    private Journee journee(String nom) throws SQLException {
        Journee j = new Journee();
        j.setNom(nom);
        j.setDateJournee(LocalDate.of(2026, 6, 15));
        dao.creerJournee(j);
        return j;
    }

    private Atelier atelier(Journee j, String nom, Epreuve... epreuves) throws SQLException {
        Atelier a = new Atelier();
        a.setJourneeId(j.getId());
        a.setNom(nom);
        a.setCreneau(Creneau.MATIN);
        a.setHeureDebut("08:00");
        a.setHeureFin("12:00");
        a.setLieu("Terrain A");
        a.setResponsable("M. Dupont");
        a.setEpreuves(new ArrayList<>(List.of(epreuves)));
        dao.creerAtelier(a);
        return a;
    }

    @Test
    void creerJourneeAttribueIdEtOrdreCroissant() throws SQLException {
        Journee a = journee("J1");
        Journee b = journee("J2");

        List<Journee> liste = dao.listerJournees();

        assertTrue(a.getId() > 0);
        assertEquals(List.of("J1", "J2"), liste.stream().map(Journee::getNom).toList());
        assertTrue(liste.get(0).getOrdre() < liste.get(1).getOrdre());
        assertEquals(LocalDate.of(2026, 6, 15), liste.get(0).getDateJournee());
    }

    @Test
    void creerAtelierConserveLOrdreDesEpreuves() throws SQLException {
        Journee j = journee("J1");
        Epreuve e1 = nouvelleEpreuve("E1");
        Epreuve e2 = nouvelleEpreuve("E2");
        Epreuve e3 = nouvelleEpreuve("E3");

        atelier(j, "Atelier", e3, e1, e2);

        Atelier lu = dao.listerAteliers(j.getId()).get(0);
        assertEquals(List.of("E3", "E1", "E2"),
                lu.getEpreuves().stream().map(Epreuve::getNom).toList());
    }

    @Test
    void uneEpreuveNePeutEtreQueDansUnAtelier() throws SQLException {
        Journee j = journee("J1");
        Epreuve e = nouvelleEpreuve("E1");
        Atelier premier = atelier(j, "Premier", e);

        atelier(j, "Second", e);

        List<Atelier> ateliers = dao.listerAteliers(j.getId());
        Atelier lePremier = ateliers.stream().filter(a -> a.getId() == premier.getId()).findFirst().orElseThrow();
        assertTrue(lePremier.getEpreuves().isEmpty());
        assertEquals(1, ateliers.stream().mapToInt(a -> a.getEpreuves().size()).sum());
    }
}