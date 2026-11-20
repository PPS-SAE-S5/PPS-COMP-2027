package com.pps.parapente.service;

import com.pps.parapente.dao.PlanningDAO;
import com.pps.parapente.model.Atelier;
import com.pps.parapente.model.Creneau;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Journee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de PlanningService.
 *
 * - Les méthodes statiques (validation des champs, des horaires, messages)
 *   sont testées directement, sans mock.
 * - Le DAO est créé en interne par le service : on le remplace par un mock via
 *   mockConstruction.
 * - On utilise de vrais objets Journee et Atelier (pour vérifier le "trim"
 *   appliqué par le service).
 *
 * Hypothèses : Journee() et Atelier() ont un constructeur sans argument et les
 * getters/setters utilisés dans le service ; Atelier.getJourneeId() est un int ;
 * Creneau.MATIN existe.
 */
class PlanningServiceTest {

    private MockedConstruction<PlanningDAO> daoConstruction;
    private PlanningService service;
    private PlanningDAO dao;

    @BeforeEach
    void setUp() {
        daoConstruction = mockConstruction(PlanningDAO.class);
        service = new PlanningService();
        dao = daoConstruction.constructed().get(0);
    }

    @AfterEach
    void tearDown() {
        daoConstruction.close();
    }

    // ---------- helpers ----------

    private Atelier atelierValide() {
        Atelier a = new Atelier();
        a.setNom("Atelier vitesse");
        a.setJourneeId(1);
        a.setCreneau(Creneau.MATIN);
        a.setHeureDebut("08:00");
        a.setHeureFin("10:30");
        a.setLieu("Terrain A");
        a.setResponsable("M. Dupont");
        return a;
    }

    private Journee journee(String nom) {
        Journee j = new Journee();
        j.setNom(nom);
        return j;
    }

    // =====================================================================
    // heureValide
    // =====================================================================

    @Test
    @DisplayName("heureValide : formats HH:mm valides (bornes comprises)")
    void heureValide_formatsValides() {
        assertTrue(PlanningService.heureValide("00:00"));
        assertTrue(PlanningService.heureValide("08:00"));
        assertTrue(PlanningService.heureValide("12:30"));
        assertTrue(PlanningService.heureValide("19:59"));
        assertTrue(PlanningService.heureValide("23:59"));
    }

    @Test
    @DisplayName("heureValide : les espaces autour sont ignorés")
    void heureValide_espacesIgnores() {
        assertTrue(PlanningService.heureValide("  08:00 "));
    }

    @Test
    @DisplayName("heureValide : formats invalides refusés")
    void heureValide_formatsInvalides() {
        assertFalse(PlanningService.heureValide(null));
        assertFalse(PlanningService.heureValide(""));
        assertFalse(PlanningService.heureValide("   "));
        assertFalse(PlanningService.heureValide("8:00"));    // heure sur 1 chiffre
        assertFalse(PlanningService.heureValide("24:00"));   // heure hors plage
        assertFalse(PlanningService.heureValide("08:60"));   // minutes hors plage
        assertFalse(PlanningService.heureValide("0800"));    // pas de séparateur
        assertFalse(PlanningService.heureValide("08h00"));
        assertFalse(PlanningService.heureValide("ab:cd"));
        assertFalse(PlanningService.heureValide("08:00:00"));
    }

    // =====================================================================
    // erreursHoraires
    // =====================================================================

    @Test
    @DisplayName("erreursHoraires : horaires valides et cohérents => aucune erreur")
    void erreursHoraires_valides_aucuneErreur() {
        assertTrue(PlanningService.erreursHoraires("08:00", "10:30").isEmpty());
    }

    @Test
    @DisplayName("erreursHoraires : espaces autour des heures tolérés")
    void erreursHoraires_espacesToleres() {
        assertTrue(PlanningService.erreursHoraires(" 08:00 ", " 10:30 ").isEmpty());
    }

    @Test
    @DisplayName("erreursHoraires : heure de début invalide => 1 erreur citant la valeur")
    void erreursHoraires_debutInvalide() {
        List<String> erreurs = PlanningService.erreursHoraires("8h", "10:30");

        assertEquals(1, erreurs.size());
        assertTrue(erreurs.get(0).contains("début"));
        assertTrue(erreurs.get(0).contains("8h"));
    }

