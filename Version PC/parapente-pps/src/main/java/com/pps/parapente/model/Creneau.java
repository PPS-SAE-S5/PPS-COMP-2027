package com.pps.parapente.model;

/** Moment de la journée auquel se déroule un atelier. */
public enum Creneau {
    MATIN("Matin"),
    APRES_MIDI("Après-midi");

    private final String libelle;

    Creneau(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
