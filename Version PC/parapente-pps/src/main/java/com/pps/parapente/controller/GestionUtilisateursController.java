package com.pps.parapente.controller;

import com.pps.parapente.dao.UtilisateurDAO;
import com.pps.parapente.model.Role;
import com.pps.parapente.model.Utilisateur;
import com.pps.parapente.util.MessageErreurUtil;
import com.pps.parapente.util.SceneManager;
import com.pps.parapente.util.SecuriteUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class GestionUtilisateursController {

    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, String> colIdentifiant;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colActif;

    @FXML private TextField champIdentifiant;
    @FXML private PasswordField champMotDePasse;
    @FXML private ComboBox<Role> champRole;
    @FXML private Label labelErreur;
    @FXML private Label labelInfo;

    @FXML private Label labelCompteSelectionne;
    @FXML private Button boutonActiver;
    @FXML private PasswordField champNouveauMotDePasse;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final ObservableList<Utilisateur> donnees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colIdentifiant.setCellValueFactory(new PropertyValueFactory<>("identifiant"));
        colRole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRole().getLibelle()));
        colActif.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isActif() ? "Oui" : "Non"));
        tableUtilisateurs.setItems(donnees);

        champRole.setItems(FXCollections.observableArrayList(Role.values()));

        tableUtilisateurs.getSelectionModel().selectedItemProperty().addListener((obs, ancien, nouveau) -> afficherSelection(nouveau));
        afficherSelection(null);

        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            donnees.setAll(utilisateurDAO.listerTous());
            // Recharge aussi la sélection affichée (son statut actif a pu changer)
            Utilisateur selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
            afficherSelection(selection);
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement : " + MessageErreurUtil.traduire(e));
        }
    }

    private void afficherSelection(Utilisateur u) {
        if (u == null) {
            labelCompteSelectionne.setText("Sélectionnez un compte dans le tableau pour l'activer/désactiver ou changer son mot de passe.");
            boutonActiver.setText("Activer / Désactiver le compte");
            boutonActiver.setDisable(true);
            champNouveauMotDePasse.setDisable(true);
        } else {
            labelCompteSelectionne.setText(u.getIdentifiant() + " (" + u.getRole().getLibelle() + ") — " +
                    (u.isActif() ? "compte actif" : "compte désactivé"));
            boutonActiver.setText(u.isActif() ? "Désactiver ce compte" : "Réactiver ce compte");
            boutonActiver.setDisable(false);
            champNouveauMotDePasse.setDisable(false);
        }
    }

    @FXML
    private void creerCompte() {
        String identifiant = champIdentifiant.getText().trim();
        String motDePasse = champMotDePasse.getText();
        Role role = champRole.getValue();

        if (identifiant.isEmpty() || motDePasse.isEmpty() || role == null) {
            afficherErreur("Identifiant, mot de passe et rôle sont obligatoires.");
            return;
        }
        try {
            Utilisateur u = new Utilisateur(identifiant, SecuriteUtil.hacher(motDePasse), role);
            utilisateurDAO.creer(u);
            chargerDonnees();
            champIdentifiant.clear();
            champMotDePasse.clear();
            champRole.setValue(null);
            masquerMessages();
        } catch (SQLException e) {
            afficherErreur("Erreur (identifiant déjà utilisé ?) : " + MessageErreurUtil.traduire(e));
        }
    }

    /** Active ou désactive le compte sélectionné (un compte désactivé ne peut plus se connecter). */
    @FXML
    private void basculerActif() {
        Utilisateur selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherErreur("Sélectionnez d'abord un compte.");
            return;
        }
        if ("admin".equals(selection.getIdentifiant())) {
            afficherErreur("Le compte administrateur par défaut ne peut pas être désactivé.");
            return;
        }
        try {
            boolean nouvelEtat = !selection.isActif();
            utilisateurDAO.definirActif(selection.getId(), nouvelEtat);
            masquerMessages();
            afficherInfo(selection.getIdentifiant() + " a été " + (nouvelEtat ? "réactivé." : "désactivé."));
            chargerDonnees();
        } catch (SQLException e) {
            afficherErreur("Erreur : " + MessageErreurUtil.traduire(e));
        }
    }

    /** Change le mot de passe du compte sélectionné (l'identifiant et le rôle ne changent pas). */
    @FXML
    private void changerMotDePasse() {
        Utilisateur selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherErreur("Sélectionnez d'abord un compte.");
            return;
        }
        String nouveau = champNouveauMotDePasse.getText();
        if (nouveau == null || nouveau.isEmpty()) {
            afficherErreur("Saisissez le nouveau mot de passe dans le champ prévu.");
            return;
        }
        try {
            utilisateurDAO.mettreAJourMotDePasse(selection.getId(), SecuriteUtil.hacher(nouveau));
            champNouveauMotDePasse.clear();
            masquerMessages();
            afficherInfo("Mot de passe mis à jour pour " + selection.getIdentifiant() + ".");
        } catch (SQLException e) {
            afficherErreur("Erreur : " + MessageErreurUtil.traduire(e));
        }
    }

    @FXML
    private void supprimerCompte() {
        Utilisateur selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherErreur("Sélectionnez un compte à supprimer.");
            return;
        }
        if ("admin".equals(selection.getIdentifiant())) {
            afficherErreur("Le compte administrateur par défaut ne peut pas être supprimé.");
            return;
        }
        try {
            utilisateurDAO.supprimer(selection.getId());
            masquerMessages();
            chargerDonnees();
        } catch (SQLException e) {
            afficherErreur("Erreur de suppression : " + MessageErreurUtil.traduire(e));
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
