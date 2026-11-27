package com.pps.parapente.util;

import com.pps.parapente.model.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests unitaires de SessionManager.
 *
 * L'état est statique : on le remet à zéro avant et après chaque test pour
 * éviter toute interférence entre tests (et avec les autres classes de test).
 *
 * Ce fichier doit être dans le package com.pps.parapente.util (côté test).
 */
class SessionManagerTest {

    @BeforeEach
    @AfterEach
    void reinitialiserSession() {
        SessionManager.deconnecter();
    }

    @Test
    @DisplayName("Au départ : personne n'est connecté")
    void etatInitial_nonConnecte() {
        assertFalse(SessionManager.estConnecte());
        assertNull(SessionManager.getUtilisateurConnecte());
    }

    @Test
    @DisplayName("connecter : l'utilisateur est mémorisé et la session est active")
    void connecter_memoriseLUtilisateur() {
        Utilisateur u = mock(Utilisateur.class);

        SessionManager.connecter(u);

        assertTrue(SessionManager.estConnecte());
        assertSame(u, SessionManager.getUtilisateurConnecte());
    }

    @Test
    @DisplayName("deconnecter : la session est vidée")
    void deconnecter_viderLaSession() {
        SessionManager.connecter(mock(Utilisateur.class));

        SessionManager.deconnecter();

        assertFalse(SessionManager.estConnecte());
        assertNull(SessionManager.getUtilisateurConnecte());
    }

    @Test
    @DisplayName("deconnecter sans être connecté : sans effet, sans exception")
    void deconnecter_sansSession() {
        assertDoesNotThrow(SessionManager::deconnecter);

        assertFalse(SessionManager.estConnecte());
    }

    @Test
    @DisplayName("Une nouvelle connexion remplace l'utilisateur précédent")
    void connecter_remplaceLUtilisateurPrecedent() {
        Utilisateur premier = mock(Utilisateur.class);
        Utilisateur second = mock(Utilisateur.class);

        SessionManager.connecter(premier);
        SessionManager.connecter(second);

        assertSame(second, SessionManager.getUtilisateurConnecte());
        assertNotSame(premier, SessionManager.getUtilisateurConnecte());
    }

    @Test
    @DisplayName("connecter(null) : équivaut à une déconnexion (estConnecte = faux)")
    void connecter_null_equivautADeconnexion() {
        SessionManager.connecter(mock(Utilisateur.class));

        SessionManager.connecter(null);

        assertFalse(SessionManager.estConnecte());
        assertNull(SessionManager.getUtilisateurConnecte());
    }

    @Test
    @DisplayName("Cycle complet : connexion, déconnexion, reconnexion")
    void cycleComplet() {
        Utilisateur u = mock(Utilisateur.class);

        SessionManager.connecter(u);
        assertTrue(SessionManager.estConnecte());

        SessionManager.deconnecter();
        assertFalse(SessionManager.estConnecte());

        SessionManager.connecter(u);
        assertTrue(SessionManager.estConnecte());
        assertSame(u, SessionManager.getUtilisateurConnecte());
    }
}