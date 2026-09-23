package com.example.pps.ui.config;

import com.google.gson.annotations.SerializedName;

public class NouvelleManche {
    @SerializedName("competition_id")
    private String competitionId;

    @SerializedName("type_epreuve_id")
    private String typeEpreuveId;

    @SerializedName("numero")
    private int numero;

    @SerializedName("date_manche")
    private String dateManche;

    @SerializedName("distance_ref_km")
    private Double distanceRefKm;

    @SerializedName("statut")
    private String statut;

    public NouvelleManche(String competitionId, String typeEpreuveId, int numero,
                          String dateManche, Double distanceRefKm) {
        this.competitionId = competitionId;
        this.typeEpreuveId = typeEpreuveId;
        this.numero = numero;
        this.dateManche = dateManche;
        this.distanceRefKm = distanceRefKm;
        this.statut = "planifiee";
    }
}