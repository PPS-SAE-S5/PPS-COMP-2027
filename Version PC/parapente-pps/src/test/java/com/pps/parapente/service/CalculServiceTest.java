package com.pps.parapente.service;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.mockConstruction;

import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.dao.PiloteDAO;
import com.pps.parapente.dao.ResultatDAO;

class CalculServiceTest {

    private MockedConstruction<ResultatDAO> resultatConstruction;
    private MockedConstruction<PiloteDAO> piloteConstruction;
    private MockedConstruction<InscriptionDAO> inscriptionConstruction;

    private ResultatDAO resultatDAO;
    private CalculService service;

    @BeforeEach
    void setUp() {
        // Tout "new XxxDAO()" fait pendant le test renverra un faux objet
        resultatConstruction = mockConstruction(ResultatDAO.class);
        piloteConstruction = mockConstruction(PiloteDAO.class);
        inscriptionConstruction = mockConstruction(InscriptionDAO.class);

        service = new CalculService();   // ses trois DAO sont maintenant des faux
        resultatDAO = resultatConstruction.constructed().get(0);
    }

    @AfterEach
    void tearDown() {
        resultatConstruction.close();
        piloteConstruction.close();
        inscriptionConstruction.close();
    }

    // ---------- evaluerFormule ----------

    @Test
    void formuleSimple() {
        double r = service.evaluerFormule("100 - temps*2 + nbrBalises*10",
                Map.of("temps", 5.0, "nbrBalises", 3.0));
        assertEquals(120.0, r, 0.0001);
    }

    @Test
    void formuleVideOuNullLeveUneException() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluerFormule("  ", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> service.evaluerFormule(null, Map.of()));
    }

    @Test
    void variableInconnueLeveUneException() {
        assertThrows(Exception.class, () -> service.evaluerFormule("x + y", Map.of("x", 1.0)));
    }
}