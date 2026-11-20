package com.pps.parapente.service;

import com.pps.parapente.dao.EpreuveDAO;
import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.dao.PiloteDAO;
import com.pps.parapente.dao.ResultatDAO;
import com.pps.parapente.model.*;
import com.pps.parapente.model.SuiviParticipant.LigneSuivi;
import com.pps.parapente.model.SuiviParticipant.Statut;
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
 * Tests unitaires de SuiviParticipantService.
 *
 * Le service crée ses 4 DAO et un ClassementService en interne : tous sont
 * remplacés par des mocks via mockConstruction. Leurs réponses lisent les
 * champs de test (pilotes, epreuves, resultats, epreuvesInscrites, classement)
 * que chaque test remplit avant d'appeler le service.
 *
 * IMPORTANT : je n'ai pas vu SuiviParticipant ni LigneSuivi. Tous les accès à
 * leurs getters sont regroupés dans les petites méthodes "accesseurs" en bas
 * de ce fichier : si les noms diffèrent chez vous, il suffit de les corriger
 * à cet endroit.
 */
class SuiviParticipantServiceTest {

    private List<Pilote> pilotes = new ArrayList<>();
    private List<Epreuve> epreuves = new ArrayList<>();
    private List<Resultat> resultats = new ArrayList<>();
    private List<Integer> epreuvesInscrites = new ArrayList<>();
    private List<LigneClassement> classement = new ArrayList<>();

    private MockedConstruction<PiloteDAO> piloteDao;
    private MockedConstruction<EpreuveDAO> epreuveDao;
    private MockedConstruction<ResultatDAO> resultatDao;
    private MockedConstruction<InscriptionDAO> inscriptionDao;
    private MockedConstruction<ClassementService> classementService;

    private SuiviParticipantService service;

    @BeforeEach
    void setUp() {
        piloteDao = mockConstruction(PiloteDAO.class, (mock, ctx) ->
                when(mock.listerTous()).thenAnswer(i -> pilotes));
        epreuveDao = mockConstruction(EpreuveDAO.class, (mock, ctx) ->
                when(mock.listerToutes()).thenAnswer(i -> epreuves));
        resultatDao = mockConstruction(ResultatDAO.class, (mock, ctx) ->
                when(mock.listerTous()).thenAnswer(i -> resultats));
        inscriptionDao = mockConstruction(InscriptionDAO.class, (mock, ctx) ->
                when(mock.listerEpreuvesDuPilote(anyInt())).thenAnswer(i -> epreuvesInscrites));
        classementService = mockConstruction(ClassementService.class, (mock, ctx) ->
                when(mock.calculerClassementGeneral(FiltreGenre.GENERAL)).thenAnswer(i -> classement));

        service = new SuiviParticipantService();
    }

    @AfterEach
    void tearDown() {
        piloteDao.close();
        epreuveDao.close();
        resultatDao.close();
        inscriptionDao.close();
        classementService.close();
    }

    // ---------- helpers de données ----------

    private Pilote pilote(int id, String nom, String prenom) {
        Pilote p = mock(Pilote.class);
        when(p.getId()).thenReturn(id);
        when(p.getNom()).thenReturn(nom);
        when(p.getPrenom()).thenReturn(prenom);
        return p;
    }

    private Epreuve epreuve(int id, String nom, boolean actif, boolean compteDansGeneral,
                            EpreuveParametre... parametres) {
        List<EpreuveParametre> liste = Arrays.asList(parametres);
        Epreuve e = mock(Epreuve.class);
        when(e.getId()).thenReturn(id);
        when(e.getNom()).thenReturn(nom);
        when(e.isActif()).thenReturn(actif);
        when(e.isCompteDansGeneral()).thenReturn(compteDansGeneral);
        when(e.getParametres()).thenReturn(liste);
        epreuves.add(e);
        return e;
    }

    private Resultat resultat(int piloteId, int epreuveId, double points, boolean disqualifie,
                              Map<String, Double> valeurs) {
        Resultat r = mock(Resultat.class);
        when(r.getPiloteId()).thenReturn(piloteId);
        when(r.getEpreuveId()).thenReturn(epreuveId);
        when(r.getPoints()).thenReturn(points);
        when(r.isDisqualifie()).thenReturn(disqualifie);
        when(r.getValeurs()).thenReturn(valeurs);
        resultats.add(r);
        return r;
    }

