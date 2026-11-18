package com.pps.parapente.model;

/** Filtre de classement par catégorie */
public enum FiltreCategorie {
    TOUTES("Toutes catégories", null),
    POMPIER("Pompiers (championnat)", Categorie.POMPIER),
    OPEN("Open (hors championnat)", Categorie.OPEN);

    private final String libelle;
    private final Categorie categorie;

    FiltreCategorie(String libelle, Categorie categorie) {
        this.libelle = libelle;
        this.categorie = categorie;
    }

    public String getLibelle() { return libelle; }

    public boolean accepte(Pilote pilote) {
        if (categorie == null) return true;
        return pilote != null && pilote.getCategorie() == categorie;
    }

    @Override
    public String toString() { return libelle; }
}
