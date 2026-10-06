package com.pps.parapente.service;

import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.dao.PiloteDAO;
import com.pps.parapente.model.Pilote;

import java.sql.SQLException;
import java.util.List;

public class PiloteService {

    private final PiloteDAO piloteDAO = new PiloteDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    public List<Pilote> listerTous() throws SQLException {
        return piloteDAO.listerTous();
    }

    public void creer(Pilote p) throws SQLException {
        valider(p);
        piloteDAO.creer(p);
        // Règle du club : tout pilote inscrit doit passer les épreuves -> on l'inscrit
        // automatiquement à toutes les épreuves actives existantes.
        inscriptionDAO.inscrirePiloteAToutesLesEpreuvesActives(p.getId());
    }

    public void mettreAJour(Pilote p) throws SQLException {
        valider(p);
        piloteDAO.mettreAJour(p);
    }

    public void supprimer(int id) throws SQLException {
        piloteDAO.supprimer(id);
    }

    private void valider(Pilote p) {
        if (p.getNumeroLicence() == null || p.getNumeroLicence().isBlank())
            throw new IllegalArgumentException("Le numéro de licence est obligatoire");
        if (p.getNom() == null || p.getNom().isBlank())
            throw new IllegalArgumentException("Le nom est obligatoire");
        if (p.getPrenom() == null || p.getPrenom().isBlank())
            throw new IllegalArgumentException("Le prénom est obligatoire");
    }
}
