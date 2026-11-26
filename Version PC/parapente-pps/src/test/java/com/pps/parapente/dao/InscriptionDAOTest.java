package com.pps.parapente.dao;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}