package com.pps.parapente.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Une ligne du classement général : un pilote, son total de points,
 * et le détail des points par épreuve (pour affichage en tableau).
 */
public class LigneClassement {
    private Pilote pilote;
    private double totalPoints;
    private int rang;
    private Map<Integer, Double> pointsParEpreuve = new LinkedHashMap<>(); // epreuveId -> points

    public LigneClassement(Pilote pilote) {
        this.pilote = pilote;
    }

    public Pilote getPilote() { return pilote; }
    public double getTotalPoints() { return totalPoints; }
    public void setTotalPoints(double totalPoints) { this.totalPoints = totalPoints; }
    public int getRang() { return rang; }
    public void setRang(int rang) { this.rang = rang; }
    public Map<Integer, Double> getPointsParEpreuve() { return pointsParEpreuve; }
}
