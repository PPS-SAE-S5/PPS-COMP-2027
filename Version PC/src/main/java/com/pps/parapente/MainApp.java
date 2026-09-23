package com.pps.parapente;

import com.pps.parapente.dao.DatabaseManager;
import com.pps.parapente.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée de l'application - Gestion du Championnat de France Pompiers de Parapente.
 * Données stockées EN LOCAL (base H2 fichier dans ./data), utilisable sans connexion internet.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stagePrincipal) {
        DatabaseManager.initialiserSchema();
        SceneManager.initialiser(stagePrincipal);
        SceneManager.naviguerVers("login.fxml", "Connexion");
        stagePrincipal.setMinWidth(900);
        stagePrincipal.setMinHeight(600);
    }

    @Override
    public void stop() {
        // Rien de spécial : H2 flush et ferme le fichier automatiquement.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
