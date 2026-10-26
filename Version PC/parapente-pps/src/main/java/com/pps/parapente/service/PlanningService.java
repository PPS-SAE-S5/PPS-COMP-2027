package com.pps.parapente.service;

import com.pps.parapente.dao.PlanningDAO;
import com.pps.parapente.model.Atelier;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Journee;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/** Règles de gestion du planning (journées, ateliers). Les messages d'erreur sont destinés à être affichés tels quels. */
public class PlanningService {

    private static final Pattern HEURE = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private final PlanningDAO dao = new PlanningDAO();

    public List<Journee> listerJournees() throws SQLException { return dao.listerJournees(); }

    public List<Atelier> listerAteliers(int journeeId) throws SQLException { return dao.listerAteliers(journeeId); }

    public List<Epreuve> listerEpreuvesNonAffectees() throws SQLException { return dao.listerEpreuvesNonAffectees(); }

    public List<Epreuve> listerEpreuvesDisponibles(int atelierId) throws SQLException { return dao.listerEpreuvesDisponibles(atelierId); }

    public int compterEpreuvesActives() throws SQLException { return dao.compterEpreuvesActives(); }

    public int compterAteliers(int journeeId) throws SQLException { return dao.compterAteliers(journeeId); }

    public void creerJournee(Journee j) throws SQLException {
        validerJournee(j);
        dao.creerJournee(j);
    }

    public void mettreAJourJournee(Journee j) throws SQLException {
        validerJournee(j);
        dao.mettreAJourJournee(j);
    }

    public void supprimerJournee(int id) throws SQLException { dao.supprimerJournee(id); }

    public void creerAtelier(Atelier a) throws SQLException {
        validerAtelier(a);
        dao.creerAtelier(a);
    }

    public void mettreAJourAtelier(Atelier a) throws SQLException {
        validerAtelier(a);
        dao.mettreAJourAtelier(a);
    }

    public void supprimerAtelier(int id) throws SQLException { dao.supprimerAtelier(id); }

    private void validerJournee(Journee j) {
        if (j.getNom() == null || j.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de la journée est obligatoire.");
        }
        j.setNom(j.getNom().trim());
    }

    private void validerAtelier(Atelier a) {
        if (a.getNom() == null || a.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'atelier est obligatoire.");
        }
        a.setNom(a.getNom().trim());
        if (a.getJourneeId() <= 0) {
            throw new IllegalArgumentException("Choisissez la journée de cet atelier.");
        }
        if (a.getCreneau() == null) {
            throw new IllegalArgumentException("Choisissez le créneau (matin ou après-midi).");
        }
        boolean debut = a.getHeureDebut() != null && !a.getHeureDebut().isBlank();
        boolean fin = a.getHeureFin() != null && !a.getHeureFin().isBlank();
        if (debut && !HEURE.matcher(a.getHeureDebut().trim()).matches()) {
            throw new IllegalArgumentException("L'heure de début doit être au format HH:mm (ex : 08:30).");
        }
        if (fin && !HEURE.matcher(a.getHeureFin().trim()).matches()) {
            throw new IllegalArgumentException("L'heure de fin doit être au format HH:mm (ex : 10:30).");
        }
        if (debut && fin && a.getHeureFin().trim().compareTo(a.getHeureDebut().trim()) <= 0) {
            throw new IllegalArgumentException("L'heure de fin doit être après l'heure de début.");
        }
        if (debut) a.setHeureDebut(a.getHeureDebut().trim());
        if (fin) a.setHeureFin(a.getHeureFin().trim());
    }
}
