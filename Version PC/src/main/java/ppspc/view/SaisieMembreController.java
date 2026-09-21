package gestclub.view;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

import gestclub.model.EtatMembre;
import gestclub.model.Membre;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SaisieMembreController implements Initializable {
	
	private Stage  dialogStage;
	private Membre membre;
	
	@FXML
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
	private TextField txtVille;
	
	@FXML
	private RadioButton radioButProspect;
	@FXML
	private RadioButton radioButMembre;
	@FXML
	private RadioButton radioButAncien;
	
	@FXML
	private DatePicker datePickInscription;
	
	@FXML
	private TextArea  txtNotes;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

    private static final Path data_local = Paths.get("data", "Membres.txt");
    
	public Membre getMembre() {
		return this.membre;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void actionOk() {
		String erreurs = this.trouverErreursSaisie();
		if (erreurs.length()>0) {
			Alert erreur = new Alert(AlertType.ERROR);
			erreur.setTitle("Mauvaise saisie...");
			erreur.setHeaderText(erreurs);
			erreur.initOwner(this.dialogStage);
			erreur.showAndWait();

		} else {
			Membre newM = this.creerMembre();
			this.membre = newM;

        	appendMembreToFile(newM);
			this.dialogStage.close();
		}
	}

	@FXML
	private void actionAnnuler() {
		this.membre = null;
		this.dialogStage.close();
	}
	
	private String trouverErreursSaisie() {
		String erreurs = "";
		
		if (txtNom.getText().trim().length()<3) {
			erreurs += "- Le nom est obligatoire et >= 2 lettres\n";
		}
		if (txtPrenom.getText().trim().length()<3) {
			erreurs += "- Le prénom est obligatoire et >= 2 lettres\n";
		}
		if ( !radioButAncien.isSelected() && !radioButMembre.isSelected() && !radioButProspect.isSelected() ) {
			erreurs += "- Il faut préciser un état pour ce membre\n";
		}
		if ( datePickInscription.getValue() == null ) {
			erreurs += "- La date d'inscription est obligatoire\n";
		}
		
		return erreurs;
	}
	
	/**
	 * Créer un Membre en partant des valeurs saisies.
	 * 
	 * cette méthode ne doit être appellée que si trouverErreursSaisie() retourne une chaine vide
	 * 
	 * @return le nouveau Membre
	 */
	private Membre creerMembre() {
		// 
		EtatMembre etat=null;
		if(radioButAncien.isSelected())etat=EtatMembre.Ancien;
        if(radioButMembre.isSelected())etat=EtatMembre.Membre;
        if(radioButProspect.isSelected())etat=EtatMembre.Prospect;
		Membre newMembre = new Membre(
				 txtNom.getText().trim(),
				 txtPrenom.getText().trim(),
				 etat,
				 txtVille.getText().trim(),
				 datePickInscription.getValue(),
				 txtNotes.getText().trim()
				);
		return newMembre;
	}
	
	public void setMembre(Membre membre) {
		this.membre=membre;
		txtNom.setText(membre.getNom());
		txtPrenom.setText(membre.getPrenom());
		txtVille.setText(membre.getVille());
		datePickInscription.setValue(membre.getDateInscription());
		txtNotes.setText(membre.getNotes());
		if(membre.getEtat()==EtatMembre.Ancien)radioButAncien.setSelected(true);
		if(membre.getEtat()==EtatMembre.Membre)radioButMembre.setSelected(true);
		if(membre.getEtat()==EtatMembre.Prospect)radioButProspect.setSelected(true);
	}

	private void appendMembreToFile(Membre m) {
    	String line = String.format("%s;%s;%s;%s;%d, %d, %d;%s%n",
        	m.getNom(),
        	m.getPrenom(),
        	m.getEtat().name(),
        	m.getVille(),
        	m.getDateInscription().getYear(),
        	m.getDateInscription().getMonthValue(),
        	m.getDateInscription().getDayOfMonth(),
        	m.getNotes().replace("\n", " ")
    );

    try {
        Files.createDirectories(data_local.getParent());

        Files.write(
            data_local,
            line.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    } catch (IOException e) {
        // e.printStackTrace();
    }
}
}
