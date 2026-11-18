package com.pps.parapente.controller;
import com.pps.parapente.util.MessageErreurUtil;

import com.pps.parapente.model.Categorie;
import com.pps.parapente.model.Pilote;
import com.pps.parapente.service.PiloteService;
import com.pps.parapente.util.CategorieConfig;
import com.pps.parapente.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class GestionPilotesController {

    @FXML private TableView<Pilote> tablePilotes;
    @FXML private TableColumn<Pilote, Integer> colNumeroPilote;
    @FXML private TableColumn<Pilote, Categorie> colCategorie;
    @FXML private TableColumn<Pilote, String> colLicence;
    @FXML private TableColumn<Pilote, String> colNom;
    @FXML private TableColumn<Pilote, String> colPrenom;
    @FXML private TableColumn<Pilote, String> colDepartement;
    @FXML private TableColumn<Pilote, Double> colPoids;
    @FXML private TableColumn<Pilote, String> colEmail;
    @FXML private TableColumn<Pilote, Integer> colAnnee;
    @FXML private TableColumn<Pilote, String> colGenre;

    @FXML private TextField champNumeroPilote;
    @FXML private ComboBox<Categorie> champCategorie;
    @FXML private Label labelRegleCategorie;
    @FXML private TextField champLicence;
    @FXML private TextField champNom;
    @FXML private TextField champPrenom;
    @FXML private TextField champDepartement;
    @FXML private TextField champPoids;
    @FXML private TextField champEmail;
    @FXML private TextField champAnnee;
    @FXML private ComboBox<String> champGenre;
    @FXML private Label labelErreur;

    private final PiloteService piloteService = new PiloteService();
    private final ObservableList<Pilote> donnees = FXCollections.observableArrayList();
    private Pilote piloteSelectionne;
    /** Vrai pendant le remplissage du formulaire : la catégorie enregistrée ne doit pas être recalculée. */
    private boolean remplissageEnCours;

    @FXML
    public void initialize() {
        colNumeroPilote.setCellValueFactory(new PropertyValueFactory<>("numeroPilote"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colLicence.setCellValueFactory(new PropertyValueFactory<>("numeroLicence"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("anneeNaissance"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        tablePilotes.setItems(donnees);

        champGenre.setItems(FXCollections.observableArrayList("Homme", "Femme"));

        // Catégorie déduite du numéro de pilote dès qu'on le saisit (reste modifiable à la main)
        champCategorie.setItems(FXCollections.observableArrayList(Categorie.values()));
        champCategorie.setValue(Categorie.POMPIER);
        champNumeroPilote.setTextFormatter(new TextFormatter<String>(c ->
                c.getControlNewText().matches("\\d{0,6}") ? c : null));
        champNumeroPilote.textProperty().addListener((obs, ancien, nouveau) -> {
            if (!remplissageEnCours) champCategorie.setValue(CategorieConfig.deduire(lireNumeroPilote()));
        });
        String numerosOpen = CategorieConfig.descriptionNumerosOpen();
        labelRegleCategorie.setText(numerosOpen.isEmpty()
                ? "Aucun numéro « open » défini (data/categories.properties) : catégorie Pompier par défaut."
                : "Numéros « open » : " + numerosOpen + " (data/categories.properties).");

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
        remplissageEnCours = true;
        champNumeroPilote.setText(p.getNumeroPilote() != null ? String.valueOf(p.getNumeroPilote()) : "");
        champCategorie.setValue(p.getCategorie());
        remplissageEnCours = false;
        champLicence.setText(p.getNumeroLicence());
        champNom.setText(p.getNom());
        champPrenom.setText(p.getPrenom());
        champDepartement.setText(p.getDepartement());
        champPoids.setText(p.getPoids() != null ? String.valueOf(p.getPoids()) : "");
        champEmail.setText(p.getEmail());
        champAnnee.setText(p.getAnneeNaissance() != null ? String.valueOf(p.getAnneeNaissance()) : "");
        champGenre.setValue(p.getGenre());
    }

    @FXML
    private void viderFormulaire() {
        piloteSelectionne = null;
        champNumeroPilote.clear(); champCategorie.setValue(Categorie.POMPIER);
        champLicence.clear(); champNom.clear(); champPrenom.clear(); champDepartement.clear();
        champPoids.clear(); champEmail.clear(); champAnnee.clear(); champGenre.setValue(null);
        tablePilotes.getSelectionModel().clearSelection();
        masquerErreur();
    }

    private Pilote construirePiloteDepuisFormulaire() {
        Pilote p = piloteSelectionne != null ? piloteSelectionne : new Pilote();
        p.setNumeroLicence(champLicence.getText().trim());
        p.setNom(champNom.getText().trim());
        p.setPrenom(champPrenom.getText().trim());
        p.setDepartement(champDepartement.getText().trim());
        p.setEmail(champEmail.getText().trim());
        p.setGenre(champGenre.getValue());
        p.setNumeroPilote(lireNumeroPilote());
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
            piloteSelectionne = null;
            Pilote p = construirePiloteDepuisFormulaire();
            piloteService.creer(p);
            chargerDonnees();
            viderFormulaire();
        } catch (IllegalArgumentException e) {
            afficherErreur(MessageErreurUtil.traduire(e));
        } catch (SQLException e) {
            afficherErreur("Erreur d'enregistrement : " + MessageErreurUtil.traduire(e));
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

    /** Numéro de pilote saisi, ou null si le champ est vide. */
    private Integer lireNumeroPilote() {
        String t = champNumeroPilote.getText();
        if (t == null || t.isBlank()) return null;
        try {
            return Integer.parseInt(t.trim());
        } catch (NumberFormatException e) {
            return null;
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
