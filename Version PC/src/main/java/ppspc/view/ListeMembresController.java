package gestclub.view;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import gestclub.GestClubApp;
import gestclub.model.Membre;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;

public class ListeMembresController implements Initializable {
	
	@FXML
	private ListView<Membre> listViewMembres;
	@FXML
	private Button butEditer;
	@FXML
	private Button butSupprimer;
	@FXML
	private MenuItem butEditerMenu;
	@FXML
	private MenuItem butSupprimerMenu;
	@FXML
	private MenuItem butNouveauMenu;

	
	private GestClubApp getClubApp;
	

	public void setGetClubApp(GestClubApp getClubApp) {
		this.getClubApp = getClubApp;
	}

	public void close() throws Exception{getClubApp.stop();}

	public void initialisation(){
		butEditer.setDisable(true);
		butSupprimer.setDisable(true);
		butEditerMenu.setDisable(true);
		butSupprimerMenu.setDisable(true);
		listViewMembres.setOnMouseClicked( e -> {butEditer.setDisable(false);butSupprimer.setDisable(false);butEditerMenu.setDisable(false);butSupprimerMenu.setDisable(false);});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	public void setListeMembre( List<Membre> liste ) {
		for (Membre membre : liste) {
			this.listViewMembres.getItems().add(membre);
		}
	}

	@FXML
	private void actionNouveau() {
		Membre m = this.getClubApp.showSaisieMembre();
		if (m!=null) {
			this.listViewMembres.getItems().add(m);
		}
	}
	@FXML
	private void actionEditer() {
		Membre membre=listViewMembres.getFocusModel().getFocusedItem();
		int index = listViewMembres.getSelectionModel().getSelectedIndex();
		if(index!=-1){
			Membre retour=getClubApp.editSaisieMembre(membre);
			if(retour!=null){
				listViewMembres.getItems().set(index, retour);

				// getClubApp.remove(index);

			}
		} else{
			Selection();
		}
	}
	@FXML
	private void actionSupprimer() {
		int index = listViewMembres.getSelectionModel().getSelectedIndex();
		if(index==-1)Selection();
		else{
			Alert confirm = new Alert(AlertType.CONFIRMATION);
			confirm.setTitle("Suppression");
			confirm.setHeaderText("Voulez-vous vraiment finaliser cette suppression ?");
			confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

			Optional<ButtonType> rep = confirm.showAndWait();
			if(rep.orElse(null) == ButtonType.YES) {
				listViewMembres.getItems().remove(index);
				getClubApp.retraitDB(index);
				Alert cBon = new Alert(AlertType.INFORMATION);
				cBon.setTitle("Confirmation");
				cBon.setHeaderText("Suppression réussie");
				cBon.showAndWait();
			}
			
		}
	}

	@FXML
    private void Interog() {
        Alert confirm = new Alert(Alert.AlertType.INFORMATION);
        confirm.setTitle("Gestion de Club");
        confirm.setHeaderText("Gestion d'un Club");
        confirm.setContentText("Fait par un étudiant");
        confirm.getButtonTypes().setAll(ButtonType.OK);
        confirm.showAndWait();
    }

	private void Selection(){
			Alert erreur = new Alert(AlertType.ERROR);
			erreur.setTitle("Erreur de selection");
			erreur.setHeaderText("Veuillez sélectionner un membre pour continuer.");
			erreur.showAndWait();
	}
}
