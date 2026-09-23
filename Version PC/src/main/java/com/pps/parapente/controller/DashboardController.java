package com.pps.parapente.controller;

import com.pps.parapente.model.Role;
import com.pps.parapente.model.Utilisateur;
import com.pps.parapente.service.AuthService;
import com.pps.parapente.util.SceneManager;
import com.pps.parapente.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML private Label labelUtilisateur;
    @FXML private VBox carteEpreuves;
    @FXML private VBox cartePilotes;
    @FXML private VBox carteSaisie;
    @FXML private VBox carteClassement;
    @FXML private VBox carteUtilisateurs;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        Utilisateur u = SessionManager.getUtilisateurConnecte();
        if (u == null) {
            SceneManager.naviguerVers("login.fxml", "Connexion");
            return;
        }
        labelUtilisateur.setText(u.getIdentifiant() + " (" + u.getRole().getLibelle() + ")");
        appliquerDroitsAccesParRole(u.getRole());
    }

    /**
     * Affiche/masque les cartes du menu selon le rôle connecté.
     * - Administrateur : tout
     * - Responsable de l'épreuve : épreuves, pilotes (lecture), saisie, classement
     * - Bénévole : saisie des résultats, classement
     * - Pilote / Comité des pilotes : classement uniquement (consultation)
     */
    private void appliquerDroitsAccesParRole(Role role) {
        switch (role) {
            case ADMINISTRATEUR -> { /* tout visible par défaut */ }
            case RESPONSABLE_EPREUVE -> masquer(carteUtilisateurs);
            case BENEVOLE -> masquer(carteUtilisateurs, carteEpreuves);
            case PILOTE, COMITE_PILOTES -> masquer(carteUtilisateurs, carteEpreuves, carteSaisie, cartePilotes);
        }
    }

    private void masquer(VBox... cartes) {
        for (VBox c : cartes) {
            c.setVisible(false);
            c.setManaged(false);
        }
    }

    @FXML private void ouvrirGestionEpreuves() { SceneManager.naviguerVers("gestion_epreuves.fxml", "Gestion des épreuves"); }
    @FXML private void ouvrirGestionPilotes() { SceneManager.naviguerVers("gestion_pilotes.fxml", "Gestion des pilotes"); }
    @FXML private void ouvrirSaisieResultats() { SceneManager.naviguerVers("saisie_resultats.fxml", "Saisie des résultats"); }
    @FXML private void ouvrirClassement() { SceneManager.naviguerVers("classement.fxml", "Classement général"); }
    @FXML private void ouvrirGestionUtilisateurs() { SceneManager.naviguerVers("gestion_utilisateurs.fxml", "Gestion des comptes"); }

    @FXML
    private void seDeconnecter() {
        authService.seDeconnecter();
        SceneManager.naviguerVers("login.fxml", "Connexion");
    }
}
