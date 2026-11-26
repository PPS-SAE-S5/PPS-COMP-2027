package com.pps.parapente.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.pps.parapente.model.BaremePoint;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.EpreuveParametre;
import com.pps.parapente.model.ModeCalcul;
import com.pps.parapente.model.SensClassement;

class EpreuveDAOTest extends DaoTestBase {

    private final EpreuveDAO dao = new EpreuveDAO();

    @Test
    void creerEnregistreParametresEtBareme() throws SQLException {
        Epreuve e = new Epreuve();
        e.setNom("Précision");
        e.setModeCalcul(ModeCalcul.BAREME);
        e.setValeurCle("temps");
        e.setSensClassement(SensClassement.ASC);
        e.setActif(true);
        EpreuveParametre p = new EpreuveParametre("temps", "Temps", "s");
        p.setObligatoire(true);
        p.setOrdre(1);
        e.setParametres(new ArrayList<>(List.of(p)));
        e.setBareme(new ArrayList<>(List.of(new BaremePoint(1, 100), new BaremePoint(2, 80))));

        dao.creer(e);

        assertTrue(e.getId() > 0);
        Epreuve lue = dao.trouverParId(e.getId()).orElseThrow();
        assertEquals("Précision", lue.getNom());
        assertEquals(ModeCalcul.BAREME, lue.getModeCalcul());
        assertEquals(SensClassement.ASC, lue.getSensClassement());
        assertEquals(1, lue.getParametres().size());
        assertEquals("temps", lue.getParametres().get(0).getNomVariable());
        assertTrue(lue.getParametres().get(0).isObligatoire());
        assertEquals(2, lue.getBareme().size());
        assertEquals(80.0, lue.getBareme().get(1).getPoints(), 1e-9);
    }

    @Test
    void mettreAJourRemplaceParametresEtBareme() throws SQLException {
        Epreuve e = nouvelleEpreuve("Vitesse");
        e.setBareme(new ArrayList<>(List.of(new BaremePoint(1, 10))));
        e.setParametres(new ArrayList<>(List.of(new EpreuveParametre("a", "A", "m"))));
        dao.mettreAJour(e);

        e.setNom("Vitesse 2");
        e.setBareme(new ArrayList<>(List.of(new BaremePoint(1, 50), new BaremePoint(2, 40))));
        e.setParametres(new ArrayList<>());
        dao.mettreAJour(e);

        Epreuve lue = dao.trouverParId(e.getId()).orElseThrow();
        assertEquals("Vitesse 2", lue.getNom());
        assertEquals(2, lue.getBareme().size());
        assertTrue(lue.getParametres().isEmpty());
    }

    @Test
    void listerToutesTrieParOrdre() throws SQLException {
        Epreuve b = nouvelleEpreuve("B");
        b.setOrdre(2);
        dao.mettreAJour(b);
        Epreuve a = nouvelleEpreuve("A");
        a.setOrdre(1);
        dao.mettreAJour(a);

        assertEquals(List.of("A", "B"),
                dao.listerToutes().stream().map(Epreuve::getNom).toList());
    }

    @Test
    void supprimerRetireLEpreuve() throws SQLException {
        Epreuve e = nouvelleEpreuve("Vitesse");

        dao.supprimer(e.getId());

        assertTrue(dao.trouverParId(e.getId()).isEmpty());
    }

    @Test
    void creerInvalideAnnuleToutEtRestitueLAutoCommit() throws Exception {
        Epreuve e = new Epreuve();
        e.setNom(null); // refusé par le schéma
        e.setModeCalcul(ModeCalcul.FORMULE);
        e.setSensClassement(SensClassement.DESC);
        e.setParametres(new ArrayList<>());
        e.setBareme(new ArrayList<>());

        assertThrows(SQLException.class, () -> dao.creer(e));

        assertTrue(dao.listerToutes().isEmpty());
        assertTrue(DatabaseManager.getConnection().getAutoCommit());
    }

    @Test
    void trouverParIdInconnuRetourneVide() throws SQLException {
        assertTrue(dao.trouverParId(9999).isEmpty());
    }

    @Test
    void listerToutesChargeParametresEtBareme() throws SQLException {
        Epreuve e = nouvelleEpreuve("Vitesse");
        e.setBareme(new ArrayList<>(List.of(new BaremePoint(1, 10), new BaremePoint(2, 5))));
        e.setParametres(new ArrayList<>(List.of(new EpreuveParametre("t", "Temps", "s"))));
        dao.mettreAJour(e);

        Epreuve lue = dao.listerToutes().get(0);

        assertEquals(2, lue.getBareme().size());
        assertEquals(1, lue.getParametres().size());
    }
}