package com.pps.parapente.controller;

import com.pps.parapente.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * Contrôleur du formulaire de création/modification d'une épreuve.
 * Toute la logique "l'épreuve est 100% paramétrable" est ici :
 * le responsable choisit lui-même le nom, les variables saisissables,
 * et la règle de calcul (formule libre ou barème par rang).
 */
public class EpreuveEditController {

    @FXML private TextField champNom;
    @FXML private TextArea champDescription;

    @FXML private RadioButton radioFormule;
    @FXML private RadioButton radioBareme;
    @FXML private ToggleGroup groupeMode;

    @FXML private VBox blocFormule;
    @FXML private VBox blocBareme;
    @FXML private TextField champFormule;

    @FXML private ComboBox<String> champValeurCle;
    @FXML private ComboBox<String> champSensClassement;

    @FXML private TableView<BaremePoint> tableBareme;
    @FXML private TableColumn<BaremePoint, Integer> colRang;
    @FXML private TableColumn<BaremePoint, Double> colPointsBareme;

    @FXML private TableView<EpreuveParametre> tableParametres;
    @FXML private TableColumn<EpreuveParametre, String> colNomVariable;
    @FXML private TableColumn<EpreuveParametre, String> colLabel;
    @FXML private TableColumn<EpreuveParametre, String> colUnite;

    @FXML private CheckBox checkAfficherClassement;
    @FXML private CheckBox checkCompteGeneral;
    @FXML private CheckBox checkActif;

    @FXML private Label labelErreur;

    private static final String SENS_ASC = "La plus petite gagne (ex: temps, nombre d'erreurs)";
    private static final String SENS_DESC = "La plus grande gagne (ex: distance, score)";

    private final ObservableList<EpreuveParametre> parametres = FXCollections.observableArrayList();
    private final ObservableList<BaremePoint> bareme = FXCollections.observableArrayList();

    private int epreuveIdEnEdition = 0; // 0 = nouvelle épreuve
    private boolean enregistre = false;

    @FXML
    public void initialize() {
        // Bascule formule / barème
        radioFormule.setSelected(true);
        blocBareme.setVisible(false);
        blocBareme.setManaged(false);
        groupeMode.selectedToggleProperty().addListener((obs, ancien, nouveau) -> {
            boolean modeBareme = radioBareme.isSelected();
            blocBareme.setVisible(modeBareme);
            blocBareme.setManaged(modeBareme);
            blocFormule.setVisible(!modeBareme);
            blocFormule.setManaged(!modeBareme);
        });

        champSensClassement.setItems(FXCollections.observableArrayList(SENS_ASC, SENS_DESC));
        champSensClassement.setValue(SENS_ASC);

        checkAfficherClassement.setSelected(true);
        checkCompteGeneral.setSelected(true);
        checkActif.setSelected(true);

        configurerTableParametres();
        configurerTableBareme();
    }

