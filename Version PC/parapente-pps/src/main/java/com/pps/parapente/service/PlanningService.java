package com.pps.parapente.service;

import com.pps.parapente.dao.PlanningDAO;
import com.pps.parapente.model.Atelier;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Journee;

import java.sql.SQLException;
import java.util.ArrayList;
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

    /**
     * Liste les champs obligatoires non renseignés d'un atelier (libellés destinés à l'affichage).
     * Liste vide = tous les champs obligatoires sont remplis.
     * Utilisée à la fois par le service (création / modification) et par le formulaire pour signaler les champs en erreur.
     */
    public static List<String> champsObligatoiresManquants(Atelier a) {
        List<String> manquants = new ArrayList<>();
        if (estVide(a.getNom())) manquants.add(CHAMP_NOM);
        if (a.getJourneeId() <= 0) manquants.add(CHAMP_JOURNEE);
        if (a.getCreneau() == null) manquants.add(CHAMP_CRENEAU);
        if (estVide(a.getHeureDebut())) manquants.add(CHAMP_DEBUT);
        if (estVide(a.getHeureFin())) manquants.add(CHAMP_FIN);
        if (estVide(a.getLieu())) manquants.add(CHAMP_LIEU);
        if (estVide(a.getResponsable())) manquants.add(CHAMP_RESPONSABLE);
        return manquants;
    }

    public static final String CHAMP_NOM = "nom de l'atelier";
    public static final String CHAMP_JOURNEE = "journée";
    public static final String CHAMP_CRENEAU = "créneau (matin ou après-midi)";
    public static final String CHAMP_DEBUT = "heure de début";
    public static final String CHAMP_FIN = "heure de fin";
    public static final String CHAMP_LIEU = "lieu";
    public static final String CHAMP_RESPONSABLE = "responsable";

    private static boolean estVide(String s) {
        return s == null || s.isBlank();
    }

    /** Construit le message d'erreur listant les champs manquants */
    public static String messageChampsManquants(List<String> manquants) {
        if (manquants.size() == 1) {
            return "L'atelier ne peut pas être enregistré : le champ « " + manquants.get(0) + " » est obligatoire.";
        }
        StringBuilder sb = new StringBuilder("L'atelier ne peut pas être enregistré, champs obligatoires manquants :");
        for (String champ : manquants) sb.append("\n  • ").append(champ);
        return sb.toString();
    }

    /** Vrai si l'heure est au format HH:mm */
    public static boolean heureValide(String heure) {
        return heure != null && HEURE.matcher(heure.trim()).matches();
    }

    /**
     * Liste les erreurs de format / cohérence des horaires
     */
    public static List<String> erreursHoraires(String heureDebut, String heureFin) {
        List<String> erreurs = new ArrayList<>();
        boolean debutOk = heureValide(heureDebut);
        boolean finOk = heureValide(heureFin);
        if (!debutOk) {
            erreurs.add("L'heure de début « " + heureDebut.trim() + " » n'est pas au format HH:mm (ex : 08:00).");
        }
        if (!finOk) {
            erreurs.add("L'heure de fin « " + heureFin.trim() + " » n'est pas au format HH:mm (ex : 10:30).");
        }
        if (debutOk && finOk && heureFin.trim().compareTo(heureDebut.trim()) <= 0) {
            erreurs.add(MESSAGE_ORDRE_HORAIRES);
        }
        return erreurs;
    }

    public static final String MESSAGE_ORDRE_HORAIRES = "L'heure de fin doit être après l'heure de début.";

    void validerAtelier(Atelier a) {
        List<String> manquants = champsObligatoiresManquants(a);
        if (!manquants.isEmpty()) {
            throw new IllegalArgumentException(messageChampsManquants(manquants));
        }
        List<String> erreurs = erreursHoraires(a.getHeureDebut(), a.getHeureFin());
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", erreurs));
        }
        a.setNom(a.getNom().trim());
        a.setHeureDebut(a.getHeureDebut().trim());
        a.setHeureFin(a.getHeureFin().trim());
        a.setLieu(a.getLieu().trim());
        a.setResponsable(a.getResponsable().trim());
    }
}
