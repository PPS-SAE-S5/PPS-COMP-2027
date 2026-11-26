package com.pps.parapente.controller;

import com.pps.parapente.dao.HistoriqueDAO;
import com.pps.parapente.model.HistoriquePoint;
import com.pps.parapente.model.Role;
import com.pps.parapente.model.TypeActionPoints;
import com.pps.parapente.model.Utilisateur;
import com.pps.parapente.util.MessageErreurUtil;
import com.pps.parapente.util.SceneManager;
import com.pps.parapente.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Consultation de l'historique des modifications de points (US #13).
 * Écran en LECTURE SEULE : aucune action ne permet de modifier ou supprimer une ligne.
 * Accessible uniquement à l'administrateur et au comité des pilotes.
 */
public class HistoriquePointsController {

    private static final String TOUTES_LES_EPREUVES = "Toutes les épreuves";
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML private ComboBox<String> comboEpreuve;
    @FXML private TextField champRecherche;
    @FXML private CheckBox checkMasquerRecalculs;

    @FXML private TableView<HistoriquePoint> tableHistorique;
    @FXML private TableColumn<HistoriquePoint, String> colDate;
    @FXML private TableColumn<HistoriquePoint, String> colAction;
    @FXML private TableColumn<HistoriquePoint, String> colMessage;
    @FXML private TableColumn<HistoriquePoint, String> colEcart;
    @FXML private TableColumn<HistoriquePoint, String> colTotal;

    @FXML private Label labelErreur;
    @FXML private Label labelNbLignes;

    private final HistoriqueDAO historiqueDAO = new HistoriqueDAO();
    private final ObservableList<HistoriquePoint> donnees = FXCollections.observableArrayList();
    private final FilteredList<HistoriquePoint> donneesFiltrees = new FilteredList<>(donnees, h -> true);

    /** Rôles autorisés à consulter l'historique. */
    public static boolean estAutorise(Role role) {
        return role == Role.ADMINISTRATEUR || role == Role.COMITE_PILOTES;
    }

    @FXML
    public void initialize() {
        Utilisateur u = SessionManager.getUtilisateurConnecte();
        if (u == null || !estAutorise(u.getRole())) {
            // Sécurité : même si l'écran est ouvert autrement que par le tableau de bord, rien n'est chargé
            afficherErreur("Accès réservé à l'administrateur et au comité des pilotes.");
            tableHistorique.setDisable(true);
            comboEpreuve.setDisable(true);
            champRecherche.setDisable(true);
            checkMasquerRecalculs.setDisable(true);
            return;
        }

        colDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateAction() != null ? c.getValue().getDateAction().format(FORMAT_DATE) : ""));
        colAction.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTypeAction() != null ? c.getValue().getTypeAction().getLibelle() : ""));
        colMessage.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMessage()));
        colEcart.setCellValueFactory(c -> new SimpleStringProperty(formaterEcart(c.getValue().getEcart())));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPointsApres() != null ? HistoriquePoint.formaterPoints(c.getValue().getPointsApres()) : ""));

        // Écart en vert (gain) ou rouge (perte)
        colEcart.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String texte, boolean vide) {
                super.updateItem(texte, vide);
                if (vide || texte == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(texte);
                    if (texte.startsWith("+")) setStyle("-fx-text-fill:#27ae60; -fx-font-weight:bold;");
                    else if (texte.startsWith("-")) setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold;");
                    else setStyle("");
                }
            }
        });

        // Le message complet en infobulle (utile quand la colonne est trop étroite)
        colMessage.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String texte, boolean vide) {
                super.updateItem(texte, vide);
                setText(vide ? null : texte);
                setTooltip(vide || texte == null ? null : new Tooltip(texte));
            }
        });

        tableHistorique.setItems(donneesFiltrees);
        tableHistorique.setEditable(false);

        comboEpreuve.valueProperty().addListener((obs, a, n) -> appliquerFiltres());
        champRecherche.textProperty().addListener((obs, a, n) -> appliquerFiltres());
        checkMasquerRecalculs.selectedProperty().addListener((obs, a, n) -> appliquerFiltres());

        chargerDonnees();
    }

    private void chargerDonnees() {
        Utilisateur u = SessionManager.getUtilisateurConnecte();
        if (u == null || !estAutorise(u.getRole())) return;
        masquerErreur();
        try {
            donnees.setAll(historiqueDAO.listerTous());

            // Liste des épreuves présentes dans l'historique (y compris des épreuves supprimées depuis)
            String selection = comboEpreuve.getValue();
            TreeSet<String> noms = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            for (HistoriquePoint h : donnees) {
                if (h.getEpreuveNom() != null && !h.getEpreuveNom().isBlank()) noms.add(h.getEpreuveNom());
            }
            List<String> choix = new ArrayList<>();
            choix.add(TOUTES_LES_EPREUVES);
            choix.addAll(noms);
            comboEpreuve.setItems(FXCollections.observableArrayList(choix));
            comboEpreuve.setValue(selection != null && choix.contains(selection) ? selection : TOUTES_LES_EPREUVES);

            appliquerFiltres();
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement de l'historique : " + MessageErreurUtil.traduire(e));
        }
    }

    private void appliquerFiltres() {
        String epreuve = comboEpreuve.getValue();
        String recherche = champRecherche.getText() == null ? "" : champRecherche.getText().trim().toLowerCase(Locale.ROOT);
        boolean masquerRecalculs = checkMasquerRecalculs.isSelected();

        donneesFiltrees.setPredicate(h -> {
            if (masquerRecalculs && h.getTypeAction() == TypeActionPoints.RECALCUL_BAREME) return false;
            if (epreuve != null && !TOUTES_LES_EPREUVES.equals(epreuve) && !epreuve.equals(h.getEpreuveNom())) return false;
            if (!recherche.isEmpty()) {
                String nom = h.getPiloteNom() != null ? h.getPiloteNom().toLowerCase(Locale.ROOT) : "";
                String licence = h.getPiloteLicence() != null ? h.getPiloteLicence().toLowerCase(Locale.ROOT) : "";
                return nom.contains(recherche) || licence.contains(recherche);
            }
            return true;
        });
        labelNbLignes.setText(donneesFiltrees.size() + " ligne(s) affichée(s) sur " + donnees.size());
    }

    private static String formaterEcart(double ecart) {
        if (ecart > 0) return "+" + HistoriquePoint.formaterPoints(ecart);
        if (ecart < 0) return "-" + HistoriquePoint.formaterPoints(-ecart);
        return "0";
    }

    @FXML
    private void actualiser() {
        chargerDonnees();
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
