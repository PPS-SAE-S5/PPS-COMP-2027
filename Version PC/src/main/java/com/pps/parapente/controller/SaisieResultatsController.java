package com.pps.parapente.controller;

import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.model.*;
import com.pps.parapente.service.CalculService;
import com.pps.parapente.service.EpreuveService;
import com.pps.parapente.service.PiloteService;
import com.pps.parapente.util.SceneManager;
import com.pps.parapente.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.*;

/**
 * Écran de saisie des résultats : la liste des champs à saisir est générée
 * DYNAMIQUEMENT selon les variables définies pour l'épreuve sélectionnée
 * (aucun champ n'est codé en dur, tout vient de la configuration de l'épreuve).
 */
public class SaisieResultatsController {

    @FXML private ComboBox<Epreuve> comboEpreuve;
    @FXML private ComboBox<Pilote> comboPilote;
    @FXML private VBox conteneurChampsDynamiques;
    @FXML private CheckBox checkDisqualifie;
    @FXML private Label labelErreur;
    @FXML private Label labelInfo;

    @FXML private TableView<Resultat> tableResultats;
    @FXML private TableColumn<Resultat, String> colPiloteNom;
    @FXML private TableColumn<Resultat, String> colValeurs;
    @FXML private TableColumn<Resultat, Double> colPoints;
    @FXML private TableColumn<Resultat, String> colDisqualifie;

    private final EpreuveService epreuveService = new EpreuveService();
    private final PiloteService piloteService = new PiloteService();
    private final CalculService calculService = new CalculService();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    private final Map<String, TextField> champsDynamiques = new LinkedHashMap<>();
    private List<Pilote> tousLesPilotes = new ArrayList<>();

    @FXML
    public void initialize() {
        configurerColonnesResultats();
        chargerEpreuves();

        comboEpreuve.valueProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null) {
                genererChampsDynamiques(nouveau);
                chargerPilotesInscrits(nouveau);
                chargerTableResultats(nouveau);
            }
        });

        comboPilote.valueProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null && comboEpreuve.getValue() != null) {
                preRemplirSiResultatExistant(comboEpreuve.getValue(), nouveau);
            }
        });
    }

    private void configurerColonnesResultats() {
        colPiloteNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPilote() != null ? c.getValue().getPilote().getNomComplet() : "?"));
        colValeurs.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                formaterValeurs(c.getValue().getValeurs())));
        colPoints.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPoints()).asObject());
        colDisqualifie.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
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

    private void chargerEpreuves() {
        try {
            List<Epreuve> actives = epreuveService.listerToutes().stream().filter(Epreuve::isActif).toList();
            comboEpreuve.setItems(FXCollections.observableArrayList(actives));
            tousLesPilotes = piloteService.listerTous();
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement : " + e.getMessage());
        }
    }

    private void genererChampsDynamiques(Epreuve epreuve) {
        conteneurChampsDynamiques.getChildren().clear();
        champsDynamiques.clear();
        for (EpreuveParametre param : epreuve.getParametres()) {
            Label label = new Label(param.toString() + (param.isObligatoire() ? " *" : ""));
            TextField champ = new TextField();
            champ.setPromptText("Valeur numérique");
            champsDynamiques.put(param.getNomVariable(), champ);
            conteneurChampsDynamiques.getChildren().addAll(label, champ);
        }
        if (epreuve.getParametres().isEmpty()) {
            conteneurChampsDynamiques.getChildren().add(new Label("Aucune variable définie pour cette épreuve."));
        }
    }

    private void chargerPilotesInscrits(Epreuve epreuve) {
        try {
            List<Integer> inscritsIds = inscriptionDAO.listerPilotesInscrits(epreuve.getId());
            List<Pilote> inscrits = tousLesPilotes.stream().filter(p -> inscritsIds.contains(p.getId())).toList();
            comboPilote.setItems(FXCollections.observableArrayList(inscrits));
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement des inscrits : " + e.getMessage());
        }
    }

    private void chargerTableResultats(Epreuve epreuve) {
        try {
            List<Resultat> resultats = new com.pps.parapente.service.ClassementService().calculerClassementEpreuve(epreuve);
            tableResultats.setItems(FXCollections.observableArrayList(resultats));
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement des résultats : " + e.getMessage());
        }
    }

    private void preRemplirSiResultatExistant(Epreuve epreuve, Pilote pilote) {
        try {
            var resultatOpt = new com.pps.parapente.dao.ResultatDAO().trouver(epreuve.getId(), pilote.getId());
            checkDisqualifie.setSelected(false);
            for (TextField champ : champsDynamiques.values()) champ.clear();
            resultatOpt.ifPresent(r -> {
                checkDisqualifie.setSelected(r.isDisqualifie());
                for (Map.Entry<String, Double> e : r.getValeurs().entrySet()) {
                    TextField champ = champsDynamiques.get(e.getKey());
                    if (champ != null) champ.setText(String.valueOf(e.getValue()));
                }
            });
        } catch (SQLException e) {
            afficherErreur("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void enregistrerResultat() {
        masquerMessages();
        Epreuve epreuve = comboEpreuve.getValue();
        Pilote pilote = comboPilote.getValue();
        if (epreuve == null || pilote == null) {
            afficherErreur("Choisissez une épreuve et un pilote.");
            return;
        }

        Map<String, Double> valeurs = new LinkedHashMap<>();
        try {
            for (Map.Entry<String, TextField> entree : champsDynamiques.entrySet()) {
                String texte = entree.getValue().getText();
                if (texte == null || texte.isBlank()) continue;
                valeurs.put(entree.getKey(), Double.parseDouble(texte.trim().replace(",", ".")));
            }
        } catch (NumberFormatException e) {
            afficherErreur("Toutes les valeurs saisies doivent être numériques.");
            return;
        }

        boolean disqualifie = checkDisqualifie.isSelected();
        String saisiPar = SessionManager.getUtilisateurConnecte() != null
                ? SessionManager.getUtilisateurConnecte().getIdentifiant() : "inconnu";

        try {
            if (epreuve.getModeCalcul() == ModeCalcul.FORMULE) {
                calculService.calculerEtEnregistrerFormule(epreuve, pilote, valeurs, disqualifie, saisiPar);
            } else {
                calculService.enregistrerValeurBareme(epreuve, pilote, valeurs, disqualifie, saisiPar);
            }
            afficherInfo("Résultat enregistré pour " + pilote.getNomComplet() + ".");
            chargerTableResultats(epreuve);
        } catch (IllegalStateException | IllegalArgumentException e) {
            afficherErreur("Erreur de calcul : " + e.getMessage());
        } catch (SQLException e) {
            afficherErreur("Erreur d'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void retour() {
        SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
    }

    private void afficherErreur(String message) {
        labelInfo.setVisible(false); labelInfo.setManaged(false);
        labelErreur.setText(message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }

    private void afficherInfo(String message) {
        labelErreur.setVisible(false); labelErreur.setManaged(false);
        labelInfo.setText(message);
        labelInfo.setVisible(true);
        labelInfo.setManaged(true);
    }

    private void masquerMessages() {
        labelErreur.setVisible(false); labelErreur.setManaged(false);
        labelInfo.setVisible(false); labelInfo.setManaged(false);
    }
}
