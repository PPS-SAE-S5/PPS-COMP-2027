package com.pps.parapente.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Centralise le chargement des vues FXML et la navigation dans la fenêtre principale. */
public final class SceneManager {

    private static Stage stagePrincipal;

    private SceneManager() {}

    public static void initialiser(Stage stage) {
        stagePrincipal = stage;
    }

    /** Change le contenu de la fenêtre principale par la vue FXML donnée, en conservant
     *  la taille et l'état (maximisée ou non) actuels de la fenêtre, pour éviter qu'elle
     *  ne change de taille à chaque changement d'écran. */
    public static void naviguerVers(String nomFxml, String titre) {
        try {
            boolean dejaAffichee = stagePrincipal.isShowing();
            double largeurPrecedente = stagePrincipal.getWidth();
            double hauteurPrecedente = stagePrincipal.getHeight();
            boolean etaitMaximisee = stagePrincipal.isMaximized();

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/pps/parapente/view/" + nomFxml));
            Parent racine = loader.load();
            Scene scene = new Scene(racine);
            scene.getStylesheets().add(SceneManager.class.getResource("/com/pps/parapente/view/style.css").toExternalForm());
            stagePrincipal.setScene(scene);
            stagePrincipal.setTitle("Championnat France Pompiers Parapente - " + titre);

            if (dejaAffichee) {
                // On réapplique la taille précédente : sans cela, JavaFX a tendance à
                // redimensionner la fenêtre selon la taille "préférée" de la nouvelle vue.
                if (etaitMaximisee) {
                    stagePrincipal.setMaximized(true);
                } else {
                    stagePrincipal.setWidth(largeurPrecedente);
                    stagePrincipal.setHeight(hauteurPrecedente);
                }
            }
            stagePrincipal.show();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la vue : " + nomFxml, e);
        }
    }

    /** Ouvre une fenêtre modale (dialogue) secondaire, ex: édition d'une épreuve. */
    public static <T> T ouvrirDialogue(String nomFxml, String titre) {
        return ouvrirDialogue(nomFxml, titre, null);
    }

    /**
     * Ouvre une fenêtre modale, en laissant la possibilité de configurer le contrôleur
     * (ex: pré-remplir un formulaire d'édition) AVANT que la fenêtre ne s'affiche et ne bloque.
     */
    @SuppressWarnings("unchecked")
    public static <T> T ouvrirDialogue(String nomFxml, String titre, java.util.function.Consumer<T> avantAffichage) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/pps/parapente/view/" + nomFxml));
            Parent racine = loader.load();
            Stage dialogue = new Stage();
            dialogue.setTitle(titre);
            Scene scene = new Scene(racine);
            scene.getStylesheets().add(SceneManager.class.getResource("/com/pps/parapente/view/style.css").toExternalForm());
            dialogue.setScene(scene);
            dialogue.initOwner(stagePrincipal);
            dialogue.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            T controleur = loader.getController();
            if (avantAffichage != null) avantAffichage.accept(controleur);

            dialogue.showAndWait();
            return controleur;
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ouvrir le dialogue : " + nomFxml, e);
        }
    }

    public static Stage getStagePrincipal() {
        return stagePrincipal;
    }
}
