package com.pps.parapente.model;

/**
 * Catégorie d'un pilote 
 */
public enum Categorie {
    POMPIER("Pompier"),
    OPEN("Open");

    private final String libelle;

    Categorie(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    /** Lit la valeur enregistrée en base */
    public static Categorie depuisCode(String code) {
        if (code != null && code.trim().equalsIgnoreCase(OPEN.name())) return OPEN;
        return POMPIER;
    }

    @Override
    public String toString() { return libelle; }
}
