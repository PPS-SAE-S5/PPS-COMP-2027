package com.pps.parapente.model;

/**
 * Rôles applicatifs. Chaque rôle donne accès à un sous-ensemble de l'application.
 */
public enum Role {
    ADMINISTRATEUR("Administrateur"),
    RESPONSABLE_EPREUVE("Responsable de l'épreuve"),
    BENEVOLE("Bénévole"),
    PILOTE("Pilote"),
    COMITE_PILOTES("Comité des pilotes");

    private final String libelle;

    Role(String libelle) {
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
