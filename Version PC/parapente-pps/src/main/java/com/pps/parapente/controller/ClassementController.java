package com.pps.parapente.controller;
import com.pps.parapente.util.MessageErreurUtil;

import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.FiltreGenre;
import com.pps.parapente.model.LigneClassement;
import com.pps.parapente.model.Resultat;
import com.pps.parapente.service.ClassementService;
import com.pps.parapente.service.EpreuveService;
import com.pps.parapente.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Deux vues dans cet écran :
 *  - Classement général : rang, pilote, total, une colonne PAR épreuve (générées dynamiquement).
 *  - Détail par épreuve : classement complet (avec les valeurs brutes saisies) d'UNE épreuve à la fois,
 *    réservé aux épreuves dont "afficher le classement" est coché. Accessible à tous les rôles,
 *    y compris Pilote et Comité des pilotes.
 */
public class ClassementController {

    @FXML private ComboBox<FiltreGenre> comboFiltreGenre;
    @FXML private Label labelFiltre;

    @FXML private TableView<LigneClassement> tableClassement;

    @FXML private ComboBox<Epreuve> comboEpreuveDetail;
    @FXML private TableView<Resultat> tableDetailEpreuve;
    @FXML private TableColumn<Resultat, Number> colDetailRang;
    @FXML private TableColumn<Resultat, String> colDetailPilote;
    @FXML private TableColumn<Resultat, String> colDetailCaserne;
    @FXML private TableColumn<Resultat, String> colDetailGenre;
    @FXML private TableColumn<Resultat, String> colDetailValeurs;
    @FXML private TableColumn<Resultat, Number> colDetailPoints;
    @FXML private TableColumn<Resultat, String> colDetailDisqualifie;

    private final ClassementService classementService = new ClassementService();
    private final EpreuveService epreuveService = new EpreuveService();

    @FXML
    public void initialize() {
        configurerColonnesDetail();
        // Filtre Mixte / Hommes / Femmes (valeur fixée AVANT d'ajouter l'écouteur pour ne pas recalculer deux fois).
        comboFiltreGenre.setItems(FXCollections.observableArrayList(FiltreGenre.values()));
        comboFiltreGenre.setValue(FiltreGenre.MIXTE);
        comboFiltreGenre.valueProperty().addListener((obs, ancien, nouveau) -> actualiser());
        comboEpreuveDetail.valueProperty().addListener((obs, ancien, nouveau) -> chargerDetailEpreuve(nouveau));
        actualiser();
    }

    @FXML
    private void actualiser() {
        try {
            construireColonnesClassementGeneral();
            FiltreGenre filtre = filtreCourant();
            List<LigneClassement> classement = classementService.calculerClassementGeneral(filtre);
            tableClassement.getItems().setAll(classement);

            boolean afficherNote = filtre != FiltreGenre.MIXTE;
            labelFiltre.setText(afficherNote
                    ? "Classement « " + filtre.getLibelle() + " » : les rangs sont recalculés parmi ces pilotes. "
                      + "Les pilotes dont le genre n'est pas renseigné n'apparaissent que dans le classement mixte."
                    : "");
            labelFiltre.setVisible(afficherNote);
            labelFiltre.setManaged(afficherNote);

            chargerListeEpreuvesAffichables();
            // Recharge explicitement le détail : si l'épreuve sélectionnée n'a pas changé, aucun événement n'est émis.
            chargerDetailEpreuve(comboEpreuveDetail.getValue());
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur de calcul du classement : " + MessageErreurUtil.traduire(e)).showAndWait();
        }
    }

    // Classement général

