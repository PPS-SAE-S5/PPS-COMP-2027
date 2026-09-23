package com.pps.parapente.controller;
import com.pps.parapente.util.MessageErreurUtil;

import com.pps.parapente.dao.UtilisateurDAO;
import com.pps.parapente.model.Role;
import com.pps.parapente.model.Utilisateur;
import com.pps.parapente.util.SceneManager;
import com.pps.parapente.util.SecuriteUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class GestionUtilisateursController {

    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, String> colIdentifiant;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colActif;

    @FXML private TextField champIdentifiant;
    @FXML private PasswordField champMotDePasse;
    @FXML private ComboBox<Role> champRole;
    @FXML private Label labelErreur;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final ObservableList<Utilisateur> donnees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colIdentifiant.setCellValueFactory(new PropertyValueFactory<>("identifiant"));
        colRole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRole().getLibelle()));
        colActif.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isActif() ? "Oui" : "Non"));
        tableUtilisateurs.setItems(donnees);

        champRole.setItems(FXCollections.observableArrayList(Role.values()));

        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            donnees.setAll(utilisateurDAO.listerTous());
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement : " + MessageErreurUtil.traduire(e));
        }
    }

    @FXML
    private void creerCompte() {
        String identifiant = champIdentifiant.getText().trim();
        String motDePasse = champMotDePasse.getText();
        Role role = champRole.getValue();

        if (identifiant.isEmpty() || motDePasse.isEmpty() || role == null) {
            afficherErreur("Identifiant, mot de passe et rôle sont obligatoires.");
            return;
        }
        try {
            Utilisateur u = new Utilisateur(identifiant, SecuriteUtil.hacher(motDePasse), role);
            utilisateurDAO.creer(u);
            chargerDonnees();
            champIdentifiant.clear();
            champMotDePasse.clear();
            champRole.setValue(null);
            masquerErreur();
        } catch (SQLException e) {
            afficherErreur("Erreur (identifiant déjà utilisé ?) : " + MessageErreurUtil.traduire(e));
        }
    }

    @FXML
    private void supprimerCompte() {
        Utilisateur selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherErreur("Sélectionnez un compte à supprimer.");
            return;
        }
        if ("admin".equals(selection.getIdentifiant())) {
            afficherErreur("Le compte administrateur par défaut ne peut pas être supprimé.");
            return;
        }
        try {
            utilisateurDAO.supprimer(selection.getId());
            chargerDonnees();
        } catch (SQLException e) {
            afficherErreur("Erreur de suppression : " + MessageErreurUtil.traduire(e));
        }
    }

    @FXML
    private void retour() {
        SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
    }

    private void afficherErreur(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }

    private void masquerErreur() {
        labelErreur.setVisible(false);
        labelErreur.setManaged(false);
    }
}
