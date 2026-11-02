package com.pps.parapente.controller;
import com.pps.parapente.util.MessageErreurUtil;

import com.pps.parapente.model.Role;
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
                ouvrirPageAccueil(utilisateur.get());
            } else {
                afficherErreur("Identifiant ou mot de passe incorrect.");
            }
        } catch (SQLException e) {
            afficherErreur("Erreur d'accès à la base locale : " + MessageErreurUtil.traduire(e));
        }
    }

    @FXML
    private void etreInvite() {

        try {
            Optional<Utilisateur> utilisateur = authService.seConnecter("invité", "invitationPPS");
            if (utilisateur.isPresent()) {
                ouvrirPageAccueil(utilisateur.get());
            } else {
                afficherErreur("Le compte invité a un problème.");
            }
        } catch (SQLException e) {
            afficherErreur("Erreur d'accès à la base locale : " + MessageErreurUtil.traduire(e));
        }
    }

    /** Le pilote arrive directement sur le classement ; les autres rôles sur le tableau de bord. */
    private void ouvrirPageAccueil(Utilisateur utilisateur) {
        if (utilisateur.getRole() == Role.PILOTE) {
            SceneManager.naviguerVers("classement.fxml", "Classement général");
        } else {
            SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
        }
    }

    private void afficherErreur(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }
}
