package com.pps.parapente.controller;

import com.pps.parapente.model.Atelier;
import com.pps.parapente.model.Creneau;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Journee;
import com.pps.parapente.model.Role;
import com.pps.parapente.model.Utilisateur;
import com.pps.parapente.service.PlanningService;
import com.pps.parapente.util.MessageErreurUtil;
import com.pps.parapente.util.SceneManager;
import com.pps.parapente.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Planning de la compétition : journées (onglets) > créneaux (matin / après-midi) > ateliers > épreuves.
 * Seuls l'administrateur et le responsable de l'épreuve peuvent modifier le planning ;
 * les autres rôles le consultent en lecture seule.
 */
public class PlanningController {

    @FXML private Button boutonNouvelleJournee;
    @FXML private Button boutonNouvelAtelier;
    @FXML private FlowPane ongletsJournees;
    @FXML private HBox actionsJournee;
    @FXML private Label labelJournee;
    @FXML private VBox contenuJournee;
    @FXML private ListView<Epreuve> listeNonAffectees;
    @FXML private Label labelResume;

    private final PlanningService planningService = new PlanningService();
    private boolean peutModifier;
    private int journeeSelectionneeId = 0;

    @FXML
    public void initialize() {
        Utilisateur u = SessionManager.getUtilisateurConnecte();
        if (u == null) {
            SceneManager.naviguerVers("login.fxml", "Connexion");
            return;
        }
        peutModifier = u.getRole() == Role.ADMINISTRATEUR || u.getRole() == Role.RESPONSABLE_EPREUVE;
        if (!peutModifier) {
            masquer(boutonNouvelleJournee, boutonNouvelAtelier);
        }
        rafraichir();
    }

    private void rafraichir() {
        try {
            List<Journee> journees = planningService.listerJournees();
            if (journeeSelectionneeId == 0 || journees.stream().noneMatch(j -> j.getId() == journeeSelectionneeId)) {
                journeeSelectionneeId = journees.isEmpty() ? 0 : journees.get(0).getId();
            }
            construireOnglets(journees);
            construireContenuJournee(journees);
            construirePanneauEpreuves();
        } catch (SQLException e) {
            erreur("Erreur de chargement : " + MessageErreurUtil.traduire(e));
        }
    }

    // onglets des journées

