package com.pps.parapente.controller;
import com.pps.parapente.util.MessageErreurUtil;

import com.pps.parapente.model.Pilote;
import com.pps.parapente.service.PiloteService;
import com.pps.parapente.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class GestionPilotesController {

    @FXML private TableView<Pilote> tablePilotes;
    @FXML private TableColumn<Pilote, String> colLicence;
    @FXML private TableColumn<Pilote, String> colNom;
    @FXML private TableColumn<Pilote, String> colPrenom;
    @FXML private TableColumn<Pilote, String> colCaserne;
    @FXML private TableColumn<Pilote, Double> colPoids;
    @FXML private TableColumn<Pilote, String> colEmail;
    @FXML private TableColumn<Pilote, Integer> colAnnee;
    @FXML private TableColumn<Pilote, String> colCategorie;

    @FXML private TextField champLicence;
    @FXML private TextField champNom;
    @FXML private TextField champPrenom;
    @FXML private TextField champCaserne;
    @FXML private TextField champPoids;
    @FXML private TextField champEmail;
    @FXML private TextField champAnnee;
    @FXML private ComboBox<String> champCategorie;
    @FXML private Label labelErreur;

    private final PiloteService piloteService = new PiloteService();
    private final ObservableList<Pilote> donnees = FXCollections.observableArrayList();
    private Pilote piloteSelectionne;

    @FXML
    public void initialize() {
        colLicence.setCellValueFactory(new PropertyValueFactory<>("numeroLicence"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCaserne.setCellValueFactory(new PropertyValueFactory<>("caserne"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("anneeNaissance"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        tablePilotes.setItems(donnees);

        champCategorie.setItems(FXCollections.observableArrayList("Espoir", "Senior", "Vétéran", "Féminine"));

        tablePilotes.getSelectionModel().selectedItemProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null) remplirFormulaire(nouveau);
        });

        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            donnees.setAll(piloteService.listerTous());
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement : " + MessageErreurUtil.traduire(e));
        }
    }

    private void remplirFormulaire(Pilote p) {
        piloteSelectionne = p;
        champLicence.setText(p.getNumeroLicence());
        champNom.setText(p.getNom());
        champPrenom.setText(p.getPrenom());
        champCaserne.setText(p.getCaserne());
        champPoids.setText(p.getPoids() != null ? String.valueOf(p.getPoids()) : "");
        champEmail.setText(p.getEmail());
        champAnnee.setText(p.getAnneeNaissance() != null ? String.valueOf(p.getAnneeNaissance()) : "");
        champCategorie.setValue(p.getCategorie());
    }

    @FXML
    private void viderFormulaire() {
        piloteSelectionne = null;
        champLicence.clear(); champNom.clear(); champPrenom.clear(); champCaserne.clear();
        champPoids.clear(); champEmail.clear(); champAnnee.clear(); champCategorie.setValue(null);
        tablePilotes.getSelectionModel().clearSelection();
        masquerErreur();
    }

    private Pilote construirePiloteDepuisFormulaire() {
        Pilote p = piloteSelectionne != null ? piloteSelectionne : new Pilote();
        p.setNumeroLicence(champLicence.getText().trim());
        p.setNom(champNom.getText().trim());
        p.setPrenom(champPrenom.getText().trim());
        p.setCaserne(champCaserne.getText().trim());
        p.setEmail(champEmail.getText().trim());
        p.setCategorie(champCategorie.getValue());
        try {
            p.setPoids(champPoids.getText().isBlank() ? null : Double.parseDouble(champPoids.getText().trim().replace(",", ".")));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le poids doit être un nombre.");
        }
        try {
            p.setAnneeNaissance(champAnnee.getText().isBlank() ? null : Integer.parseInt(champAnnee.getText().trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("L'année de naissance doit être un nombre entier.");
        }
        return p;
    }

    @FXML
    private void ajouter() {
        try {
            Pilote p = new Pilote();
            piloteSelectionne = null;
            p.setNumeroLicence(champLicence.getText().trim());
            p.setNom(champNom.getText().trim());
            p.setPrenom(champPrenom.getText().trim());
            p.setCaserne(champCaserne.getText().trim());
            p.setEmail(champEmail.getText().trim());
            p.setCategorie(champCategorie.getValue());
            p.setPoids(champPoids.getText().isBlank() ? null : Double.parseDouble(champPoids.getText().trim().replace(",", ".")));
            p.setAnneeNaissance(champAnnee.getText().isBlank() ? null : Integer.parseInt(champAnnee.getText().trim()));
            piloteService.creer(p);
            chargerDonnees();
            viderFormulaire();
        } catch (IllegalArgumentException e) {
            afficherErreur(MessageErreurUtil.traduire(e));
        } catch (SQLException e) {
            afficherErreur("Erreur d'enregistrement (licence en double ?) : " + MessageErreurUtil.traduire(e));
        }
    }

    @FXML
    private void modifier() {
        if (piloteSelectionne == null) {
            afficherErreur("Sélectionnez d'abord un pilote dans le tableau.");
            return;
        }
        try {
            Pilote p = construirePiloteDepuisFormulaire();
            piloteService.mettreAJour(p);
            chargerDonnees();
            viderFormulaire();
        } catch (IllegalArgumentException e) {
            afficherErreur(MessageErreurUtil.traduire(e));
        } catch (SQLException e) {
            afficherErreur("Erreur d'enregistrement : " + MessageErreurUtil.traduire(e));
        }
    }

    @FXML
    private void supprimer() {
        if (piloteSelectionne == null) {
            afficherErreur("Sélectionnez d'abord un pilote dans le tableau.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer définitivement " + piloteSelectionne.getNomComplet() + " ainsi que tous ses résultats ?",
                ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.YES) {
                try {
                    piloteService.supprimer(piloteSelectionne.getId());
                    chargerDonnees();
                    viderFormulaire();
                } catch (SQLException e) {
                    afficherErreur("Erreur de suppression : " + MessageErreurUtil.traduire(e));
                }
            }
        });
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