    @Test
    @DisplayName("erreursHoraires : heure de fin invalide => 1 erreur citant la valeur")
    void erreursHoraires_finInvalide() {
        List<String> erreurs = PlanningService.erreursHoraires("08:00", "25:00");

        assertEquals(1, erreurs.size());
        assertTrue(erreurs.get(0).contains("fin"));
        assertTrue(erreurs.get(0).contains("25:00"));
    }

    @Test
    @DisplayName("erreursHoraires : début et fin invalides => 2 erreurs, pas d'erreur d'ordre")
    void erreursHoraires_lesDeuxInvalides() {
        List<String> erreurs = PlanningService.erreursHoraires("xx", "yy");

        assertEquals(2, erreurs.size());
        assertFalse(erreurs.contains(PlanningService.MESSAGE_ORDRE_HORAIRES));
    }

    @Test
    @DisplayName("erreursHoraires : fin = début => erreur d'ordre")
    void erreursHoraires_finEgaleDebut() {
        assertEquals(List.of(PlanningService.MESSAGE_ORDRE_HORAIRES),
                PlanningService.erreursHoraires("09:00", "09:00"));
    }

    @Test
    @DisplayName("erreursHoraires : fin avant début => erreur d'ordre")
    void erreursHoraires_finAvantDebut() {
        assertEquals(List.of(PlanningService.MESSAGE_ORDRE_HORAIRES),
                PlanningService.erreursHoraires("14:00", "09:30"));
    }

    @Test
    @DisplayName("erreursHoraires : une minute d'écart suffit")
    void erreursHoraires_uneMinuteDEcart() {
        assertTrue(PlanningService.erreursHoraires("09:00", "09:01").isEmpty());
    }

    @Test
    @Disabled("Bug connu : erreursHoraires(null, ...) ou (..., null) lève une NullPointerException "
            + "(heureDebut.trim() dans le message d'erreur). À réactiver une fois corrigé.")
    @DisplayName("erreursHoraires : valeur null => erreur de format, sans exception")
    void erreursHoraires_nullNeLevePasDException() {
        List<String> erreurs = assertDoesNotThrow(() -> PlanningService.erreursHoraires(null, "10:30"));

        assertEquals(1, erreurs.size());
    }

    // =====================================================================
    // champsObligatoiresManquants
    // =====================================================================

    @Test
    @DisplayName("champsObligatoiresManquants : atelier complet => liste vide")
    void champs_atelierComplet_vide() {
        assertTrue(PlanningService.champsObligatoiresManquants(atelierValide()).isEmpty());
    }

    @Test
    @DisplayName("champsObligatoiresManquants : atelier vide => les 7 champs, dans l'ordre du formulaire")
    void champs_atelierVide_tousLesChamps() {
        List<String> manquants = PlanningService.champsObligatoiresManquants(new Atelier());

        assertEquals(List.of(
                PlanningService.CHAMP_NOM,
                PlanningService.CHAMP_JOURNEE,
                PlanningService.CHAMP_CRENEAU,
                PlanningService.CHAMP_DEBUT,
                PlanningService.CHAMP_FIN,
                PlanningService.CHAMP_LIEU,
                PlanningService.CHAMP_RESPONSABLE), manquants);
    }

    @Test
    @DisplayName("champsObligatoiresManquants : nom blanc détecté")
    void champs_nomBlanc() {
        Atelier a = atelierValide();
        a.setNom("   ");

        assertEquals(List.of(PlanningService.CHAMP_NOM), PlanningService.champsObligatoiresManquants(a));
    }

    @Test
    @DisplayName("champsObligatoiresManquants : journée à 0 ou négative détectée")
    void champs_journeeInvalide() {
        Atelier a = atelierValide();
        a.setJourneeId(0);
        assertEquals(List.of(PlanningService.CHAMP_JOURNEE), PlanningService.champsObligatoiresManquants(a));

        a.setJourneeId(-1);
        assertEquals(List.of(PlanningService.CHAMP_JOURNEE), PlanningService.champsObligatoiresManquants(a));
    }

    @Test
    @DisplayName("champsObligatoiresManquants : créneau null détecté")
    void champs_creneauNull() {
        Atelier a = atelierValide();
        a.setCreneau(null);

        assertEquals(List.of(PlanningService.CHAMP_CRENEAU), PlanningService.champsObligatoiresManquants(a));
    }