    private void construireColonnesClassementGeneral() throws SQLException {
        tableClassement.getColumns().clear();

        TableColumn<LigneClassement, Number> colRang = new TableColumn<>("Rang");
        colRang.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getRang()));
        colRang.setPrefWidth(60);

        TableColumn<LigneClassement, String> colPilote = new TableColumn<>("Pilote");
        colPilote.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPilote().getNomComplet()));
        colPilote.setPrefWidth(180);

        TableColumn<LigneClassement, String> colCaserne = new TableColumn<>("Caserne");
        colCaserne.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPilote().getCaserne()));
        colCaserne.setPrefWidth(150);

        TableColumn<LigneClassement, String> colGenre = new TableColumn<>("Genre");
        colGenre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPilote().getGenre() != null ? c.getValue().getPilote().getGenre() : ""));
        colGenre.setPrefWidth(70);

        tableClassement.getColumns().addAll(colRang, colPilote, colCaserne, colGenre);

        List<Epreuve> epreuvesComptees = epreuveService.listerToutes().stream()
                .filter(Epreuve::isActif)
                .filter(Epreuve::isCompteDansGeneral)
                .filter(Epreuve::isAfficherClassement) // check si respecte la case "afficher le classement ou non"
                .toList();

        for (Epreuve epreuve : epreuvesComptees) {
            TableColumn<LigneClassement, Number> colEpreuve = new TableColumn<>(epreuve.getNom());
            colEpreuve.setPrefWidth(120);
            colEpreuve.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(
                    c.getValue().getPointsParEpreuve().getOrDefault(epreuve.getId(), 0.0)));
            tableClassement.getColumns().add(colEpreuve);
        }

        TableColumn<LigneClassement, Number> colTotal = new TableColumn<>("TOTAL");
        colTotal.setPrefWidth(90);
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotalPoints()));
        colTotal.setStyle("-fx-font-weight: bold;");
        tableClassement.getColumns().add(colTotal);
    }

    // Détail par épreuve

    private void configurerColonnesDetail() {
        colDetailRang.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                tableDetailEpreuve.getItems().indexOf(c.getValue()) + 1));
        colDetailPilote.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPilote() != null ? c.getValue().getPilote().getNomComplet() : "?"));
        colDetailCaserne.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPilote() != null ? c.getValue().getPilote().getCaserne() : ""));
        colDetailGenre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPilote() != null && c.getValue().getPilote().getGenre() != null
                        ? c.getValue().getPilote().getGenre() : ""));
        colDetailValeurs.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                formaterValeurs(c.getValue().getValeurs())));
        colDetailPoints.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPoints()));
        colDetailDisqualifie.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().isDisqualifie() ? "Oui" : ""));
    }

    private String formaterValeurs(Map<String, Double> valeurs) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : valeurs.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }

    private void chargerListeEpreuvesAffichables() throws SQLException {
        Epreuve selectionActuelle = comboEpreuveDetail.getValue();
        List<Epreuve> affichables = epreuveService.listerToutes().stream()
                .filter(Epreuve::isAfficherClassement)
                .toList();
        comboEpreuveDetail.setItems(FXCollections.observableArrayList(affichables));
        if (selectionActuelle != null && affichables.contains(selectionActuelle)) {
            comboEpreuveDetail.setValue(selectionActuelle);
        } else if (!affichables.isEmpty()) {
            comboEpreuveDetail.setValue(affichables.get(0));
        }
    }

    private void chargerDetailEpreuve(Epreuve epreuve) {
        if (epreuve == null) {
            tableDetailEpreuve.getItems().clear();
            return;
        }
        try {
            // Résultats triés par points décroissants = classement de cette seule épreuve.
            List<Resultat> resultats = classementService.calculerClassementEpreuve(epreuve, filtreCourant());
            tableDetailEpreuve.getItems().setAll(resultats);
            tableDetailEpreuve.refresh(); // pour recalculer les rangs affichés (basés sur la position dans la liste)
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement du détail : " + MessageErreurUtil.traduire(e)).showAndWait();
        }
    }

    private FiltreGenre filtreCourant() {
        FiltreGenre f = comboFiltreGenre.getValue();
        return f != null ? f : FiltreGenre.MIXTE;
    }

    @FXML
    private void retour() {
        SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
    }
}
