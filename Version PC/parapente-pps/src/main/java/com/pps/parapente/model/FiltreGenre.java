package com.pps.parapente.model;

/**
 * Filtre de classement : mixte (hommes + femmes), hommes seulement ou femmes seulement.
 * Les valeurs de genre enregistrées sur la fiche pilote sont "Homme" et "Femme".
 */
public enum FiltreGenre {
    MIXTE("Mixte (hommes + femmes)", null),
    HOMMES("Hommes", "Homme"),
    FEMMES("Femmes", "Femme");

    private final String libelle;
    private final String valeur;

    FiltreGenre(String libelle, String valeur) {
        this.libelle = libelle;
        this.valeur = valeur;
    }

    public String getLibelle() { return libelle; }

    /** Vrai si le pilote appartient à ce classement (le mixte accepte tout le monde, même sans genre renseigné). */
    public boolean accepte(Pilote pilote) {
        if (valeur == null) return true;
        if (pilote == null || pilote.getGenre() == null) return false;
        return valeur.equalsIgnoreCase(pilote.getGenre().trim());
    }

    /** Texte affiché dans la liste déroulante. */
    @Override
    public String toString() { return libelle; }
}
