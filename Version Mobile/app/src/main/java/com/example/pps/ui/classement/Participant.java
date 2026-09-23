package com.example.pps.ui.classement;

import com.google.gson.annotations.SerializedName;

public class Participant {
    @SerializedName("rang")
    private Integer rang;

    @SerializedName("total_points")
    private Double totalPoints;

    @SerializedName("nom")
    private String nom;

    @SerializedName("prenom")
    private String prenom;

    public int getRang() {
        return rang != null ? rang : 0;
    }

    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    public double getTotalPoints() {
        return totalPoints != null ? totalPoints : 0;
    }
}