package com.pps.parapente.service;

import com.pps.parapente.dao.EpreuveDAO;
import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.model.BaremePoint;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.EpreuveParametre;
import com.pps.parapente.model.ModeCalcul;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de EpreuveService.
 *
 * Les DAO sont créés en interne par le service : on les remplace par des mocks
 * via mockConstruction. Les objets modèle (Epreuve, EpreuveParametre,
 * BaremePoint) sont de vrais objets.
 *
 * Hypothèses : Epreuve a un constructeur sans argument et les getters/setters
 * utilisés dans le service ; EpreuveParametre(nomVariable, label, unite) ;
 * BaremePoint(rang, points).
 */
class EpreuveServiceTest {

    private MockedConstruction<EpreuveDAO> epreuveDaoConstruction;
    private MockedConstruction<InscriptionDAO> inscriptionDaoConstruction;

    private EpreuveService service;
    private EpreuveDAO epreuveDao;
    private InscriptionDAO inscriptionDao;

    @BeforeEach
    void setUp() {
        epreuveDaoConstruction = mockConstruction(EpreuveDAO.class);
        inscriptionDaoConstruction = mockConstruction(InscriptionDAO.class);
        service = new EpreuveService();
        epreuveDao = epreuveDaoConstruction.constructed().get(0);
        inscriptionDao = inscriptionDaoConstruction.constructed().get(0);
    }

    @AfterEach
    void tearDown() {
        epreuveDaoConstruction.close();
        inscriptionDaoConstruction.close();
    }

    // ---------- helpers ----------

    private Epreuve epreuveFormuleValide() {
        Epreuve e = new Epreuve();
        e.setNom("Vitesse");
        e.setModeCalcul(ModeCalcul.FORMULE);
        e.setFormule("x * 2");
        return e;
    }

    private Epreuve epreuveBaremeValide() {
        Epreuve e = new Epreuve();
        e.setNom("Précision");
        e.setModeCalcul(ModeCalcul.BAREME);
        e.setValeurCle("temps");
        e.setBareme(new ArrayList<>(List.of(new BaremePoint(1, 10), new BaremePoint(2, 8))));
        return e;
    }

    private Epreuve existante(String nom, int ordre) {
        Epreuve e = new Epreuve();
        e.setNom(nom);
        e.setOrdre(ordre);
        return e;
    }

    // ---------- listerToutes ----------

    @Test
    @DisplayName("listerToutes délègue au DAO")
    void listerToutes_delegue() throws SQLException {
        List<Epreuve> attendues = List.of(existante("A", 1), existante("B", 2));
        when(epreuveDao.listerToutes()).thenReturn(attendues);

        assertSame(attendues, service.listerToutes());
    }

    // ---------- supprimer ----------

    @Test
    @DisplayName("supprimer délègue au DAO")
    void supprimer_delegue() throws SQLException {
        service.supprimer(7);

        verify(epreuveDao).supprimer(7);
    }

    // ---------- creer ----------

    @Test
    @DisplayName("creer (mode Formule valide) : enregistre puis inscrit tous les pilotes")
    void creer_formuleValide_creeEtInscrit() throws SQLException {
        Epreuve e = epreuveFormuleValide();

        service.creer(e);

        verify(epreuveDao).creer(e);
        verify(inscriptionDao).inscrireTousLesPilotes(e.getId());
    }

    @Test
    @DisplayName("creer (mode Barème valide) : enregistre puis inscrit tous les pilotes")
    void creer_baremeValide_creeEtInscrit() throws SQLException {
        Epreuve e = epreuveBaremeValide();

        service.creer(e);

        verify(epreuveDao).creer(e);
        verify(inscriptionDao).inscrireTousLesPilotes(e.getId());
    }

    @Test
    @DisplayName("creer : nom null => IllegalArgumentException, rien n'est enregistré")
    void creer_nomNull_refuse() throws SQLException {
        Epreuve e = epreuveFormuleValide();
        e.setNom(null);

        assertThrows(IllegalArgumentException.class, () -> service.creer(e));

        verify(epreuveDao, never()).creer(any());
        verify(inscriptionDao, never()).inscrireTousLesPilotes(anyInt());
    }

