package com.pps.parapente.controller;

import com.pps.parapente.sync.RapportSync;
import com.pps.parapente.sync.SyncConfig;
import com.pps.parapente.sync.SyncService;
import com.pps.parapente.util.MessageErreurUtil;
import com.pps.parapente.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;

public class SyncController {

    @FXML private TextField champUrl;
    @FXML private TextField champCle;
    @FXML private CheckBox checkAuto;
    @FXML private Label labelDerniereSync;
    @FXML private Label labelErreur;
    @FXML private TextArea zoneResultat;
    @FXML private ProgressIndicator indicateur;

    private SyncConfig config;

    @FXML
    public void initialize() {
        config = SyncConfig.charger();
        champUrl.setText(config.getUrl());
        champCle.setText(config.getCleAnon());
        checkAuto.setSelected(config.isSyncAuto());
        afficherDerniereSync();
    }

    private void afficherDerniereSync() {
        labelDerniereSync.setText(
                config.getDerniereSync() == null || config.getDerniereSync().isBlank()
                        ? "Aucune synchronisation effectuée depuis cet appareil pour le moment."
                        : "Dernière synchronisation réussie : " + config.getDerniereSync()
        );
    }

    @FXML
    private void enregistrerConfig() {
        config.setUrl(champUrl.getText());
        config.setCleAnon(champCle.getText());
        config.setSyncAuto(checkAuto.isSelected());
        try {
            config.sauvegarder();
            masquerMessages();
            afficherInfo("Configuration enregistrée.");
        } catch (Exception e) {
            afficherErreur(MessageErreurUtil.traduire(e));
        }
    }

    @FXML
    private void tester() {
        enregistrerConfig();
        masquerMessages();
        indicateur.setVisible(true);
        indicateur.setManaged(true);
        new Thread(() -> {
            try {
                new SyncService(config).testerConnexion();
                Platform.runLater(() -> {
                    indicateur.setVisible(false); indicateur.setManaged(false);
                    afficherInfo("Connexion réussie : la base Supabase est accessible.");
                });
            } catch (Exception e) {
                String message = MessageErreurUtil.traduire(e);
                Platform.runLater(() -> {
                    indicateur.setVisible(false); indicateur.setManaged(false);
                    afficherErreur(message);
                });
            }
        }, "test-connexion-supabase").start();
    }

    /** Fusion dans les deux sens, sans aucune suppression (ancien bouton "Synchroniser maintenant"). */
    @FXML
    private void synchroniser() {
        lancer("synchronisation-supabase", "Synchronisation terminée.", () -> new SyncService(config).synchroniser());
    }

    /** Base en ligne -> application ("git pull") : l'application devient identique à la base en ligne. */
    @FXML
    private void synchroniserVersAppli() {
        if (!confirmer("Synchroniser : base en ligne → application",
                "Votre application va devenir IDENTIQUE à la base en ligne.\n\n" +
                "Les pilotes, épreuves, inscriptions et résultats qui n'existent pas en ligne " +
                "seront SUPPRIMÉS de cet ordinateur.\n\nContinuer ?")) return;
        lancer("sync-base-vers-appli", "Réception terminée : l'application est maintenant identique à la base en ligne.",
                () -> new SyncService(config).recevoirDepuisSupabase());
    }

    /** Application -> base en ligne ("git push") : la base en ligne devient identique à l'application. */
    @FXML
    private void synchroniserVersBase() {
        if (!confirmer("Synchroniser : application → base en ligne",
                "La base en ligne va devenir IDENTIQUE à votre application.\n\n" +
                "Les pilotes, épreuves, inscriptions et résultats qui n'existent pas dans cette application " +
                "seront SUPPRIMÉS de la base en ligne, donc pour tous les ordinateurs.\n\nContinuer ?")) return;
        lancer("sync-appli-vers-base", "Envoi terminé : la base en ligne est maintenant identique à l'application.",
                () -> new SyncService(config).envoyerVersSupabase());
    }

    private boolean confirmer(String titre, String message) {
        Alert alerte = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alerte.setTitle("Confirmation");
        alerte.setHeaderText(titre);
        alerte.initOwner(SceneManager.getStagePrincipal());
        return alerte.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    @FunctionalInterface
    private interface TacheSync {
        RapportSync executer() throws Exception;
    }

    /** Exécute une synchronisation en arrière-plan puis affiche le résumé (ou l'erreur) à l'écran. */
    private void lancer(String nomThread, String titreSucces, TacheSync tache) {
        enregistrerConfig();
        masquerMessages();
        indicateur.setVisible(true);
        indicateur.setManaged(true);
        new Thread(() -> {
            try {
                RapportSync rapport = tache.executer();
                config.setDerniereSync(LocalDateTime.now().format(SyncConfig.FORMAT_DATE));
                config.sauvegarder();
                Platform.runLater(() -> {
                    indicateur.setVisible(false); indicateur.setManaged(false);
                    afficherResultat(titreSucces, rapport.resume());
                    afficherDerniereSync();
                });
            } catch (Exception e) {
                String message = MessageErreurUtil.traduire(e);
                Platform.runLater(() -> {
                    indicateur.setVisible(false); indicateur.setManaged(false);
                    afficherErreur(message);
                });
            }
        }, nomThread).start();
    }

    private void afficherInfo(String message) {
        zoneResultat.setText(message);
        zoneResultat.setVisible(true);
        zoneResultat.setManaged(true);
    }

    private void afficherResultat(String titre, String texte) {
        zoneResultat.setText(titre + "\n\n" + texte);
        zoneResultat.setVisible(true);
        zoneResultat.setManaged(true);
    }

    private void afficherErreur(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }

    private void masquerMessages() {
        labelErreur.setVisible(false); labelErreur.setManaged(false);
        zoneResultat.setVisible(false); zoneResultat.setManaged(false);
    }

    @FXML
    private void retour() {
        SceneManager.naviguerVers("dashboard.fxml", "Tableau de bord");
    }
}