    private void configurerTableParametres() {
        colNomVariable.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomVariable()));
        colNomVariable.setCellFactory(TextFieldTableCell.forTableColumn());
        colNomVariable.setOnEditCommit(e -> e.getRowValue().setNomVariable(nettoyerNomVariable(e.getNewValue())));

        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLabel()));
        colLabel.setCellFactory(TextFieldTableCell.forTableColumn());
        colLabel.setOnEditCommit(e -> e.getRowValue().setLabel(e.getNewValue()));

        colUnite.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnite()));
        colUnite.setCellFactory(TextFieldTableCell.forTableColumn());
        colUnite.setOnEditCommit(e -> e.getRowValue().setUnite(e.getNewValue()));

        tableParametres.setItems(parametres);
        tableParametres.setEditable(true);

        // La liste des variables disponibles pour le classement (mode barème) se met à jour automatiquement
        parametres.addListener((javafx.collections.ListChangeListener<EpreuveParametre>) c -> rafraichirListeVariables());
    }

    private void configurerTableBareme() {
        colRang.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getRang()).asObject());
        colRang.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colRang.setOnEditCommit(e -> e.getRowValue().setRang(e.getNewValue()));

        colPointsBareme.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPoints()).asObject());
        colPointsBareme.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colPointsBareme.setOnEditCommit(e -> e.getRowValue().setPoints(e.getNewValue()));

        tableBareme.setItems(bareme);
        tableBareme.setEditable(true);
    }

    private void rafraichirListeVariables() {
        String valeurActuelle = champValeurCle.getValue();
        ObservableList<String> noms = FXCollections.observableArrayList();
        for (EpreuveParametre p : parametres) {
            if (p.getNomVariable() != null && !p.getNomVariable().isBlank()) noms.add(p.getNomVariable());
        }
        champValeurCle.setItems(noms);
        if (valeurActuelle != null) champValeurCle.setValue(valeurActuelle);
    }

    private String nettoyerNomVariable(String saisie) {
        if (saisie == null) return "";
        return saisie.trim().replaceAll("\\s+", "");
    }

    @FXML
    private void ajouterParametre() {
        EpreuveParametre p = new EpreuveParametre("variable" + (parametres.size() + 1), "Nouvelle variable", "");
        p.setOrdre(parametres.size());
        parametres.add(p);
    }

    @FXML
    private void retirerParametre() {
        EpreuveParametre selection = tableParametres.getSelectionModel().getSelectedItem();
        if (selection != null) parametres.remove(selection);
    }

    @FXML
    private void ajouterLigneBareme() {
        int prochainRang = bareme.isEmpty() ? 1 : bareme.get(bareme.size() - 1).getRang() + 1;
        bareme.add(new BaremePoint(prochainRang, 0));
    }

    @FXML
    private void retirerLigneBareme() {
        BaremePoint selection = tableBareme.getSelectionModel().getSelectedItem();
        if (selection != null) bareme.remove(selection);
    }

    /** Pré-remplit le formulaire pour la modification d'une épreuve existante. */
    public void chargerEpreuveExistante(Epreuve e) {
        this.epreuveIdEnEdition = e.getId();
        champNom.setText(e.getNom());
        champDescription.setText(e.getDescription());

        if (e.getModeCalcul() == ModeCalcul.FORMULE) {
            radioFormule.setSelected(true);
        } else {
            radioBareme.setSelected(true);
        }
        champFormule.setText(e.getFormule());
        champSensClassement.setValue(e.getSensClassement() == SensClassement.ASC ? SENS_ASC : SENS_DESC);

        parametres.setAll(e.getParametres());
        bareme.setAll(e.getBareme());
        rafraichirListeVariables();
        champValeurCle.setValue(e.getValeurCle());

        checkAfficherClassement.setSelected(e.isAfficherClassement());
        checkCompteGeneral.setSelected(e.isCompteDansGeneral());
        checkActif.setSelected(e.isActif());
    }

    @FXML
    private void enregistrer() {
        if (champNom.getText() == null || champNom.getText().isBlank()) {
            afficherErreur("Le nom de l'épreuve est obligatoire.");
            return;
        }
        boolean modeBareme = radioBareme.isSelected();
        if (!modeBareme && (champFormule.getText() == null || champFormule.getText().isBlank())) {
            afficherErreur("Veuillez saisir la formule de calcul.");
            return;
        }
        if (modeBareme && (champValeurCle.getValue() == null || champValeurCle.getValue().isBlank())) {
            afficherErreur("Veuillez choisir la variable utilisée pour classer les pilotes.");
            return;
        }
        if (modeBareme && bareme.isEmpty()) {
            afficherErreur("Veuillez définir au moins une ligne de barème (rang -> points).");
            return;
        }

        enregistre = true;
        fermer();
    }

    @FXML
    private void annuler() {
        enregistre = false;
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) champNom.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }

    public boolean isEnregistre() {
        return enregistre;
    }

    /** Construit l'objet Epreuve final à partir du formulaire (à appeler après isEnregistre()==true). */
    public Epreuve getEpreuve() {
        Epreuve e = new Epreuve();
        e.setId(epreuveIdEnEdition);
        e.setNom(champNom.getText().trim());
        e.setDescription(champDescription.getText());
        e.setModeCalcul(radioBareme.isSelected() ? ModeCalcul.BAREME : ModeCalcul.FORMULE);
        e.setFormule(champFormule.getText());
        e.setValeurCle(champValeurCle.getValue());
        e.setSensClassement(SENS_DESC.equals(champSensClassement.getValue()) ? SensClassement.DESC : SensClassement.ASC);
        e.setAfficherClassement(checkAfficherClassement.isSelected());
        e.setCompteDansGeneral(checkCompteGeneral.isSelected());
        e.setActif(checkActif.isSelected());
        e.setParametres(new java.util.ArrayList<>(parametres));
        e.setBareme(new java.util.ArrayList<>(bareme));
        return e;
    }
}
