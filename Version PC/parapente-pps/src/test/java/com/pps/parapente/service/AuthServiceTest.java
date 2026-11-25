package com.pps.parapente.service;

import com.pps.parapente.dao.UtilisateurDAO;
import com.pps.parapente.model.Utilisateur;
import com.pps.parapente.util.SecuriteUtil;
import com.pps.parapente.util.SessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de AuthService.
 *
 * Prérequis (pom.xml) : junit-jupiter + mockito-core 5.x (mockito-inline
 * est le comportement par défaut depuis Mockito 5, nécessaire pour
 * mockConstruction et mockStatic).
 *
 * AuthService crée son UtilisateurDAO en interne ("new UtilisateurDAO()")
 * et utilise des classes statiques (SecuriteUtil, SessionManager) :
 * on les remplace donc via mockConstruction / mockStatic.
 */
class AuthServiceTest {

    private static final String IDENTIFIANT = "alice";
    private static final String MDP_CLAIR = "secret123";
    private static final String MDP_HACHE = "hash-de-secret123";

    private Utilisateur utilisateurAvecHash(String hache) {
        Utilisateur u = mock(Utilisateur.class);
        when(u.getMotDePasseHache()).thenReturn(hache);
        return u;
    }

    // ---------- connexionReussie ----------
    @Test
    @DisplayName("Connexion réussie : retourne l'utilisateur et ouvre la session")
    void seConnecter_identifiantsValides_retourneUtilisateurEtConnecte() throws SQLException {
        Utilisateur utilisateur = utilisateurAvecHash(MDP_HACHE);

        try (MockedConstruction<UtilisateurDAO> dao = mockConstruction(UtilisateurDAO.class,
                     (mock, ctx) -> when(mock.trouverParIdentifiant(IDENTIFIANT))
                             .thenReturn(Optional.of(utilisateur)));
             MockedStatic<SecuriteUtil> securite = mockStatic(SecuriteUtil.class);
             MockedStatic<SessionManager> session = mockStatic(SessionManager.class)) {

            securite.when(() -> SecuriteUtil.hacher(MDP_CLAIR)).thenReturn(MDP_HACHE);

            Optional<Utilisateur> resultat = new AuthService().seConnecter(IDENTIFIANT, MDP_CLAIR);

            assertTrue(resultat.isPresent());
            assertSame(utilisateur, resultat.get());
            session.verify(() -> SessionManager.connecter(utilisateur), times(1));
        }
    }

    // ---------- mauvaisIdentifiant ---------- 

    @Test
    @DisplayName("Identifiant inconnu : Optional vide, pas de hachage, pas de session")
    void seConnecter_identifiantInconnu_retourneVide() throws SQLException {
        try (MockedConstruction<UtilisateurDAO> dao = mockConstruction(UtilisateurDAO.class,
                     (mock, ctx) -> when(mock.trouverParIdentifiant(anyString()))
                             .thenReturn(Optional.empty()));
             MockedStatic<SecuriteUtil> securite = mockStatic(SecuriteUtil.class);
             MockedStatic<SessionManager> session = mockStatic(SessionManager.class)) {

            Optional<Utilisateur> resultat = new AuthService().seConnecter("inconnu", MDP_CLAIR);

            assertTrue(resultat.isEmpty());
            securite.verifyNoInteractions();
            session.verifyNoInteractions();
        }
    }

    // ---------- mauvaisMotDePasse ----------

    @Test
    @DisplayName("Mot de passe incorrect : Optional vide, pas de session")
    void seConnecter_mauvaisMotDePasse_retourneVide() throws SQLException {
        Utilisateur utilisateur = utilisateurAvecHash(MDP_HACHE);

        try (MockedConstruction<UtilisateurDAO> dao = mockConstruction(UtilisateurDAO.class,
                     (mock, ctx) -> when(mock.trouverParIdentifiant(IDENTIFIANT))
                             .thenReturn(Optional.of(utilisateur)));
             MockedStatic<SecuriteUtil> securite = mockStatic(SecuriteUtil.class);
             MockedStatic<SessionManager> session = mockStatic(SessionManager.class)) {

            securite.when(() -> SecuriteUtil.hacher("mauvais")).thenReturn("hash-different");

            Optional<Utilisateur> resultat = new AuthService().seConnecter(IDENTIFIANT, "mauvais");

            assertTrue(resultat.isEmpty());
            session.verify(() -> SessionManager.connecter(any()), never());
        }
    }

    // ---------- erreurSQL ----------

    @Test
    @DisplayName("Erreur SQL du DAO : l'exception est propagée, pas de session")
    void seConnecter_erreurDao_propageSQLException() throws SQLException {
        try (MockedConstruction<UtilisateurDAO> dao = mockConstruction(UtilisateurDAO.class,
                     (mock, ctx) -> when(mock.trouverParIdentifiant(anyString()))
                             .thenThrow(new SQLException("DB indisponible")));
             MockedStatic<SessionManager> session = mockStatic(SessionManager.class)) {

            AuthService service = new AuthService();

            assertThrows(SQLException.class, () -> service.seConnecter(IDENTIFIANT, MDP_CLAIR));
            session.verifyNoInteractions();
        }
    }

    // ---------- seDeconnecter ----------

    @Test
    @DisplayName("Déconnexion : délègue à SessionManager.deconnecter()")
    void seDeconnecter_appelleSessionManager() {
        try (MockedConstruction<UtilisateurDAO> dao = mockConstruction(UtilisateurDAO.class);
             MockedStatic<SessionManager> session = mockStatic(SessionManager.class)) {

            new AuthService().seDeconnecter();

            session.verify(SessionManager::deconnecter, times(1));
        }
    }

    private static String anyString() {
        return org.mockito.ArgumentMatchers.anyString();
    }
}