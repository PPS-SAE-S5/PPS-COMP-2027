package com.pps.parapente;

import com.pps.parapente.dao.DatabaseManager;
import com.pps.parapente.sync.SyncConfig;
import com.pps.parapente.sync.SyncService;
import com.pps.parapente.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.time.LocalDateTime;

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
        tenterSynchronisationAutomatique();
    }

    /**
     * Si la synchronisation Supabase est configurée ET que la case "synchronisation
     * automatique" est cochée, on tente une synchronisation en arrière-plan au démarrage.
     * Entièrement silencieux et sans bloquer l'ouverture de l'application : si l'appareil
     * est hors ligne ou que la configuration est absente, rien ne se passe (l'application
     * continue de fonctionner normalement en local, comme avant).
     */
    private void tenterSynchronisationAutomatique() {
        SyncConfig config = SyncConfig.charger();
        if (!config.estConfigure() || !config.isSyncAuto()) return;

        Thread tache = new Thread(() -> {
            try {
                new SyncService(config).synchroniser();
                config.setDerniereSync(LocalDateTime.now().format(SyncConfig.FORMAT_DATE));
                config.sauvegarder();
            } catch (Exception ignoree) {
                // Échec silencieux (pas d'internet, Supabase indisponible...) : l'utilisateur
                // peut toujours synchroniser manuellement depuis l'écran dédié.
            }
        }, "sync-auto-demarrage");
        tache.setDaemon(true);
        tache.start();
    }

    @Override
    public void stop() {
        // Rien de spécial : H2 flush et ferme le fichier automatiquement.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
