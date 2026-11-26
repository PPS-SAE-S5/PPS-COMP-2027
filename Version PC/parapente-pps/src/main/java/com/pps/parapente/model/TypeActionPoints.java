package com.pps.parapente.model;

/**
 * Nature d'une ligne de l'historique des points.
 */
public enum TypeActionPoints {
    SAISIE("Saisie"),
    MODIFICATION("Modification"),
    DISQUALIFICATION("Disqualification"),
    RECALCUL_BAREME("Recalcul du barème"),
    SUPPRESSION("Suppression");

    private final String libelle;

    TypeActionPoints(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    /** Lit la valeur enregistrée en base (MODIFICATION si inconnue). */
    public static TypeActionPoints depuisCode(String code) {
        if (code != null) {
            for (TypeActionPoints t : values()) {
                if (t.name().equalsIgnoreCase(code.trim())) return t;
            }
        }
        return MODIFICATION;
    }

    @Override
    public String toString() { return libelle; }
}
