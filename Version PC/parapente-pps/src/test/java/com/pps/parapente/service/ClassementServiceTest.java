package com.pps.parapente.service;

import com.pps.parapente.dao.EpreuveDAO;
import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.dao.PiloteDAO;
import com.pps.parapente.dao.ResultatDAO;
import com.pps.parapente.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de ClassementService.
 *
 * Les 4 DAO sont créés en interne par le service ("new ...DAO()"), on les
 * remplace donc par des mocks via mockConstruction. Leurs réponses lisent les
 * champs de test (pilotes, epreuves, resultatsParEpreuve, inscritsParEpreuve),
 * que chaque test remplit avant d'appeler le service.
 *
 * Hypothèses sur vos classes modèle (à adapter si besoin) :
 *  - Pilote : getId()
 *  - Epreuve : getId(), isActif(), isCompteDansGeneral()
 *  - Resultat : getPiloteId(), getPoints(), setPilote(), getPilote()
 *  - LigneClassement : getPilote(), getTotalPoints(), getRang(), getPointsParEpreuve()
 *  - FiltreGenre / FiltreCategorie : enums avec accepte(Pilote)
 */
class ClassementServiceTest {

    private List<Pilote> pilotes = new ArrayList<>();
    private List<Epreuve> epreuves = new ArrayList<>();
    private Map<Integer, List<Resultat>> resultatsParEpreuve = new HashMap<>();
    private Map<Integer, List<Integer>> inscritsParEpreuve = new HashMap<>();

    private MockedConstruction<PiloteDAO> piloteDao;
    private MockedConstruction<EpreuveDAO> epreuveDao;
    private MockedConstruction<ResultatDAO> resultatDao;
    private MockedConstruction<InscriptionDAO> inscriptionDao;

    @BeforeEach
    void setUp() {
        piloteDao = mockConstruction(PiloteDAO.class, (mock, ctx) ->
                when(mock.listerTous()).thenAnswer(i -> pilotes));
        epreuveDao = mockConstruction(EpreuveDAO.class, (mock, ctx) ->
                when(mock.listerToutes()).thenAnswer(i -> epreuves));
        resultatDao = mockConstruction(ResultatDAO.class, (mock, ctx) ->
                when(mock.listerParEpreuve(anyInt())).thenAnswer(i ->
                        resultatsParEpreuve.getOrDefault(i.<Integer>getArgument(0), List.of())));
        inscriptionDao = mockConstruction(InscriptionDAO.class, (mock, ctx) ->
                when(mock.listerPilotesInscrits(anyInt())).thenAnswer(i ->
                        inscritsParEpreuve.getOrDefault(i.<Integer>getArgument(0), List.of())));
    }

    @AfterEach
    void tearDown() {
        piloteDao.close();
        epreuveDao.close();
        resultatDao.close();
        inscriptionDao.close();
    }

    // ---------- helpers ----------

    private Pilote pilote(int id) {
        Pilote p = mock(Pilote.class);
        when(p.getId()).thenReturn(id);
        pilotes.add(p);
        return p;
    }

    private Epreuve epreuve(int id, boolean actif, boolean compteDansGeneral) {
        Epreuve e = mock(Epreuve.class);
        when(e.getId()).thenReturn(id);
        when(e.isActif()).thenReturn(actif);
        when(e.isCompteDansGeneral()).thenReturn(compteDansGeneral);
        epreuves.add(e);
        return e;
    }

    private Resultat resultat(Pilote p, double points) {
        int piloteId = p.getId(); // à lire AVANT de commencer le stubbing de r
        Resultat r = mock(Resultat.class);
        when(r.getPiloteId()).thenReturn(piloteId);
        when(r.getPoints()).thenReturn(points);
        when(r.getPilote()).thenReturn(p);
        return r;
    }

    private void inscrits(Epreuve e, Pilote... ps) {
        inscritsParEpreuve.put(e.getId(), Arrays.stream(ps).map(Pilote::getId).toList());
    }

    private void resultats(Epreuve e, Resultat... rs) {
        resultatsParEpreuve.put(e.getId(), List.of(rs));
    }

    private FiltreGenre filtreGenreQuiAccepte(Pilote... acceptes) {
        FiltreGenre f = mock(FiltreGenre.class);
        for (Pilote p : pilotes) when(f.accepte(p)).thenReturn(Arrays.asList(acceptes).contains(p));
        return f;
    }

