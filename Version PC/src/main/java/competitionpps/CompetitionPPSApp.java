package competitionpps;
	
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
// import java.io.InputStream;
// import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import competitionpps.model.EtatMembre;
import competitionpps.model.Membre;
import competitionpps.view.ListeMembresController;
import competitionpps.view.SaisieMembreController;
import javafx.application.Application;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class CompetitionPPSApp extends Application {
	
	private BorderPane rootPane;
	private Stage primaryStage;
	
    private static final Path data_local = Paths.get("data", "Membres.txt");
    private List<Membre> listeMembres = new ArrayList<>();
    
    public CompetitionPPSApp() {
        loadMembres();
    }

    private void loadMembres() {

    if (!Files.exists(data_local)) {
        return;
    }

    try (BufferedReader reader = Files.newBufferedReader(data_local, StandardCharsets.UTF_8)) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(";");
            if (parts.length < 6) continue;

            String nom    = parts[0].trim();
            String prenom = parts[1].trim();
            EtatMembre etat = EtatMembre.valueOf(parts[2].trim());
            String ville  = parts[3].trim();

            String[] dd = parts[4].split(",");
            int year  = Integer.parseInt(dd[0].trim());
            int month = Integer.parseInt(dd[1].trim());
            int day   = Integer.parseInt(dd[2].trim());
            LocalDate dateInscription = LocalDate.of(year, month, day);

            String notes = parts[5].trim();
            listeMembres.add(new Membre(nom, prenom, etat, ville, dateInscription, notes));
        }
    } catch (IOException e) {
        // e.printStackTrace();
    }
	}


	/** private final ObservableList<Membre> listeMembres = FXCollections.observableArrayList();

	// Ancienne version pour créer des membres placeholder

	public CompetitionPPSApp() {
		// Création de quelques membres de départ dans liste Membre
		// code temporaire en attendant d'avoir les méthodes le sauvegarde/restauration en base ou dans des fichiers.
		this.listeMembres.add(new Membre("Floraville", "Rose", EtatMembre.Membre, "Toulouse", LocalDate.of(1985, 11, 13), "Amis de Tom" ));
        this.listeMembres.add(new Membre("Brown", "Emet", EtatMembre.Ancien, "Paris", LocalDate.of(1855, 9, 12), "Chercheur fou" ));
		this.listeMembres.add(new Membre("D'Idrila", "Argenti", EtatMembre.Ancien, "Espace", LocalDate.of(1310, 2, 14), "Chevalier de la beauté" ));
	}
	*/


	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		this.rootPane = new BorderPane();
		
		Scene scene = new Scene(rootPane);
		scene.getStylesheets().add(CompetitionPPSApp.class.getResource("style.css").toExternalForm());
		primaryStage.setTitle("CompetitionPPS App");
		primaryStage.setScene(scene);

		loadListeMembre();

		primaryStage.show();		
		
	}
	
	public void loadListeMembre() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation( CompetitionPPSApp.class.getResource("view/ListeMembres.fxml"));
			
			BorderPane vueListe = loader.load();
			
			ListeMembresController ctrl = loader.getController();
			ctrl.setListeMembre( this.listeMembres );
			ctrl.setGetClubApp(this);
			ctrl.initialisation();
			
			this.rootPane.setCenter( vueListe );
						
		} catch (IOException e) {
			System.out.println("Ressource FXML non disponible : ListeMembres");
			System.exit(1);
		}	
	}
	
	public Membre showSaisieMembre() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation( CompetitionPPSApp.class.getResource("view/SaisieMembre.fxml"));
			
			BorderPane vueSaisie = loader.load();
			
			Scene scene = new Scene(vueSaisie);
			scene.getStylesheets().setAll( primaryStage.getScene().getStylesheets() );
			
			Stage dialogStage =new Stage();
			dialogStage.setTitle("Edition membre");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.primaryStage);
			dialogStage.setScene(scene);
			
			SaisieMembreController ctrl = loader.getController();
			ctrl.setDialogStage(dialogStage);
			
			dialogStage.showAndWait();
			if(ctrl.getMembre()!=null)this.listeMembres.add(ctrl.getMembre());
			return ctrl.getMembre();
						
		} catch (IOException e) {
			System.out.println("Ressource FXML non disponible : SaisieMembres");
			System.exit(1);
			return null;
		}	
		
	}

	public Membre editSaisieMembre(Membre membre) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation( CompetitionPPSApp.class.getResource("view/SaisieMembre.fxml"));
			
			BorderPane vueSaisie = loader.load();
			
			Scene scene = new Scene(vueSaisie);
			scene.getStylesheets().setAll( primaryStage.getScene().getStylesheets() );
			
			Stage dialogStage =new Stage();
			dialogStage.setTitle("Edition membre");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.primaryStage);
			dialogStage.setScene(scene);
			
			SaisieMembreController ctrl = loader.getController();
			ctrl.setDialogStage(dialogStage);

			ctrl.setMembre(membre);
			dialogStage.showAndWait();

			return ctrl.getMembre();
						
		} catch (IOException e) {
			System.out.println("Ressource FXML non disponible : SaisieMembres");
			System.exit(1);
			return null;
		}	
	}
	
	public void saveLocal() {
    try {
        Files.createDirectories(data_local.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(
                  data_local,
                  StandardCharsets.UTF_8,
                  StandardOpenOption.CREATE,
                  StandardOpenOption.TRUNCATE_EXISTING)) {
            
				DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy,MM,dd");
            

            for (Membre m : listeMembres) {
                String line = "" +
                    m.getNom() +";" +
                    m.getPrenom() +";" +
                    m.getEtat() +";" +
                    m.getVille() +";" +
                    m.getDateInscription().format(fmt) +";" +
                    m.getNotes()
                ;
                writer.write(line);
                writer.newLine();
            }
        }
    } catch (IOException e) {
        // e.printStackTrace();
    }
}
	
	@Override
	public void stop() throws Exception {
		this.primaryStage.close();
	}

	public void remove(int index){
		this.listeMembres.remove(index);
	}

	public void retraitDB(int index) {
    	listeMembres.remove(index);
    	saveLocal();
	}
	
	public static void main2(String[] args) {
		launch(args);
	}
}
