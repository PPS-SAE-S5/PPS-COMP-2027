package com.pps.parapente.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void listerEpreuvesNonAffecteesEtDisponibles() throws SQLException {
        Journee j = journee("J1");
        Epreuve placee = nouvelleEpreuve("Placée");
        Epreuve libre = nouvelleEpreuve("Libre");
        Atelier a = atelier(j, "Atelier", placee);

        assertEquals(List.of("Libre"),
                dao.listerEpreuvesNonAffectees().stream().map(Epreuve::getNom).toList());
        assertEquals(2, dao.listerEpreuvesDisponibles(a.getId()).size()); // libre + déjà dans l'atelier
    }

    @Test
    void champsVidesSontEnregistresCommeNull() throws SQLException {
        Journee j = journee("J1");
        Atelier a = atelier(j, "Atelier");
        a.setLieu("   ");
        a.setResponsable("");
        dao.mettreAJourAtelier(a);

        Atelier lu = dao.listerAteliers(j.getId()).get(0);
        assertNull(lu.getLieu());
        assertNull(lu.getResponsable());
    }

    @Test
    void supprimerJourneeSupprimeSesAteliersMaisPasLesEpreuves() throws SQLException {
        Journee j = journee("J1");
        Epreuve e = nouvelleEpreuve("E1");
        atelier(j, "Atelier", e);

        dao.supprimerJournee(j.getId());

        assertTrue(dao.listerJournees().isEmpty());
        assertEquals(0, dao.compterAteliers(j.getId()));
        assertTrue(new EpreuveDAO().trouverParId(e.getId()).isPresent());
        assertEquals(1, dao.listerEpreuvesNonAffectees().size()); // redevient « à placer »
    }

    @Test
    void compterAteliersParJournee() throws SQLException {
        Journee j1 = journee("J1");
        Journee j2 = journee("J2");
        atelier(j1, "A");
        atelier(j1, "B");

        assertEquals(2, dao.compterAteliers(j1.getId()));
        assertEquals(0, dao.compterAteliers(j2.getId()));
    }
}