    @Test
    @DisplayName("champsObligatoiresManquants : horaires, lieu et responsable manquants détectés")
    void champs_horairesLieuResponsable() {
        Atelier a = atelierValide();
        a.setHeureDebut(null);
        a.setHeureFin("");
        a.setLieu(" ");
        a.setResponsable(null);

        assertEquals(List.of(
                PlanningService.CHAMP_DEBUT,
                PlanningService.CHAMP_FIN,
                PlanningService.CHAMP_LIEU,
                PlanningService.CHAMP_RESPONSABLE), PlanningService.champsObligatoiresManquants(a));
    }

    // =====================================================================
    // messageChampsManquants
    // =====================================================================

    @Test
    @DisplayName("messageChampsManquants : un seul champ => phrase simple")
    void message_unSeulChamp() {
        assertEquals("L'atelier ne peut pas être enregistré : le champ « lieu » est obligatoire.",
                PlanningService.messageChampsManquants(List.of("lieu")));
    }

    @Test
    @DisplayName("messageChampsManquants : plusieurs champs => liste à puces")
    void message_plusieursChamps() {
        String message = PlanningService.messageChampsManquants(List.of("lieu", "responsable"));

        assertEquals("L'atelier ne peut pas être enregistré, champs obligatoires manquants :"
                + "\n  • lieu\n  • responsable", message);
    }

    // =====================================================================
    // Journées
    // =====================================================================

    @Test
    @DisplayName("creerJournee : valide => nom nettoyé (trim) puis délégation au DAO")
    void creerJournee_valide() throws SQLException {
        Journee j = journee("  Finale  ");

        service.creerJournee(j);

        assertEquals("Finale", j.getNom());
        verify(dao).creerJournee(j);
    }

