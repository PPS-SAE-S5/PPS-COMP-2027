package com.pps.parapente.controller;

import com.pps.parapente.model.Utilisateur;
import com.pps.parapente.service.AuthService;
import com.pps.parapente.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField champIdentifiant;
    @FXML private PasswordField champMotDePasse;
    @FXML private Label labelErreur;

    private final AuthService authService = new AuthService();

    @FXML
    private void seConnecter() {
        String identifiant = champIdentifiant.getText().trim();
        String motDePasse = champMotDePasse.getText();

        if (identifiant.isEmpty() || motDePasse.isEmpty()) {
            afficherErreur("Veuillez saisir votre identifiant et votre mot de passe.");
            return;
        }

        try {
            Optional<Utilisateur> utilisateur = authService.seConnecter(identifiant, motDePasse);
            if (utilisateur.isPresent()) {
                SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
            } else {
                afficherErreur("Identifiant ou mot de passe incorrect.");
            }
        } catch (SQLException e) {
            afficherErreur("Erreur d'accès à la base locale : " + e.getMessage());
        }
    }

    private void afficherErreur(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }
}