    private void construireOnglets(List<Journee> journees) {
        ongletsJournees.getChildren().clear();
        ToggleGroup groupe = new ToggleGroup();
        for (Journee j : journees) {
            String texte = j.getNom() + (j.getDateJournee() == null ? "" : "\n" + j.getDateFormatee());
            ToggleButton onglet = new ToggleButton(texte);
            onglet.getStyleClass().add("onglet-journee");
            onglet.setToggleGroup(groupe);
            onglet.setSelected(j.getId() == journeeSelectionneeId);
            onglet.setOnAction(ev -> {
                journeeSelectionneeId = j.getId();
                rafraichir();
            });
            ongletsJournees.getChildren().add(onglet);
        }
        // Empêche de désélectionner l'onglet actif en recliquant dessus
        groupe.selectedToggleProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau == null && ancien != null) ancien.setSelected(true);
        });
    }

    // contenu de la journée sélectionnée

    private void construireContenuJournee(List<Journee> journees) throws SQLException {
        contenuJournee.getChildren().clear();
        Journee courante = journees.stream().filter(j -> j.getId() == journeeSelectionneeId).findFirst().orElse(null);

        if (courante == null) {
            actionsJournee.setVisible(false);
            actionsJournee.setManaged(false);
            Label vide = new Label(peutModifier
                    ? "Aucune journée pour le moment. Cliquez sur « + Nouvelle journée » pour commencer à organiser la compétition."
                    : "Le planning n'a pas encore été créé.");
            vide.setWrapText(true);
            vide.getStyleClass().add("etat-vide");
            contenuJournee.getChildren().add(vide);
            return;
        }

        actionsJournee.setVisible(true);
        actionsJournee.setManaged(true);
        labelJournee.setText(courante.getNom() + (courante.getDateJournee() == null ? "" : " · " + courante.getDateFormatee()));
        if (!peutModifier) {
            // Lecture seule : on garde le titre, on retire les boutons d'action
            actionsJournee.getChildren().stream().filter(n -> n instanceof Button).forEach(n -> masquer((Button) n));
        }

        List<Atelier> ateliers = planningService.listerAteliers(courante.getId());
        for (Creneau creneau : Creneau.values()) {
            List<Atelier> duCreneau = ateliers.stream().filter(a -> a.getCreneau() == creneau).toList();
            contenuJournee.getChildren().add(construireCreneau(creneau, duCreneau));
        }
    }

    private VBox construireCreneau(Creneau creneau, List<Atelier> ateliers) {
        VBox bloc = new VBox(10);

        Label pastille = new Label(creneau.getLibelle());
        pastille.getStyleClass().addAll("pastille-creneau", creneau == Creneau.MATIN ? "pastille-matin" : "pastille-apres-midi");
        String compte = ateliers.isEmpty() ? "aucun atelier" : ateliers.size() + (ateliers.size() > 1 ? " ateliers" : " atelier");
        Label info = new Label(compte);
        info.setStyle("-fx-text-fill:#566779;");
        HBox entete = new HBox(12, pastille, info);
        entete.setAlignment(Pos.CENTER_LEFT);
        bloc.getChildren().add(entete);

        if (ateliers.isEmpty()) {
            Label vide = new Label("Aucun atelier sur ce créneau.");
            vide.getStyleClass().add("etat-vide");
            vide.setMaxWidth(Double.MAX_VALUE);
            bloc.getChildren().add(vide);
        }
        for (Atelier a : ateliers) {
            bloc.getChildren().add(construireCarteAtelier(a));
        }
        return bloc;
    }

    private VBox construireCarteAtelier(Atelier a) {
        VBox carte = new VBox(10);
        carte.getStyleClass().add("carte-atelier");

        Label nom = new Label(a.getNom());
        nom.getStyleClass().add("sous-titre");
        nom.setWrapText(true);

        StringBuilder meta = new StringBuilder();
        ajouterMeta(meta, a.getPlageHoraire());
        ajouterMeta(meta, a.getLieu());
        if (a.getResponsable() != null && !a.getResponsable().isBlank()) ajouterMeta(meta, "Resp. " + a.getResponsable());
        Label detail = new Label(meta.toString());
        detail.setStyle("-fx-text-fill:#566779; -fx-font-size:12px;");
        detail.setWrapText(true);

        VBox titres = new VBox(4, nom, detail);
        if (meta.length() == 0) titres.getChildren().remove(detail);
        HBox.setHgrow(titres, Priority.ALWAYS);

        HBox entete = new HBox(10, titres);
        entete.setAlignment(Pos.TOP_LEFT);
        if (peutModifier) {
            Button modifier = new Button("Modifier");
            modifier.getStyleClass().add("bouton-secondaire");
            modifier.setOnAction(ev -> modifierAtelier(a));
            Button supprimer = new Button("Supprimer");
            supprimer.getStyleClass().add("bouton-danger");
            supprimer.setOnAction(ev -> supprimerAtelier(a));
            entete.getChildren().addAll(modifier, supprimer);
        }
        carte.getChildren().add(entete);

        if (a.getEpreuves().isEmpty()) {
            Label vide = new Label("Aucune épreuve dans cet atelier.");
            vide.setStyle("-fx-text-fill:#7f8c8d; -fx-font-size:12px;");
            carte.getChildren().add(vide);
        } else {
            VBox lignes = new VBox(6);
            int n = 1;
            for (Epreuve e : a.getEpreuves()) {
                Label numero = new Label(String.valueOf(n++));
                numero.getStyleClass().add("pastille-numero");
                Label libelle = new Label(e.getNom());
                HBox ligne = new HBox(10, numero, libelle);
                ligne.setAlignment(Pos.CENTER_LEFT);
                ligne.getStyleClass().add("ligne-epreuve");
                lignes.getChildren().add(ligne);
            }
            carte.getChildren().add(lignes);
        }
        return carte;
    }

    private void ajouterMeta(StringBuilder sb, String texte) {
        if (texte == null || texte.isBlank()) return;
        if (sb.length() > 0) sb.append("   ·   ");
        sb.append(texte);
    }

    // panneau "épreuves à placer"

    private void construirePanneauEpreuves() throws SQLException {
        List<Epreuve> libres = planningService.listerEpreuvesNonAffectees();
        listeNonAffectees.setItems(FXCollections.observableArrayList(libres));
        listeNonAffectees.setPlaceholder(new Label("Toutes les épreuves sont placées."));
        int total = planningService.compterEpreuvesActives();
        labelResume.setText(total + (total > 1 ? " épreuves actives" : " épreuve active") + " · " + libres.size() + " à placer");
    }

    // actions : journées

    @FXML
    private void nouvelleJournee() {
        Optional<Journee> saisie = demanderJournee(null);
        saisie.ifPresent(j -> {
            try {
                planningService.creerJournee(j);
                journeeSelectionneeId = j.getId();
                rafraichir();
            } catch (Exception e) {
                erreur(MessageErreurUtil.traduire(e));
            }
        });
    }

    @FXML
    private void modifierJournee() {
        try {
            Journee courante = planningService.listerJournees().stream()
                    .filter(j -> j.getId() == journeeSelectionneeId).findFirst().orElse(null);
            if (courante == null) return;
            demanderJournee(courante).ifPresent(j -> {
                try {
                    planningService.mettreAJourJournee(j);
                    rafraichir();
                } catch (Exception e) {
                    erreur(MessageErreurUtil.traduire(e));
                }
            });
        } catch (SQLException e) {
            erreur(MessageErreurUtil.traduire(e));
        }
    }

    @FXML
    private void supprimerJournee() {
        if (journeeSelectionneeId == 0) return;
        try {
            int nbAteliers = planningService.compterAteliers(journeeSelectionneeId);
            String message = nbAteliers == 0
                    ? "Supprimer cette journée ?"
                    : "Supprimer cette journée et ses " + nbAteliers + " atelier(s) ?\nLes épreuves ne sont pas supprimées : elles redeviendront « à placer ».";
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
            confirmation.showAndWait().ifPresent(rep -> {
                if (rep == ButtonType.YES) {
                    try {
                        planningService.supprimerJournee(journeeSelectionneeId);
                        journeeSelectionneeId = 0;
                        rafraichir();
                    } catch (SQLException e) {
                        erreur("Erreur de suppression : " + MessageErreurUtil.traduire(e));
                    }
                }
            });
        } catch (SQLException e) {
            erreur(MessageErreurUtil.traduire(e));
        }
    }

    /** Petite boîte de dialogue : nom + date. Renvoie la journée saisie (ou vide si annulé). */
    private Optional<Journee> demanderJournee(Journee existante) {
        Dialog<ButtonType> dialogue = new Dialog<>();
        dialogue.setTitle(existante == null ? "Nouvelle journée" : "Modifier la journée");
        dialogue.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogue.getDialogPane().getStylesheets().add(getClass().getResource("/com/pps/parapente/view/style.css").toExternalForm());

        TextField nom = new TextField(existante != null ? existante.getNom() : "Journée " + (journeeSuivante()));
        DatePicker date = new DatePicker(existante != null ? existante.getDateJournee() : null);
        date.setPromptText("jj/mm/aaaa (facultatif)");

        GridPane grille = new GridPane();
        grille.setHgap(10);
        grille.setVgap(10);
        grille.setPadding(new Insets(16));
        grille.addRow(0, new Label("Nom *"), nom);
        grille.addRow(1, new Label("Date"), date);
        dialogue.getDialogPane().setContent(grille);

        Optional<ButtonType> rep = dialogue.showAndWait();
        if (rep.isEmpty() || rep.get() != ButtonType.OK) return Optional.empty();

        Journee j = existante != null ? existante : new Journee();
        j.setNom(nom.getText());
        LocalDate d = date.getValue();
        j.setDateJournee(d);
        return Optional.of(j);
    }

    private int journeeSuivante() {
        try {
            return planningService.listerJournees().size() + 1;
        } catch (SQLException e) {
            return 1;
        }
    }

    // actions : ateliers

    @FXML
    private void nouvelAtelier() {
        try {
            if (planningService.listerJournees().isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Créez d'abord une journée : un atelier doit être rattaché à une journée.").showAndWait();
                return;
            }
        } catch (SQLException e) {
            erreur(MessageErreurUtil.traduire(e));
            return;
        }
        Integer journeeId = journeeSelectionneeId == 0 ? null : journeeSelectionneeId;
        AtelierEditController c = SceneManager.ouvrirDialogue("atelier_edit.fxml", "Nouvel atelier",
                (AtelierEditController ctrl) -> ctrl.preparer(null, journeeId));
        apresEdition(c);
    }

    private void modifierAtelier(Atelier a) {
        AtelierEditController c = SceneManager.ouvrirDialogue("atelier_edit.fxml", "Modifier l'atelier",
                (AtelierEditController ctrl) -> ctrl.preparer(a, null));
        apresEdition(c);
    }

    private void apresEdition(AtelierEditController c) {
        if (c != null && c.isEnregistre()) rafraichir();
    }

    private void supprimerAtelier(Atelier a) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'atelier \"" + a.getNom() + "\" ?\nSes épreuves ne sont pas supprimées : elles redeviendront « à placer ».",
                ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.YES) {
                try {
                    planningService.supprimerAtelier(a.getId());
                    rafraichir();
                } catch (SQLException e) {
                    erreur("Erreur de suppression : " + MessageErreurUtil.traduire(e));
                }
            }
        });
    }

    // divers

    @FXML
    private void retour() {
        SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
    }

    private void masquer(Node... noeuds) {
        for (Node n : noeuds) {
            n.setVisible(false);
            n.setManaged(false);
        }
    }

    private void erreur(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}