    private Resultat resultat(int piloteId, int epreuveId, double points) {
        return resultat(piloteId, epreuveId, points, false, new LinkedHashMap<>());
    }

    private Map<String, Double> valeurs(Object... clesEtValeurs) {
        Map<String, Double> m = new LinkedHashMap<>();
        for (int i = 0; i < clesEtValeurs.length; i += 2) {
            m.put((String) clesEtValeurs[i], ((Number) clesEtValeurs[i + 1]).doubleValue());
        }
        return m;
    }

    private void ligneClassement(Pilote p, double total, int rang) {
        int id = p.getId();
        LigneClassement l = mock(LigneClassement.class);
        when(l.getPilote()).thenReturn(p);
        when(l.getTotalPoints()).thenReturn(total);
        when(l.getRang()).thenReturn(rang);
        classement.add(l);
    }

    private void inscrit(int... idsEpreuves) {
        for (int id : idsEpreuves) epreuvesInscrites.add(id);
    }

    // =====================================================================
    // listerPilotes
    // =====================================================================

    @Test
    @DisplayName("listerPilotes : tri par nom puis prénom, sans tenir compte de la casse")
    void listerPilotes_triNomPuisPrenom() throws SQLException {
        Pilote martinPaul = pilote(1, "martin", "Paul");
        Pilote dupontZoe = pilote(2, "Dupont", "Zoé");
        Pilote dupontAlice = pilote(3, "DUPONT", "alice");
        pilotes.addAll(List.of(martinPaul, dupontZoe, dupontAlice));

        List<Pilote> tries = service.listerPilotes();

        assertEquals(List.of(dupontAlice, dupontZoe, martinPaul), tries);
    }

    @Test
    @DisplayName("listerPilotes : nom ou prénom null => traité comme vide (en premier), sans exception")
    void listerPilotes_nomOuPrenomNull() throws SQLException {
        Pilote sansNom = pilote(1, null, "Jean");
        Pilote avecNom = pilote(2, "Abel", "Marc");
        pilotes.addAll(List.of(avecNom, sansNom));

        List<Pilote> tries = service.listerPilotes();

        assertEquals(List.of(sansNom, avecNom), tries);
    }

    @Test
    @DisplayName("listerPilotes : ne modifie pas la liste renvoyée par le DAO")
    void listerPilotes_neModifiePasLaListeDuDao() throws SQLException {
        Pilote b = pilote(1, "Bernard", "Luc");
        Pilote a = pilote(2, "Arnaud", "Eve");
        pilotes.addAll(List.of(b, a));

        service.listerPilotes();

        assertEquals(List.of(b, a), pilotes);
    }

    @Test
    @DisplayName("listerPilotes : aucun pilote => liste vide")
    void listerPilotes_vide() throws SQLException {
        assertTrue(service.listerPilotes().isEmpty());
    }

    // =====================================================================
    // detailler : statuts et filtrage des épreuves
    // =====================================================================

    @Test
    @DisplayName("Épreuve inscrite sans résultat : « pas encore saisi », points null, 1 attendue / 0 saisie")
    void detailler_inscritSansResultat() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Vitesse", true, true);
        inscrit(10);

        SuiviParticipant suivi = service.detailler(p);

