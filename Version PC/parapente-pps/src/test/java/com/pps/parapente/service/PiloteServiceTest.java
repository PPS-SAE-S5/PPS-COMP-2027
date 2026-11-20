package com.pps.parapente.service;

import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.dao.PiloteDAO;
import com.pps.parapente.model.Pilote;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de PiloteService.
 *
 * Les DAO sont créés en interne par le service : on les remplace par des mocks
 * via mockConstruction (Mockito 5, ou mockito-inline avant).
 *
 * Hypothèses : Pilote expose getId() (int), getNumeroLicence(), getNom(),
 * getPrenom() (String) et getNumeroPilote() (Integer, nullable).
 */
class PiloteServiceTest {

    private MockedConstruction<PiloteDAO> piloteDaoConstruction;
    private MockedConstruction<InscriptionDAO> inscriptionDaoConstruction;

    private PiloteService service;
    private PiloteDAO piloteDao;
    private InscriptionDAO inscriptionDao;

    @BeforeEach
    void setUp() {
        piloteDaoConstruction = mockConstruction(PiloteDAO.class);
        inscriptionDaoConstruction = mockConstruction(InscriptionDAO.class);
        service = new PiloteService();
        piloteDao = piloteDaoConstruction.constructed().get(0);
        inscriptionDao = inscriptionDaoConstruction.constructed().get(0);
    }

    @AfterEach
    void tearDown() {
        piloteDaoConstruction.close();
        inscriptionDaoConstruction.close();
    }

    // ---------- helper ----------

    private Pilote pilote(int id, String licence, String nom, String prenom, Integer numeroPilote) {
        Pilote p = mock(Pilote.class);
        when(p.getId()).thenReturn(id);
        when(p.getNumeroLicence()).thenReturn(licence);
        when(p.getNom()).thenReturn(nom);
        when(p.getPrenom()).thenReturn(prenom);
        when(p.getNumeroPilote()).thenReturn(numeroPilote);
        return p;
    }

    private Pilote piloteValide() {
        return pilote(42, "LIC-001", "Dupont", "Jean", 7);
    }

