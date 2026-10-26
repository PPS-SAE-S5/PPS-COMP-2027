package com.pps.parapente.controller;

import com.pps.parapente.model.Atelier;
import com.pps.parapente.model.Creneau;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Journee;
import com.pps.parapente.service.PlanningService;
import com.pps.parapente.util.MessageErreurUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulaire de création / modification d'un atelier : informations (journée, créneau, horaires,
 * lieu, responsable) + liste ordonnée des épreuves qu'il regroupe.
 */
public class AtelierEditController {

    @FXML private Label labelTitre;
    @FXML private TextField champNom;
    @FXML private ComboBox<Journee> champJournee;
    @FXML private RadioButton radioMatin;
    @FXML private RadioButton radioApresMidi;
    @FXML private TextField champDebut;
    @FXML private TextField champFin;
    @FXML private TextField champLieu;
    @FXML private TextField champResponsable;
    @FXML private ListView<Epreuve> listeEpreuves;
    @FXML private ComboBox<Epreuve> champAjout;
    @FXML private Label labelAucuneEpreuve;
    @FXML private Label labelErreur;

    private final PlanningService planningService = new PlanningService();
    private final ObservableList<Epreuve> epreuves = FXCollections.observableArrayList();

    private Atelier atelier = new Atelier();   // id = 0 : nouvel atelier
    private boolean enregistre = false;
    private List<Epreuve> epreuvesDisponibles = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            champJournee.setItems(FXCollections.observableArrayList(planningService.listerJournees()));
        } catch (SQLException e) {
            afficherErreur("Impossible de charger les journées : " + MessageErreurUtil.traduire(e));
        }

        listeEpreuves.setItems(epreuves);
        listeEpreuves.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Epreuve e, boolean vide) {
                super.updateItem(e, vide);
                setText(vide || e == null ? null : (getIndex() + 1) + ".  " + e.getNom());
            }
        });
        chargerEpreuvesDisponibles();
    }

    /** Appelé avant l'affichage : atelier existant à modifier (ou null) et journée à présélectionner pour un nouvel atelier. */
    public void preparer(Atelier existant, Integer journeeParDefautId) {
        if (existant != null) {
            this.atelier = existant;
            labelTitre.setText("Modifier l'atelier");
            champNom.setText(existant.getNom());
            (existant.getCreneau() == Creneau.APRES_MIDI ? radioApresMidi : radioMatin).setSelected(true);
            champDebut.setText(existant.getHeureDebut());
            champFin.setText(existant.getHeureFin());
            champLieu.setText(existant.getLieu());
            champResponsable.setText(existant.getResponsable());
            epreuves.setAll(existant.getEpreuves());
            selectionnerJournee(existant.getJourneeId());
        } else if (journeeParDefautId != null) {
            selectionnerJournee(journeeParDefautId);
        }
        chargerEpreuvesDisponibles();
    }

    private void selectionnerJournee(int id) {
        for (Journee j : champJournee.getItems()) {
            if (j.getId() == id) {
                champJournee.setValue(j);
                return;
            }
        }
    }

    private void chargerEpreuvesDisponibles() {
        try {
            epreuvesDisponibles = planningService.listerEpreuvesDisponibles(atelier.getId());
        } catch (SQLException e) {
            afficherErreur("Impossible de charger les épreuves : " + MessageErreurUtil.traduire(e));
            epreuvesDisponibles = new ArrayList<>();
        }
        rafraichirListeAjout();
    }

    /** La liste déroulante ne propose que les épreuves libres, et pas celles déjà dans la liste. */
    private void rafraichirListeAjout() {
        List<Epreuve> proposees = new ArrayList<>();
        for (Epreuve e : epreuvesDisponibles) {
            boolean dejaLa = epreuves.stream().anyMatch(x -> x.getId() == e.getId());
            if (!dejaLa) proposees.add(e);
        }
        champAjout.setItems(FXCollections.observableArrayList(proposees));
        champAjout.setValue(null);
        boolean aucune = proposees.isEmpty();
        champAjout.setDisable(aucune);
        labelAucuneEpreuve.setVisible(aucune);
        labelAucuneEpreuve.setManaged(aucune);
    }

    @FXML
    private void ajouter() {
        Epreuve choix = champAjout.getValue();
        if (choix == null) return;
        epreuves.add(choix);
        rafraichirListeAjout();
        listeEpreuves.getSelectionModel().select(choix);
    }

    @FXML
    private void retirer() {
        Epreuve selection = listeEpreuves.getSelectionModel().getSelectedItem();
        if (selection == null) return;
        epreuves.remove(selection);
        rafraichirListeAjout();
    }

    @FXML private void monter() { deplacer(-1); }
    @FXML private void descendre() { deplacer(1); }

    private void deplacer(int sens) {
        int i = listeEpreuves.getSelectionModel().getSelectedIndex();
        int j = i + sens;
        if (i < 0 || j < 0 || j >= epreuves.size()) return;
        Epreuve e = epreuves.remove(i);
        epreuves.add(j, e);
        listeEpreuves.getSelectionModel().select(j);
    }

    @FXML
    private void enregistrer() {
        Journee journee = champJournee.getValue();
        if (journee == null) {
            afficherErreur("Choisissez la journée de cet atelier.");
            return;
        }
        atelier.setNom(champNom.getText());
        atelier.setJourneeId(journee.getId());
        atelier.setCreneau(radioApresMidi.isSelected() ? Creneau.APRES_MIDI : Creneau.MATIN);
        atelier.setHeureDebut(champDebut.getText());
        atelier.setHeureFin(champFin.getText());
        atelier.setLieu(champLieu.getText());
        atelier.setResponsable(champResponsable.getText());
        atelier.setEpreuves(new ArrayList<>(epreuves));

        try {
            if (atelier.getId() == 0) planningService.creerAtelier(atelier);
            else planningService.mettreAJourAtelier(atelier);
        } catch (Exception e) {
            afficherErreur(MessageErreurUtil.traduire(e));
            return;
        }
        enregistre = true;
        fermer();
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        ((Stage) champNom.getScene().getWindow()).close();
    }

    private void afficherErreur(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }

    public boolean isEnregistre() { return enregistre; }
}