    @Test
    @DisplayName("creerJournee : nom null ou blanc => IllegalArgumentException, DAO non appelé")
    void creerJournee_nomInvalide() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> service.creerJournee(journee(null)));
        assertThrows(IllegalArgumentException.class, () -> service.creerJournee(journee("   ")));

        verify(dao, never()).creerJournee(any());
    }

    @Test
    @DisplayName("mettreAJourJournee : valide => trim puis délégation")
    void mettreAJourJournee_valide() throws SQLException {
        Journee j = journee(" Qualifs ");

        service.mettreAJourJournee(j);

        assertEquals("Qualifs", j.getNom());
        verify(dao).mettreAJourJournee(j);
    }

    @Test
    @DisplayName("mettreAJourJournee : nom blanc => refusé, DAO non appelé")
    void mettreAJourJournee_nomBlanc() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> service.mettreAJourJournee(journee("")));

        verify(dao, never()).mettreAJourJournee(any());
    }

    @Test
    @DisplayName("creerJournee : erreur SQL propagée")
    void creerJournee_erreurSql() throws SQLException {
        Journee j = journee("Finale");
        doThrow(new SQLException("DB indisponible")).when(dao).creerJournee(j);

        assertThrows(SQLException.class, () -> service.creerJournee(j));
    }

    // =====================================================================
    // Ateliers
    // =====================================================================

    @Test
    @DisplayName("creerAtelier : valide => champs nettoyés (trim) puis délégation au DAO")
    void creerAtelier_valide_trimEtDelegue() throws SQLException {
        Atelier a = atelierValide();
        a.setNom("  Atelier vitesse ");
        a.setHeureDebut(" 08:00 ");
        a.setHeureFin(" 10:30 ");
        a.setLieu(" Terrain A ");
        a.setResponsable(" M. Dupont ");

        service.creerAtelier(a);

        assertEquals("Atelier vitesse", a.getNom());
        assertEquals("08:00", a.getHeureDebut());
        assertEquals("10:30", a.getHeureFin());
        assertEquals("Terrain A", a.getLieu());
        assertEquals("M. Dupont", a.getResponsable());
        verify(dao).creerAtelier(a);
    }

    @Test
    @DisplayName("creerAtelier : champ manquant => message dédié, DAO non appelé")
    void creerAtelier_champManquant() throws SQLException {
        Atelier a = atelierValide();
        a.setLieu("");

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.creerAtelier(a));

        assertEquals(PlanningService.messageChampsManquants(List.of(PlanningService.CHAMP_LIEU)),
                ex.getMessage());
        verify(dao, never()).creerAtelier(any());
    }

    @Test
    @DisplayName("creerAtelier : plusieurs champs manquants => tous listés dans le message")
    void creerAtelier_plusieursChampsManquants() throws SQLException {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.creerAtelier(new Atelier()));

        assertTrue(ex.getMessage().contains(PlanningService.CHAMP_NOM));
        assertTrue(ex.getMessage().contains(PlanningService.CHAMP_RESPONSABLE));
        verify(dao, never()).creerAtelier(any());
    }

    @Test
    @DisplayName("creerAtelier : format d'heure invalide => message d'erreur horaire")
    void creerAtelier_heureInvalide() throws SQLException {
        Atelier a = atelierValide();
        a.setHeureDebut("8h");

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.creerAtelier(a));

        assertTrue(ex.getMessage().contains("8h"));
        verify(dao, never()).creerAtelier(any());
    }

    @Test
    @DisplayName("creerAtelier : fin avant début => message d'ordre des horaires")
    void creerAtelier_ordreHoraires() throws SQLException {
        Atelier a = atelierValide();
        a.setHeureDebut("14:00");
        a.setHeureFin("09:00");

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.creerAtelier(a));

        assertEquals(PlanningService.MESSAGE_ORDRE_HORAIRES, ex.getMessage());
        verify(dao, never()).creerAtelier(any());
    }

    @Test
    @DisplayName("creerAtelier : deux heures invalides => 2 erreurs séparées par un saut de ligne")
    void creerAtelier_deuxHeuresInvalides() {
        Atelier a = atelierValide();
        a.setHeureDebut("xx");
        a.setHeureFin("yy");

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.creerAtelier(a));

        assertEquals(2, ex.getMessage().split("\n").length);
    }

    @Test
    @DisplayName("mettreAJourAtelier : valide => trim puis délégation")
    void mettreAJourAtelier_valide() throws SQLException {
        Atelier a = atelierValide();
        a.setNom("  Précision ");

        service.mettreAJourAtelier(a);

        assertEquals("Précision", a.getNom());
        verify(dao).mettreAJourAtelier(a);
    }

    @Test
    @DisplayName("mettreAJourAtelier : invalide => refusé, DAO non appelé")
    void mettreAJourAtelier_invalide() throws SQLException {
        Atelier a = atelierValide();
        a.setCreneau(null);

        assertThrows(IllegalArgumentException.class, () -> service.mettreAJourAtelier(a));

        verify(dao, never()).mettreAJourAtelier(any());
    }

    @Test
    @DisplayName("validerAtelier : atelier valide => pas d'exception")
    void validerAtelier_valide() {
        assertDoesNotThrow(() -> service.validerAtelier(atelierValide()));
    }

    // =====================================================================
    // Délégations simples
    // =====================================================================

    @Test
    @DisplayName("Listes et compteurs : délégués au DAO")
    void delegations_listesEtCompteurs() throws SQLException {
        List<Journee> journees = List.of(journee("J1"));
        List<Atelier> ateliers = List.of(atelierValide());
        List<Epreuve> nonAffectees = List.of();
        List<Epreuve> disponibles = List.of();
        when(dao.listerJournees()).thenReturn(journees);
        when(dao.listerAteliers(3)).thenReturn(ateliers);
        when(dao.listerEpreuvesNonAffectees()).thenReturn(nonAffectees);
        when(dao.listerEpreuvesDisponibles(4)).thenReturn(disponibles);
        when(dao.compterEpreuvesActives()).thenReturn(12);
        when(dao.compterAteliers(3)).thenReturn(5);

        assertSame(journees, service.listerJournees());
        assertSame(ateliers, service.listerAteliers(3));
        assertSame(nonAffectees, service.listerEpreuvesNonAffectees());
        assertSame(disponibles, service.listerEpreuvesDisponibles(4));
        assertEquals(12, service.compterEpreuvesActives());
        assertEquals(5, service.compterAteliers(3));
    }

    @Test
    @DisplayName("supprimerJournee et supprimerAtelier : délégués au DAO")
    void delegations_suppressions() throws SQLException {
        service.supprimerJournee(8);
        service.supprimerAtelier(9);

        verify(dao).supprimerJournee(8);
        verify(dao).supprimerAtelier(9);
    }
}