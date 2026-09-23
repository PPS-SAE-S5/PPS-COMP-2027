package com.pps.parapente.controller;

import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.LigneClassement;
import com.pps.parapente.service.ClassementService;
import com.pps.parapente.service.EpreuveService;
import com.pps.parapente.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Classement général : rang, pilote, total, puis une colonne PAR épreuve comptant
 * dans le général (générées dynamiquement, car le nombre et le nom des épreuves
 * ne sont jamais fixés à l'avance).
 */
public class ClassementController {

    @FXML private TableView<LigneClassement> tableClassement;

    private final ClassementService classementService = new ClassementService();
    private final EpreuveService epreuveService = new EpreuveService();

    @FXML
    public void initialize() {
        actualiser();
    }

    @FXML
    private void actualiser() {
        try {
            construireColonnes();
            List<LigneClassement> classement = classementService.calculerClassementGeneral();
            tableClassement.setItems(FXCollections.observableArrayList(classement));
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur de calcul du classement : " + e.getMessage()).showAndWait();
        }
    }

    private void construireColonnes() throws SQLException {
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

        tableClassement.getColumns().addAll(colRang, colPilote, colCaserne);

        List<Epreuve> epreuvesComptees = epreuveService.listerToutes().stream()
                .filter(Epreuve::isActif)
                .filter(Epreuve::isCompteDansGeneral)
                .filter(Epreuve::isAfficherClassement) // respecte la case "afficher le classement ou non"
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

    @FXML
    private void retour() {
        SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
    }
}
