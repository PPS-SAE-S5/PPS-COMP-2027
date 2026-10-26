package com.pps.parapente.controller;

import com.pps.parapente.model.Pilote;
import com.pps.parapente.model.SuiviParticipant;
import com.pps.parapente.model.SuiviParticipant.LigneSuivi;
import com.pps.parapente.model.SuiviParticipant.Statut;
import com.pps.parapente.service.SuiviParticipantService;
import com.pps.parapente.util.MessageErreurUtil;
import com.pps.parapente.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Locale;

/**
* Suivi des participants
*/
public class SuiviParticipantsController {

    @FXML private TextField champRecherche;
    @FXML private ListView<Pilote> listePilotes;
    @FXML private Label labelNbResultats;

    @FXML private VBox panneauVide;
    @FXML private ScrollPane panneauDetail;
    @FXML private Label labelNom;
    @FXML private Label labelInfos;
    @FXML private Label valeurTotal;
    @FXML private Label valeurRang;
    @FXML private Label valeurSaisies;

    @FXML private TableView<LigneSuivi> tableScores;
    @FXML private TableColumn<LigneSuivi, String> colEpreuve;
    @FXML private TableColumn<LigneSuivi, String> colValeurs;
    @FXML private TableColumn<LigneSuivi, String> colPoints;
    @FXML private TableColumn<LigneSuivi, String> colStatut;
    @FXML private TableColumn<LigneSuivi, String> colCompte;

    private final SuiviParticipantService service = new SuiviParticipantService();
    private final ObservableList<Pilote> tousLesPilotes = FXCollections.observableArrayList();
    private final FilteredList<Pilote> pilotesFiltres = new FilteredList<>(tousLesPilotes, p -> true);

    @FXML
    public void initialize() {
        listePilotes.setItems(pilotesFiltres);
        listePilotes.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Pilote p, boolean vide) {
                super.updateItem(p, vide);
                if (vide || p == null) {
                    setText(null);
                } else {
                    String caserne = p.getCaserne() == null || p.getCaserne().isBlank() ? "" : "   ·   " + p.getCaserne();
                    setText(p.getNom() + " " + p.getPrenom() + caserne);
                }
            }
        });

        configurerTable();

        champRecherche.textProperty().addListener((obs, ancien, nouveau) -> appliquerFiltre(nouveau));
        listePilotes.getSelectionModel().selectedItemProperty().addListener((obs, ancien, nouveau) -> afficher(nouveau));

        charger(null);
    }

    private void configurerTable() {
        tableScores.setPlaceholder(new Label("Ce pilote n'est inscrit à aucune épreuve."));
        colEpreuve.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().epreuve()));
        colValeurs.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().valeurs()));
        colPoints.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().points() == null ? "—" : formaterPoints(c.getValue().points())));
        colStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().statut().getLibelle()));
        colCompte.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().compteGeneral() ? "Oui" : "Non"));

        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String texte, boolean vide) {
                super.updateItem(texte, vide);
                setText(vide ? null : texte);
                if (vide || texte == null) {
                    setStyle("");
                } else if (texte.equals(Statut.DISQUALIFIE.getLibelle())) {
                    setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold;");
                } else if (texte.equals(Statut.PAS_ENCORE_SAISI.getLibelle())) {
                    setStyle("-fx-text-fill:#7f8c8d;");
                } else {
                    setStyle("");
                }
            }
        });
        colPoints.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
    }

    @FXML
    private void actualiser() {
        Pilote selection = listePilotes.getSelectionModel().getSelectedItem();
        charger(selection == null ? null : selection.getId());
    }

    /** Recharge la liste des pilotes */
    private void charger(Integer piloteIdASelectionner) {
        try {
            tousLesPilotes.setAll(service.listerPilotes());
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement : " + MessageErreurUtil.traduire(e)).showAndWait();
            return;
        }
        appliquerFiltre(champRecherche.getText());
        if (piloteIdASelectionner != null) {
            for (Pilote p : pilotesFiltres) {
                if (p.getId() == piloteIdASelectionner) {
                    listePilotes.getSelectionModel().select(p);
                    afficher(p);   // force le rafresh
                    return;
                }
            }
        }
        afficher(listePilotes.getSelectionModel().getSelectedItem());
    }

    private void appliquerFiltre(String saisie) {
        String[] mots = normaliser(saisie).split("\\s+");
        pilotesFiltres.setPredicate(p -> {
            String botteDeFoin = normaliser(p.getNom() + " " + p.getPrenom() + " " + p.getPrenom() + " " + p.getNom()
                    + " " + (p.getNumeroLicence() == null ? "" : p.getNumeroLicence())
                    + " " + (p.getCaserne() == null ? "" : p.getCaserne()));
            for (String mot : mots) {
                if (!mot.isEmpty() && !botteDeFoin.contains(mot)) return false;
            }
            return true;
        });

        int n = pilotesFiltres.size();
        labelNbResultats.setText(n + (n > 1 ? " pilotes" : " pilote") + (n == tousLesPilotes.size() ? "" : " sur " + tousLesPilotes.size()));

        // Un seul résultat
        if (n == 1) {
            listePilotes.getSelectionModel().select(0);
        }
    }

    private void afficher(Pilote pilote) {
        if (pilote == null) {
            panneauVide.setVisible(true);
            panneauVide.setManaged(true);
            panneauDetail.setVisible(false);
            panneauDetail.setManaged(false);
            return;
        }
        try {
            SuiviParticipant suivi = service.detailler(pilote);
            labelNom.setText(pilote.getNomComplet());

            StringBuilder infos = new StringBuilder();
            ajouter(infos, pilote.getNumeroLicence() == null || pilote.getNumeroLicence().isBlank() ? null : "Licence n°" + pilote.getNumeroLicence());
            ajouter(infos, pilote.getCaserne());
            ajouter(infos, pilote.getGenre());
            ajouter(infos, pilote.getAnneeNaissance() == null ? null : pilote.getAge() + " ans");
            labelInfos.setText(infos.toString());

            valeurTotal.setText(formaterPoints(suivi.totalPoints()));
            valeurRang.setText(suivi.rang() == 0 ? "—" : suivi.rang() + (suivi.rang() == 1 ? "er" : "e") + " / " + suivi.nbPilotes());
            valeurSaisies.setText(suivi.epreuvesSaisies() + " / " + suivi.epreuvesAttendues());
            tableScores.getItems().setAll(suivi.lignes());

            panneauVide.setVisible(false);
            panneauVide.setManaged(false);
            panneauDetail.setVisible(true);
            panneauDetail.setManaged(true);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement des scores : " + MessageErreurUtil.traduire(e)).showAndWait();
        }
    }

    private void ajouter(StringBuilder sb, String texte) {
        if (texte == null || texte.isBlank()) return;
        if (sb.length() > 0) sb.append("   ·   ");
        sb.append(texte);
    }

    private String formaterPoints(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.format(Locale.FRANCE, "%.2f", v);
    }

    /** Regex pour les minuscules et sans accents */
    private String normaliser(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    @FXML
    private void retour() {
        SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
    }
}