    @Test
    @DisplayName("creer : nom vide ou blanc => IllegalArgumentException")
    void creer_nomBlanc_refuse() throws SQLException {
        Epreuve e = epreuveFormuleValide();
        e.setNom("   ");

        assertThrows(IllegalArgumentException.class, () -> service.creer(e));

        verify(epreuveDao, never()).creer(any());
    }

    @Test
    @DisplayName("creer : mode Formule sans formule => IllegalArgumentException")
    void creer_formuleManquante_refuse() throws SQLException {
        Epreuve e = epreuveFormuleValide();
        e.setFormule(" ");

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.creer(e));

        assertTrue(ex.getMessage().toLowerCase().contains("formule"));
        verify(epreuveDao, never()).creer(any());
    }

    @Test
    @DisplayName("creer : mode Barème sans valeur clé => IllegalArgumentException")
    void creer_valeurCleManquante_refuse() throws SQLException {
        Epreuve e = epreuveBaremeValide();
        e.setValeurCle(null);

        assertThrows(IllegalArgumentException.class, () -> service.creer(e));

        verify(epreuveDao, never()).creer(any());
    }

    @Test
    @DisplayName("creer : mode Barème sans barème => IllegalArgumentException")
    void creer_baremeVide_refuse() throws SQLException {
        Epreuve e = epreuveBaremeValide();
        e.setBareme(new ArrayList<>());

        assertThrows(IllegalArgumentException.class, () -> service.creer(e));

        verify(epreuveDao, never()).creer(any());
    }

    // ---------- mettreAJour ----------

    @Test
    @DisplayName("mettreAJour (valide) : délègue au DAO")
    void mettreAJour_valide_delegue() throws SQLException {
        Epreuve e = epreuveFormuleValide();

        service.mettreAJour(e);

        verify(epreuveDao).mettreAJour(e);
        verify(inscriptionDao, never()).inscrireTousLesPilotes(anyInt());
    }

    @Test
    @DisplayName("mettreAJour (invalide) : refuse sans toucher au DAO")
    void mettreAJour_invalide_refuse() throws SQLException {
        Epreuve e = epreuveFormuleValide();
        e.setNom("");

        assertThrows(IllegalArgumentException.class, () -> service.mettreAJour(e));

        verify(epreuveDao, never()).mettreAJour(any());
    }

    // ---------- dupliquer ----------

    @Test
    @DisplayName("dupliquer : épreuve introuvable => IllegalStateException")
    void dupliquer_introuvable_exception() throws SQLException {
        when(epreuveDao.trouverParId(99)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.dupliquer(99));

        verify(epreuveDao, never()).creer(any());
    }

    @Test
    @DisplayName("dupliquer : copie les champs, ajoute « (copie) » et passe par creer()")
    void dupliquer_copieLesChamps() throws SQLException {
        Epreuve original = epreuveBaremeValide();
        original.setDescription("Atterrissage précis");
        original.setAfficherClassement(true);
        original.setCompteDansGeneral(true);
        original.setActif(true);
        original.setOrdre(2);
        original.setParametres(new ArrayList<>());

        when(epreuveDao.trouverParId(1)).thenReturn(Optional.of(original));
        when(epreuveDao.listerToutes()).thenReturn(List.of(original));

        Epreuve copie = service.dupliquer(1);

        assertNotSame(original, copie);
        assertEquals("Précision (copie)", copie.getNom());
        assertEquals(original.getDescription(), copie.getDescription());
        assertEquals(original.getModeCalcul(), copie.getModeCalcul());
        assertEquals(original.getFormule(), copie.getFormule());
        assertEquals(original.getValeurCle(), copie.getValeurCle());
        assertEquals(original.getSensClassement(), copie.getSensClassement());
        assertEquals(original.isAfficherClassement(), copie.isAfficherClassement());
        assertEquals(original.isCompteDansGeneral(), copie.isCompteDansGeneral());
        assertEquals(original.isActif(), copie.isActif());

        // Même traitement qu'une création : enregistrement + inscription des pilotes
        ArgumentCaptor<Epreuve> captor = ArgumentCaptor.forClass(Epreuve.class);
        verify(epreuveDao).creer(captor.capture());
        assertSame(copie, captor.getValue());
        verify(inscriptionDao).inscrireTousLesPilotes(copie.getId());
    }

    @Test
    @DisplayName("dupliquer : l'ordre de la copie = ordre max existant + 1")
    void dupliquer_ordreMaxPlusUn() throws SQLException {
        Epreuve original = epreuveBaremeValide();
        original.setOrdre(1);
        original.setParametres(new ArrayList<>());

        when(epreuveDao.trouverParId(1)).thenReturn(Optional.of(original));
        when(epreuveDao.listerToutes()).thenReturn(
                List.of(original, existante("Autre", 5), existante("Encore", 3)));

        Epreuve copie = service.dupliquer(1);

        assertEquals(6, copie.getOrdre());
    }

    @Test
    @DisplayName("dupliquer : paramètres et barème sont copiés en profondeur (nouvelles instances)")
    void dupliquer_copieProfondeParametresEtBareme() throws SQLException {
        Epreuve original = epreuveBaremeValide();
        EpreuveParametre p = new EpreuveParametre("temps", "Temps", "s");
        p.setObligatoire(true);
        p.setOrdre(3);
        original.setParametres(new ArrayList<>(List.of(p)));

        when(epreuveDao.trouverParId(1)).thenReturn(Optional.of(original));
        when(epreuveDao.listerToutes()).thenReturn(List.of(original));

        Epreuve copie = service.dupliquer(1);

        assertEquals(1, copie.getParametres().size());
        EpreuveParametre pc = copie.getParametres().get(0);
        assertNotSame(p, pc);
        assertEquals("temps", pc.getNomVariable());
        assertEquals("Temps", pc.getLabel());
        assertEquals("s", pc.getUnite());
        assertTrue(pc.isObligatoire());
        assertEquals(3, pc.getOrdre());

        assertEquals(2, copie.getBareme().size());
        for (int i = 0; i < 2; i++) {
            BaremePoint bo = original.getBareme().get(i);
            BaremePoint bc = copie.getBareme().get(i);
            assertNotSame(bo, bc);
            assertEquals(bo.getRang(), bc.getRang());
            assertEquals(bo.getPoints(), bc.getPoints(), 1e-9);
        }
    }

    @Test
    @DisplayName("dupliquer : nom déjà pris => « (copie 2) », puis « (copie 3) »")
    void dupliquer_nomDejaPris_incremente() throws SQLException {
        Epreuve original = epreuveBaremeValide(); // "Précision"
        original.setParametres(new ArrayList<>());

        when(epreuveDao.trouverParId(1)).thenReturn(Optional.of(original));
        when(epreuveDao.listerToutes()).thenReturn(List.of(
                original,
                existante("Précision (copie)", 2)));

        assertEquals("Précision (copie 2)", service.dupliquer(1).getNom());

        when(epreuveDao.listerToutes()).thenReturn(List.of(
                original,
                existante("Précision (copie)", 2),
                existante("Précision (copie 2)", 3)));

        assertEquals("Précision (copie 3)", service.dupliquer(1).getNom());
    }

    @Test
    @DisplayName("dupliquer : la comparaison des noms ignore la casse")
    void dupliquer_nomDejaPris_ignoreCasse() throws SQLException {
        Epreuve original = epreuveBaremeValide();
        original.setParametres(new ArrayList<>());

        when(epreuveDao.trouverParId(1)).thenReturn(Optional.of(original));
        when(epreuveDao.listerToutes()).thenReturn(List.of(
                original,
                existante("PRÉCISION (COPIE)", 2)));

        assertEquals("Précision (copie 2)", service.dupliquer(1).getNom());
    }

    @Test
    @DisplayName("dupliquer : erreur SQL du DAO propagée")
    void dupliquer_erreurDao_propageSQLException() throws SQLException {
        when(epreuveDao.trouverParId(1)).thenThrow(new SQLException("DB indisponible"));

        assertThrows(SQLException.class, () -> service.dupliquer(1));
    }
}