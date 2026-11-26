package com.pps.parapente.dao;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Pilote;
import com.pps.parapente.model.Resultat;

class ResultatDAOTest extends DaoTestBase {

    private final ResultatDAO dao = new ResultatDAO();

    private Resultat resultat(Epreuve e, Pilote p, double points, boolean disqualifie) {
        Resultat r = new Resultat();
        r.setEpreuveId(e.getId());
        r.setPiloteId(p.getId());
        r.setValeurs(new LinkedHashMap<>(Map.of("distance", 12.5)));
        r.setPoints(points);
        r.setDisqualifie(disqualifie);
        r.setSaisiPar("benevole1");
        return r;
    }

    @Test
    void enregistrerPuisRelireConserveValeursJson() throws SQLException {
        Epreuve e = nouvelleEpreuve("Vitesse");
        Pilote p = nouveauPilote("L1", "Dupont");

        dao.enregistrer(resultat(e, p, 42.0, false));

        Resultat lu = dao.trouver(e.getId(), p.getId()).orElseThrow();
        assertEquals(12.5, lu.getValeurs().get("distance"), 1e-9);
        assertEquals(42.0, lu.getPoints(), 1e-9);
        assertFalse(lu.isDisqualifie());
        assertEquals("benevole1", lu.getSaisiPar());
        assertNotNull(lu.getDateSaisie());
    }
}