    // ---------- classementGénéral ----------

    @Test
    @DisplayName("Somme des points sur plusieurs épreuves, tri décroissant et rangs 1..n")
    void general_sommeEtTriEtRangs() throws SQLException {
        Pilote a = pilote(1), b = pilote(2), c = pilote(3);
        Epreuve e1 = epreuve(10, true, true), e2 = epreuve(20, true, true);
        inscrits(e1, a, b, c);
        inscrits(e2, a, b, c);
        resultats(e1, resultat(a, 10), resultat(b, 30), resultat(c, 20));
        resultats(e2, resultat(a, 5), resultat(b, 10), resultat(c, 40));

        List<LigneClassement> classement = new ClassementService().calculerClassementGeneral();

        assertEquals(3, classement.size());
        // c = 60, b = 40, a = 15
        assertSame(c, classement.get(0).getPilote());
        assertSame(b, classement.get(1).getPilote());
        assertSame(a, classement.get(2).getPilote());
        assertEquals(60.0, classement.get(0).getTotalPoints(), 1e-9);
        assertEquals(40.0, classement.get(1).getTotalPoints(), 1e-9);
        assertEquals(15.0, classement.get(2).getTotalPoints(), 1e-9);
        assertEquals(1, classement.get(0).getRang());
        assertEquals(2, classement.get(1).getRang());
        assertEquals(3, classement.get(2).getRang());
    }

    @Test
    @DisplayName("Règle du club : inscrit sans résultat => 0 point pour l'épreuve")
    void general_inscritSansResultat_zeroPoint() throws SQLException {
        Pilote a = pilote(1), b = pilote(2);
        Epreuve e1 = epreuve(10, true, true);
        inscrits(e1, a, b);
        resultats(e1, resultat(a, 25)); // b n'a pas de résultat

        List<LigneClassement> classement = new ClassementService().calculerClassementGeneral();

        LigneClassement ligneB = classement.stream()
                .filter(l -> l.getPilote() == b).findFirst().orElseThrow();
        assertEquals(0.0, ligneB.getTotalPoints(), 1e-9);
        assertEquals(0.0, ligneB.getPointsParEpreuve().get(10), 1e-9);
        assertSame(a, classement.get(0).getPilote());
    }

    @Test
    @DisplayName("Pilote non inscrit : ses points ne sont pas comptés")
    void general_piloteNonInscrit_pasDePoints() throws SQLException {
        Pilote a = pilote(1), b = pilote(2);
        Epreuve e1 = epreuve(10, true, true);
        inscrits(e1, a); // b non inscrit
        resultats(e1, resultat(a, 10), resultat(b, 99));

        List<LigneClassement> classement = new ClassementService().calculerClassementGeneral();

        LigneClassement ligneB = classement.stream()
                .filter(l -> l.getPilote() == b).findFirst().orElseThrow();
        assertEquals(0.0, ligneB.getTotalPoints(), 1e-9);
        assertTrue(ligneB.getPointsParEpreuve().isEmpty());
    }

    @Test
    @DisplayName("Épreuve inactive ou ne comptant pas dans le général : ignorée")
    void general_epreuvesNonComptees_ignorees() throws SQLException {
        Pilote a = pilote(1);
        Epreuve inactive = epreuve(10, false, true);
        Epreuve horsGeneral = epreuve(20, true, false);
        Epreuve ok = epreuve(30, true, true);
        for (Epreuve e : List.of(inactive, horsGeneral, ok)) {
            inscrits(e, a);
            resultats(e, resultat(a, 10));
        }

        List<LigneClassement> classement = new ClassementService().calculerClassementGeneral();

        assertEquals(10.0, classement.get(0).getTotalPoints(), 1e-9);
        assertEquals(Set.of(30), classement.get(0).getPointsParEpreuve().keySet());
    }

    @Test
    @DisplayName("Inscrit qui n'existe plus dans la liste des pilotes : ignoré sans erreur")
    void general_inscritInconnu_ignore() throws SQLException {
        Pilote a = pilote(1);
        Epreuve e1 = epreuve(10, true, true);
        inscritsParEpreuve.put(10, List.of(1, 999)); // 999 supprimé entre-temps
        resultats(e1, resultat(a, 10));

        List<LigneClassement> classement = new ClassementService().calculerClassementGeneral();

        assertEquals(1, classement.size());
        assertSame(a, classement.get(0).getPilote());
    }

