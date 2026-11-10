package com.pps.parapente.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.dao.PiloteDAO;
import com.pps.parapente.dao.ResultatDAO;
import com.pps.parapente.model.BaremePoint;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.ModeCalcul;
import com.pps.parapente.model.Resultat;
import com.pps.parapente.model.SensClassement;

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

        // ---------- Helpers pour construire les objets de test ----------

    private Epreuve epreuveBareme(String cle, SensClassement sens, double... points) {
        Epreuve e = new Epreuve();
        e.setId(1);
        e.setModeCalcul(ModeCalcul.BAREME);
        e.setValeurCle(cle);
        e.setSensClassement(sens);
        List<BaremePoint> bareme = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            bareme.add(new BaremePoint(i + 1, points[i]));
        }
        e.setBareme(bareme);
        return e;
    }

    private Resultat resultat(int piloteId, Map<String, Double> valeurs, boolean disqualifie) {
        Resultat r = new Resultat();
        r.setEpreuveId(1);
        r.setPiloteId(piloteId);
        r.setValeurs(valeurs);
        r.setDisqualifie(disqualifie);
        return r;
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

        // ---------- recalculerBareme ----------

    @Test
    void baremeDescLePlusGrandGagne() throws Exception {
        Epreuve e = epreuveBareme("distance", SensClassement.DESC, 100, 80, 60);
        Resultat a = resultat(1, Map.of("distance", 10.0), false);
        Resultat b = resultat(2, Map.of("distance", 30.0), false);
        Resultat c = resultat(3, Map.of("distance", 20.0), false);
        when(resultatDAO.listerParEpreuve(1)).thenReturn(List.of(a, b, c));

        service.recalculerBareme(e);

        assertEquals(100, b.getPoints()); // 30 km -> rang 1
        assertEquals(80, c.getPoints());  // 20 km -> rang 2
        assertEquals(60, a.getPoints());  // 10 km -> rang 3
    }

    @Test
    void baremeAscLePlusPetitGagne() throws Exception {
        Epreuve e = epreuveBareme("temps", SensClassement.ASC, 100, 80);
        Resultat lent = resultat(1, Map.of("temps", 50.0), false);
        Resultat rapide = resultat(2, Map.of("temps", 20.0), false);
        when(resultatDAO.listerParEpreuve(1)).thenReturn(List.of(lent, rapide));

        service.recalculerBareme(e);

        assertEquals(100, rapide.getPoints());
        assertEquals(80, lent.getPoints());
    }
}