        assertEquals(1, lignes(suivi).size());
        LigneSuivi l = lignes(suivi).get(0);
        assertEquals("Vitesse", nomEpreuve(l));
        assertEquals("", valeurs(l));
        assertNull(points(l));
        assertEquals(Statut.PAS_ENCORE_SAISI, statut(l));
        assertEquals(1, attendues(suivi));
    }

    @Test
    @DisplayName("Épreuve avec résultat : « saisi » avec ses points, compte comme saisie")
    void detailler_resultatSaisi() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Vitesse", true, true);
        inscrit(10);
        resultat(1, 10, 42.5);

        SuiviParticipant suivi = service.detailler(p);

        LigneSuivi l = lignes(suivi).get(0);
        assertEquals(Statut.SAISI, statut(l));
        assertEquals(42.5, points(l), 1e-9);
        assertEquals(1, attendues(suivi));
    }

    @Test
    @DisplayName("Résultat disqualifié : statut « disqualifié », compte quand même comme saisie")
    void detailler_disqualifie() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Vitesse", true, true);
        inscrit(10);
        resultat(1, 10, 0, true, valeurs());

        SuiviParticipant suivi = service.detailler(p);

        assertEquals(Statut.DISQUALIFIE, statut(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Épreuve inactive sans résultat : ignorée")
    void detailler_inactiveSansResultat_ignoree() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Ancienne", false, true);
        inscrit(10);

        SuiviParticipant suivi = service.detailler(p);

        assertTrue(lignes(suivi).isEmpty());
        assertEquals(0, attendues(suivi));
    }

    @Test
    @DisplayName("Épreuve inactive mais avec un résultat : conservée")
    void detailler_inactiveAvecResultat_conservee() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Ancienne", false, true);
        inscrit(10);
        resultat(1, 10, 15);

        SuiviParticipant suivi = service.detailler(p);

        assertEquals(1, lignes(suivi).size());
        assertEquals(Statut.SAISI, statut(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Épreuve active où le pilote n'est pas inscrit, sans résultat : ignorée")
    void detailler_nonInscritSansResultat_ignoree() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Vitesse", true, true);
        // pas d'inscription

        SuiviParticipant suivi = service.detailler(p);

        assertTrue(lignes(suivi).isEmpty());
        assertEquals(0, attendues(suivi));
    }

    @Test
    @DisplayName("Non inscrit mais avec un résultat : conservée")
    void detailler_nonInscritAvecResultat_conservee() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Vitesse", true, true);
        resultat(1, 10, 20);

        SuiviParticipant suivi = service.detailler(p);

        assertEquals(1, lignes(suivi).size());
        assertEquals(Statut.SAISI, statut(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Résultats des autres pilotes ignorés")
    void detailler_resultatsDAutresPilotesIgnores() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Vitesse", true, true);
        inscrit(10);
        resultat(2, 10, 99); // autre pilote

        SuiviParticipant suivi = service.detailler(p);

        assertEquals(Statut.PAS_ENCORE_SAISI, statut(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Les lignes suivent l'ordre des épreuves et transmettent « compte dans le général »")
    void detailler_ordreEtCompteDansGeneral() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Première", true, true);
        epreuve(20, "Deuxième", true, false);
        epreuve(30, "Troisième", true, true);
        inscrit(10, 20, 30);

        SuiviParticipant suivi = service.detailler(p);

        assertEquals(List.of("Première", "Deuxième", "Troisième"),
                lignes(suivi).stream().map(this::nomEpreuve).toList());
        assertEquals(List.of(true, false, true),
                lignes(suivi).stream().map(this::compteDansGeneral).toList());
        assertEquals(3, attendues(suivi));
    }

    @Test
    @DisplayName("Mélange : attendues = lignes affichées, saisies = lignes avec résultat")
    void detailler_comptesAttenduesEtSaisies() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "A", true, true);   // inscrit + résultat
        epreuve(20, "B", true, true);   // inscrit sans résultat
        epreuve(30, "C", true, true);   // non inscrit, ignorée
        epreuve(40, "D", false, true);  // inactive sans résultat, ignorée
        inscrit(10, 20);
        resultat(1, 10, 10);

        SuiviParticipant suivi = service.detailler(p);

        assertEquals(2, lignes(suivi).size());
        assertEquals(2, attendues(suivi));
    }

    // =====================================================================
    // detailler : total et rang
    // =====================================================================

    @Test
    @DisplayName("Total, rang et nombre de participants repris du classement général")
    void detailler_totalEtRang() throws SQLException {
        Pilote p1 = pilote(1, "Dupont", "Jean");
        Pilote p2 = pilote(2, "Martin", "Paul");
        Pilote p3 = pilote(3, "Durand", "Luc");
        ligneClassement(p2, 80, 1);
        ligneClassement(p1, 55.5, 2);
        ligneClassement(p3, 10, 3);

        SuiviParticipant suivi = service.detailler(p1);

        assertEquals(55.5, total(suivi), 1e-9);
        assertEquals(2, rang(suivi));
    }

    @Test
    @DisplayName("Pilote absent du classement : total 0 et rang 0")
    void detailler_piloteAbsentDuClassement() throws SQLException {
        Pilote p1 = pilote(1, "Dupont", "Jean");
        Pilote autre = pilote(2, "Martin", "Paul");
        ligneClassement(autre, 80, 1);

        SuiviParticipant suivi = service.detailler(p1);

        assertEquals(0.0, total(suivi), 1e-9);
        assertEquals(0, rang(suivi));
    }

    // =====================================================================
    // detailler : formatage des valeurs
    // =====================================================================

    @Test
    @DisplayName("Valeurs : libellé du paramètre + valeur entière sans décimale + unité")
    void valeurs_libelleEntierEtUnite() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Marche", true, true, new EpreuveParametre("temps", "Temps", "s"));
        inscrit(10);
        resultat(1, 10, 5, false, valeurs("temps", 12.0));

        SuiviParticipant suivi = service.detailler(p);

        assertEquals("Temps : 12 s", valeurs(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Valeurs : décimale conservée")
    void valeurs_decimale() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Marche", true, true, new EpreuveParametre("temps", "Temps", "s"));
        inscrit(10);
        resultat(1, 10, 5, false, valeurs("temps", 3.5));

        SuiviParticipant suivi = service.detailler(p);

        assertEquals("Temps : 3.5 s", valeurs(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Valeurs : plusieurs paramètres séparés par « ; » dans l'ordre")
    void valeurs_plusieursParametres() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Marche", true, true,
                new EpreuveParametre("temps", "Temps de marche", "min"),
                new EpreuveParametre("balises", "Balises", null));
        inscrit(10);
        resultat(1, 10, 5, false, valeurs("temps", 45, "balises", 8));

        SuiviParticipant suivi = service.detailler(p);

        assertEquals("Temps de marche : 45 min ; Balises : 8", valeurs(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Valeurs : libellé contenant déjà « ( » => l'unité n'est pas ajoutée")
    void valeurs_libelleAvecParenthese_pasDUnite() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Marche", true, true, new EpreuveParametre("temps", "Temps (s)", "s"));
        inscrit(10);
        resultat(1, 10, 5, false, valeurs("temps", 12));

        SuiviParticipant suivi = service.detailler(p);

        assertEquals("Temps (s) : 12", valeurs(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Valeurs : unité blanche ignorée")
    void valeurs_uniteBlanche() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Marche", true, true, new EpreuveParametre("balises", "Balises", "  "));
        inscrit(10);
        resultat(1, 10, 5, false, valeurs("balises", 8));

        SuiviParticipant suivi = service.detailler(p);

        assertEquals("Balises : 8", valeurs(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Valeurs : paramètre inconnu de l'épreuve => on affiche le nom de variable brut")
    void valeurs_parametreInconnu() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Marche", true, true); // aucun paramètre déclaré
        inscrit(10);
        resultat(1, 10, 5, false, valeurs("mystere", 7));

        SuiviParticipant suivi = service.detailler(p);

        assertEquals("mystere : 7", valeurs(lignes(suivi).get(0)));
    }

    @Test
    @DisplayName("Valeurs : résultat sans valeur => chaîne vide")
    void valeurs_aucuneValeur() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuve(10, "Marche", true, true);
        inscrit(10);
        resultat(1, 10, 5);

        SuiviParticipant suivi = service.detailler(p);

        assertEquals("", valeurs(lignes(suivi).get(0)));
    }

    // =====================================================================
    // erreurs
    // =====================================================================

    @Test
    @DisplayName("Erreur SQL d'un DAO : propagée")
    void detailler_erreurDao_propagee() throws SQLException {
        Pilote p = pilote(1, "Dupont", "Jean");
        epreuveDao.close();
        epreuveDao = mockConstruction(EpreuveDAO.class, (mock, ctx) ->
                when(mock.listerToutes()).thenThrow(new SQLException("DB indisponible")));
        SuiviParticipantService serviceEnErreur = new SuiviParticipantService();

        assertThrows(SQLException.class, () -> serviceEnErreur.detailler(p));
    }

    // =====================================================================
    // ACCESSEURS : à adapter si les noms de getters de SuiviParticipant /
    // LigneSuivi sont différents (ex. si ce sont des records : l.nom(), etc.)
    // =====================================================================

    private List<LigneSuivi> lignes(SuiviParticipant s)  { return s.lignes(); }
    private double total(SuiviParticipant s)             { return s.totalPoints(); }
    private int rang(SuiviParticipant s)                 { return s.rang(); }
    private int attendues(SuiviParticipant s)            { return s.epreuvesAttendues(); }

    private String nomEpreuve(LigneSuivi l)               { return l.epreuve(); }
    private String valeurs(LigneSuivi l)                  { return l.valeurs(); }
    private Double points(LigneSuivi l)                   { return l.points(); }
    private Statut statut(LigneSuivi l)                   { return l.statut(); }
    private boolean compteDansGeneral(LigneSuivi l)       { return l.compteGeneral(); }
}