    @Test
    @DisplayName("Aucun pilote / aucune épreuve : liste vide")
    void general_aucuneDonnee_listeVide() throws SQLException {
        assertTrue(new ClassementService().calculerClassementGeneral().isEmpty());
    }

    @Test
    @DisplayName("Filtre de genre : seuls les pilotes acceptés apparaissent, rangs recalculés")
    void general_filtreGenre_rangsRecalcules() throws SQLException {
        Pilote homme = pilote(1), femme1 = pilote(2), femme2 = pilote(3);
        Epreuve e1 = epreuve(10, true, true);
        inscrits(e1, homme, femme1, femme2);
        resultats(e1, resultat(homme, 100), resultat(femme1, 20), resultat(femme2, 30));

        FiltreGenre femmes = filtreGenreQuiAccepte(femme1, femme2);

        List<LigneClassement> classement = new ClassementService().calculerClassementGeneral(femmes);

        assertEquals(2, classement.size());
        assertSame(femme2, classement.get(0).getPilote());
        assertEquals(1, classement.get(0).getRang()); // 1re femme => rang 1
        assertSame(femme1, classement.get(1).getPilote());
        assertEquals(2, classement.get(1).getRang());
    }

    @Test
    @DisplayName("Filtre de catégorie : seuls les pilotes acceptés apparaissent")
    void general_filtreCategorie() throws SQLException {
        Pilote pompier = pilote(1), open = pilote(2);
        Epreuve e1 = epreuve(10, true, true);
        inscrits(e1, pompier, open);
        resultats(e1, resultat(pompier, 10), resultat(open, 20));

        FiltreCategorie cat = mock(FiltreCategorie.class);
        when(cat.accepte(pompier)).thenReturn(true);
        when(cat.accepte(open)).thenReturn(false);

        List<LigneClassement> classement =
                new ClassementService().calculerClassementGeneral(FiltreGenre.GENERAL, cat);

        assertEquals(1, classement.size());
        assertSame(pompier, classement.get(0).getPilote());
    }

    @Test
    @DisplayName("Erreur SQL d'un DAO : propagée")
    void general_erreurDao_propageSQLException() throws SQLException {
        piloteDao.close();
        piloteDao = mockConstruction(PiloteDAO.class, (mock, ctx) ->
                when(mock.listerTous()).thenThrow(new SQLException("DB indisponible")));

        ClassementService service = new ClassementService();

        assertThrows(SQLException.class, service::calculerClassementGeneral);
    }

    // ---------- classementEpreuve ----------

    @Test
    @DisplayName("Classement d'épreuve : tri par points décroissants et pilote associé")
    void epreuve_triDecroissantEtPiloteAssocie() throws SQLException {
        Pilote a = pilote(1), b = pilote(2), c = pilote(3);
        Epreuve e1 = epreuve(10, true, true);
        Resultat ra = resultat(a, 10), rb = resultat(b, 30), rc = resultat(c, 20);
        resultats(e1, ra, rb, rc);

        List<Resultat> classement = new ClassementService().calculerClassementEpreuve(e1);

        assertEquals(List.of(rb, rc, ra), classement);
        verify(ra).setPilote(a);
        verify(rb).setPilote(b);
        verify(rc).setPilote(c);
    }

    @Test
    @DisplayName("Classement d'épreuve : le filtre de genre retire les résultats non acceptés")
    void epreuve_filtreGenre() throws SQLException {
        Pilote homme = pilote(1), femme = pilote(2);
        Epreuve e1 = epreuve(10, true, true);
        Resultat rh = resultat(homme, 50), rf = resultat(femme, 10);
        resultats(e1, rh, rf);

        FiltreGenre femmes = filtreGenreQuiAccepte(femme);

        List<Resultat> classement = new ClassementService().calculerClassementEpreuve(e1, femmes);

        assertEquals(List.of(rf), classement);
    }

    @Test
    @DisplayName("Classement d'épreuve sans résultat : liste vide")
    void epreuve_sansResultat_listeVide() throws SQLException {
        Epreuve e1 = epreuve(10, true, true);

        assertTrue(new ClassementService().calculerClassementEpreuve(e1).isEmpty());
    }
}