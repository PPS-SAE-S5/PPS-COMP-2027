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
    private Map<String, Double> valeurs = new LinkedHashMap<>();
    private double points;
    private boolean disqualifie; // sécurité : passe automatiquement à 0 pt
    private String saisiPar;
    private LocalDateTime dateSaisie;

    // Champs pratiques pour l'affichage (non stockés directement, remplis par les services)
    private transient Pilote pilote;

    // Contexte pour l'historique des points (non stocké dans la table resultats, lu par ResultatDAO)
    private transient TypeActionPoints typeActionHistorique;
    private transient String motifHistorique;
    private transient boolean sansHistorique;
    private transient boolean etatAvantFourni;
    private transient Resultat etatAvant;

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

    public TypeActionPoints getTypeActionHistorique() { return typeActionHistorique; }
    public void setTypeActionHistorique(TypeActionPoints typeActionHistorique) { this.typeActionHistorique = typeActionHistorique; }

    public String getMotifHistorique() { return motifHistorique; }
    public void setMotifHistorique(String motifHistorique) { this.motifHistorique = motifHistorique; }

    public boolean isSansHistorique() { return sansHistorique; }
    public void setSansHistorique(boolean sansHistorique) { this.sansHistorique = sansHistorique; }

    public boolean isEtatAvantFourni() { return etatAvantFourni; }
    public Resultat getEtatAvant() { return etatAvant; }
    /** Impose l'état de référence "avant" de l'historique (null = le pilote n'avait pas encore de résultat). */
    public void setEtatAvant(Resultat etatAvant) {
        this.etatAvant = etatAvant;
        this.etatAvantFourni = true;
    }
}
