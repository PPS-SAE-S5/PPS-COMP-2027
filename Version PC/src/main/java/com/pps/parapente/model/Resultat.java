package com.pps.parapente.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Résultat d'un pilote sur une épreuve : valeurs brutes saisies (par variable) + points calculés.
 */
public class Resultat {
    private int id;
    private int epreuveId;
    private int piloteId;
    private Map<String, Double> valeurs = new LinkedHashMap<>(); // nomVariable -> valeur saisie
    private double points;
    private boolean disqualifie; // sécurité : passe automatiquement à 0 pt
    private String saisiPar;
    private LocalDateTime dateSaisie;

    // Champs pratiques pour l'affichage (non stockés directement, remplis par les services)
    private transient Pilote pilote;

    public Resultat() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEpreuveId() { return epreuveId; }
    public void setEpreuveId(int epreuveId) { this.epreuveId = epreuveId; }

    public int getPiloteId() { return piloteId; }
    public void setPiloteId(int piloteId) { this.piloteId = piloteId; }

    public Map<String, Double> getValeurs() { return valeurs; }
    public void setValeurs(Map<String, Double> valeurs) { this.valeurs = valeurs; }

    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }

    public boolean isDisqualifie() { return disqualifie; }
    public void setDisqualifie(boolean disqualifie) { this.disqualifie = disqualifie; }

    public String getSaisiPar() { return saisiPar; }
    public void setSaisiPar(String saisiPar) { this.saisiPar = saisiPar; }

    public LocalDateTime getDateSaisie() { return dateSaisie; }
    public void setDateSaisie(LocalDateTime dateSaisie) { this.dateSaisie = dateSaisie; }

    public Pilote getPilote() { return pilote; }
    public void setPilote(Pilote pilote) { this.pilote = pilote; }
}
