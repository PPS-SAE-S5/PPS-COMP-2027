package com.example.pps.ui.classement;

public class Participant {
    private int rang;
    private String nom;
    private int numero;
    private int points;

    public Participant(int rang, String nom, int numero, int points) {
        this.rang = rang;
        this.nom = nom;
        this.numero = numero;
        this.points = points;
    }

    public int getRang() {
        return rang;
    }

    public String getNom() {
        return nom;
    }

    public int getNumero() {
        return numero;
    }

    public int getPoints() {
        return points;
    }
}
