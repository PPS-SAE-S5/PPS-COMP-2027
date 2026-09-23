package com.pps.parapente.controller;

import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.ModeCalcul;
import com.pps.parapente.service.EpreuveService;
import com.pps.parapente.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class GestionEpreuvesController {

    @FXML private TableView<Epreuve> tableEpreuves;
    @FXML private TableColumn<Epreuve, Integer> colOrdre;
    @FXML private TableColumn<Epreuve, String> colNom;
    @FXML private TableColumn<Epreuve, String> colMode;
    @FXML private TableColumn<Epreuve, String> colRegle;
    @FXML private TableColumn<Epreuve, String> colClassementVisible;
    @FXML private TableColumn<Epreuve, String> colCompteGeneral;
    @FXML private TableColumn<Epreuve, String> colActif;

    private final EpreuveService epreuveService = new EpreuveService();
    private final ObservableList<Epreuve> donnees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colMode.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getModeCalcul() == ModeCalcul.FORMULE ? "Formule" : "Barème"));
        colRegle.setCellValueFactory(cell -> {
            Epreuve e = cell.getValue();
            String texte = e.getModeCalcul() == ModeCalcul.FORMULE
                    ? e.getFormule()
                    : "Classement sur \"" + e.getValeurCle() + "\" (" + e.getSensClassement() + ")";
            return new javafx.beans.property.SimpleStringProperty(texte);
        });
        colClassementVisible.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().isAfficherClassement() ? "Oui" : "Non"));
        colCompteGeneral.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().isCompteDansGeneral() ? "Oui" : "Non"));
        colActif.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().isActif() ? "Oui" : "Non"));

        tableEpreuves.setItems(donnees);
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            donnees.setAll(epreuveService.listerToutes());
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement : " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void nouvelleEpreuve() {
        EpreuveEditController controleur = SceneManager.ouvrirDialogue("epreuve_edit.fxml", "Nouvelle épreuve");
        if (controleur != null && controleur.isEnregistre()) {
            try {
                epreuveService.creer(controleur.getEpreuve());
                chargerDonnees();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void modifierEpreuve() {
        Epreuve selection = tableEpreuves.getSelectionModel().getSelectedItem();
        if (selection == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez une épreuve à modifier.").showAndWait();
            return;
        }
        EpreuveEditController controleur = SceneManager.ouvrirDialogue("epreuve_edit.fxml", "Modifier l'épreuve",
                (EpreuveEditController c) -> c.chargerEpreuveExistante(selection));
        if (controleur != null && controleur.isEnregistre()) {
            try {
                epreuveService.mettreAJour(controleur.getEpreuve());
                chargerDonnees();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void supprimerEpreuve() {
        Epreuve selection = tableEpreuves.getSelectionModel().getSelectedItem();
        if (selection == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez une épreuve à supprimer.").showAndWait();
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'épreuve \"" + selection.getNom() + "\" et tous ses résultats ?",
                ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.YES) {
                try {
                    epreuveService.supprimer(selection.getId());
                    chargerDonnees();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur de suppression : " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    @FXML
    private void retour() {
        SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
    }
}
