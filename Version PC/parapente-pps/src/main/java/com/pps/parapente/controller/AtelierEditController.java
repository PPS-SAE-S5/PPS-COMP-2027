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

        configurerChampHeure(champDebut);
        configurerChampHeure(champFin);
    }

    /**
     * Verification des champs horaires : seuls les chiffres et le caractère ':' sont autorisés et la longueur maximale est de 5 caractères
     */
    private void configurerChampHeure(TextField champ) {
        champ.setTextFormatter(new TextFormatter<String>(changement ->
                changement.getControlNewText().matches("[0-9:]{0,5}") ? changement : null));
        champ.focusedProperty().addListener((obs, avant, focus) -> {
            if (focus) return;
            String texte = champ.getText();
            if (texte == null || texte.isBlank() || PlanningService.heureValide(texte)) {
                champ.getStyleClass().remove(CLASSE_INVALIDE);
            } else {
                marquer(champ);
            }
        });
    }

    /** Appelé avant l'affichage : atelier existant à modifier (ou null) et journée à présélectionner pour un nouvel atelier. */
    public void preparer(Atelier existant, Integer journeeParDefautId) {
        if (existant != null) {
            this.atelier = existant;
            labelTitre.setText("Modifier l'atelier");
            champNom.setText(existant.getNom());
            if (existant.getCreneau() == Creneau.APRES_MIDI) radioApresMidi.setSelected(true);
            else if (existant.getCreneau() == Creneau.MATIN) radioMatin.setSelected(true);
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
        masquerErreur();
        Journee journee = champJournee.getValue();

        // Saisie copiée dans un objet temporaire : l'atelier d'origine (affiché dans le planning)
        // n'est modifié que si les contrôles passent (obligé pour rétablir les valeurs précédentes en cas d'erreur de saisie)
        Atelier saisie = new Atelier();
        saisie.setNom(champNom.getText());
        saisie.setJourneeId(journee == null ? 0 : journee.getId());
        saisie.setCreneau(radioApresMidi.isSelected() ? Creneau.APRES_MIDI
                : radioMatin.isSelected() ? Creneau.MATIN : null);
        saisie.setHeureDebut(champDebut.getText());
        saisie.setHeureFin(champFin.getText());
        saisie.setLieu(champLieu.getText());
        saisie.setResponsable(champResponsable.getText());

        List<String> manquants = PlanningService.champsObligatoiresManquants(saisie);
        if (!manquants.isEmpty()) {
            signalerChampsManquants(manquants);
            afficherErreur(PlanningService.messageChampsManquants(manquants));
            return;
        }

        // Format HH:mm des horaires
        List<String> erreursHoraires = PlanningService.erreursHoraires(saisie.getHeureDebut(), saisie.getHeureFin());
        if (!erreursHoraires.isEmpty()) {
            if (!PlanningService.heureValide(saisie.getHeureDebut())) marquer(champDebut);
            if (!PlanningService.heureValide(saisie.getHeureFin())) marquer(champFin);
            if (erreursHoraires.contains(PlanningService.MESSAGE_ORDRE_HORAIRES)) { marquer(champDebut); marquer(champFin); }
            afficherErreur(String.join("\n", erreursHoraires));
            return;
        }

        atelier.setNom(saisie.getNom());
        atelier.setJourneeId(saisie.getJourneeId());
        atelier.setCreneau(saisie.getCreneau());
        atelier.setHeureDebut(saisie.getHeureDebut());
        atelier.setHeureFin(saisie.getHeureFin());
        atelier.setLieu(saisie.getLieu());
        atelier.setResponsable(saisie.getResponsable());
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

    private void masquerErreur() {
        labelErreur.setVisible(false);
        labelErreur.setManaged(false);
        for (Control c : champsControles()) c.getStyleClass().remove(CLASSE_INVALIDE);
    }

    private static final String CLASSE_INVALIDE = "champ-invalide";

    private List<Control> champsControles() {
        return List.of(champNom, champJournee, radioMatin, radioApresMidi, champDebut, champFin, champLieu, champResponsable);
    }

    /** Si mauvaise saisie/ manquants : encadre en rouge les champs obligatoires non renseignés. */
    private void signalerChampsManquants(List<String> manquants) {
        if (manquants.contains(PlanningService.CHAMP_NOM)) marquer(champNom);
        if (manquants.contains(PlanningService.CHAMP_JOURNEE)) marquer(champJournee);
        if (manquants.contains(PlanningService.CHAMP_CRENEAU)) { marquer(radioMatin); marquer(radioApresMidi); }
        if (manquants.contains(PlanningService.CHAMP_DEBUT)) marquer(champDebut);
        if (manquants.contains(PlanningService.CHAMP_FIN)) marquer(champFin);
        if (manquants.contains(PlanningService.CHAMP_LIEU)) marquer(champLieu);
        if (manquants.contains(PlanningService.CHAMP_RESPONSABLE)) marquer(champResponsable);
    }

    private void marquer(Control c) {
        if (!c.getStyleClass().contains(CLASSE_INVALIDE)) c.getStyleClass().add(CLASSE_INVALIDE);
    }

    public boolean isEnregistre() { return enregistre; }
}