    private void assertRefuse(Pilote p, String fragmentMessage) throws SQLException {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.creer(p));
        assertTrue(ex.getMessage().toLowerCase().contains(fragmentMessage.toLowerCase()),
                "message inattendu : " + ex.getMessage());
        verify(piloteDao, never()).creer(any());
        verify(inscriptionDao, never()).inscrirePiloteAToutesLesEpreuvesActives(anyInt());
    }

    // ---------- listerTous ----------

    @Test
    @DisplayName("listerTous délègue au DAO")
    void listerTous_delegue() throws SQLException {
        List<Pilote> attendus = List.of(piloteValide());
        when(piloteDao.listerTous()).thenReturn(attendus);

        assertSame(attendus, service.listerTous());
    }

    // ---------- supprimer ----------

    @Test
    @DisplayName("supprimer délègue au DAO")
    void supprimer_delegue() throws SQLException {
        service.supprimer(5);

        verify(piloteDao).supprimer(5);
    }

    // ---------- creer : cas nominal ----------

    @Test
    @DisplayName("creer (valide) : enregistre puis inscrit le pilote aux épreuves actives")
    void creer_valide_creeEtInscrit() throws SQLException {
        Pilote p = piloteValide();

        service.creer(p);

        InOrder ordre = inOrder(piloteDao, inscriptionDao);
        ordre.verify(piloteDao).creer(p);
        ordre.verify(inscriptionDao).inscrirePiloteAToutesLesEpreuvesActives(42);
    }

    @Test
    @DisplayName("creer : numéro de pilote absent (null) accepté")
    void creer_numeroPiloteNull_accepte() throws SQLException {
        Pilote p = pilote(1, "LIC-001", "Dupont", "Jean", null);

        service.creer(p);

        verify(piloteDao).creer(p);
    }

    @Test
    @DisplayName("creer : numéro de pilote = 1 (borne basse valide) accepté")
    void creer_numeroPilote1_accepte() throws SQLException {
        Pilote p = pilote(1, "LIC-001", "Dupont", "Jean", 1);

        service.creer(p);

        verify(piloteDao).creer(p);
    }

    // ---------- creer : validations ----------

    @Test
    @DisplayName("creer : licence null refusée")
    void creer_licenceNull_refuse() throws SQLException {
        assertRefuse(pilote(1, null, "Dupont", "Jean", 7), "licence");
    }

    @Test
    @DisplayName("creer : licence vide/blanche refusée")
    void creer_licenceBlanche_refuse() throws SQLException {
        assertRefuse(pilote(1, "   ", "Dupont", "Jean", 7), "licence");
    }

    @Test
    @DisplayName("creer : nom null refusé")
    void creer_nomNull_refuse() throws SQLException {
        assertRefuse(pilote(1, "LIC-001", null, "Jean", 7), "nom");
    }

    @Test
    @DisplayName("creer : nom vide refusé")
    void creer_nomVide_refuse() throws SQLException {
        assertRefuse(pilote(1, "LIC-001", "", "Jean", 7), "nom");
    }

    @Test
    @DisplayName("creer : prénom null refusé")
    void creer_prenomNull_refuse() throws SQLException {
        assertRefuse(pilote(1, "LIC-001", "Dupont", null, 7), "prénom");
    }

    @Test
    @DisplayName("creer : prénom blanc refusé")
    void creer_prenomBlanc_refuse() throws SQLException {
        assertRefuse(pilote(1, "LIC-001", "Dupont", " \t", 7), "prénom");
    }

    @Test
    @DisplayName("creer : numéro de pilote 0 refusé")
    void creer_numeroPiloteZero_refuse() throws SQLException {
        assertRefuse(pilote(1, "LIC-001", "Dupont", "Jean", 0), "numéro de pilote");
    }

    @Test
    @DisplayName("creer : numéro de pilote négatif refusé")
    void creer_numeroPiloteNegatif_refuse() throws SQLException {
        assertRefuse(pilote(1, "LIC-001", "Dupont", "Jean", -3), "numéro de pilote");
    }

    // ---------- creer : erreurs DAO ----------

    @Test
    @DisplayName("creer : si l'enregistrement échoue, l'exception est propagée et aucune inscription n'est faite")
    void creer_erreurDaoPilote_pasDInscription() throws SQLException {
        Pilote p = piloteValide();
        doThrow(new SQLException("DB indisponible")).when(piloteDao).creer(p);

        assertThrows(SQLException.class, () -> service.creer(p));

        verify(inscriptionDao, never()).inscrirePiloteAToutesLesEpreuvesActives(anyInt());
    }

    @Test
    @DisplayName("creer : une erreur lors de l'inscription est propagée")
    void creer_erreurInscription_propagee() throws SQLException {
        Pilote p = piloteValide();
        doThrow(new SQLException("Échec inscription"))
                .when(inscriptionDao).inscrirePiloteAToutesLesEpreuvesActives(42);

        assertThrows(SQLException.class, () -> service.creer(p));
    }

    // ---------- mettreAJour ----------

    @Test
    @DisplayName("mettreAJour (valide) : délègue au DAO, sans nouvelle inscription")
    void mettreAJour_valide_delegue() throws SQLException {
        Pilote p = piloteValide();

        service.mettreAJour(p);

        verify(piloteDao).mettreAJour(p);
        verify(inscriptionDao, never()).inscrirePiloteAToutesLesEpreuvesActives(anyInt());
    }

    @Test
    @DisplayName("mettreAJour (invalide) : refuse sans toucher au DAO")
    void mettreAJour_invalide_refuse() throws SQLException {
        Pilote p = pilote(1, "LIC-001", "", "Jean", 7);

        assertThrows(IllegalArgumentException.class, () -> service.mettreAJour(p));

        verify(piloteDao, never()).mettreAJour(any());
    }

    @Test
    @DisplayName("mettreAJour : numéro de pilote négatif refusé")
    void mettreAJour_numeroNegatif_refuse() throws SQLException {
        Pilote p = pilote(1, "LIC-001", "Dupont", "Jean", -1);

        assertThrows(IllegalArgumentException.class, () -> service.mettreAJour(p));

        verify(piloteDao, never()).mettreAJour(any());
    }
}