package com.pps.parapente.model;

/**
 * Une ligne de barème : "le rang N rapporte X points".
 * Utilisé quand l'épreuve est en ModeCalcul.BAREME.
 */
public class BaremePoint {
    private int id;
    private int epreuveId;
    private int rang;
    private double points;

    public BaremePoint() {}

    public BaremePoint(int rang, double points) {
        this.rang = rang;
        this.points = points;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEpreuveId() { return epreuveId; }
    public void setEpreuveId(int epreuveId) { this.epreuveId = epreuveId; }

    public int getRang() { return rang; }
    public void setRang(int rang) { this.rang = rang; }

    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }
}
