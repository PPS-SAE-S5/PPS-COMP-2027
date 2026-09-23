package com.example.pps.ui.config;

import com.google.gson.annotations.SerializedName;

public class Competition {
    @SerializedName("id")
    private String id;

    @SerializedName("nom")
    private String nom;

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return nom;
    }
}