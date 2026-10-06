package com.pps.parapente.model;

/**
 * Variable saisissable définie librement par le Responsable de l'épreuve.
 * Exemple pour "Marche & Vol" : nomVariable="temps", label="Temps (secondes)"
 * Ces noms de variables sont ensuite utilisables dans la formule de calcul (mode FORMULE)
 * ou comme "valeur clé" de classement (mode BAREME).
 */
public class EpreuveParametre {
    private int id;
    private int epreuveId;
    private String nomVariable; // identifiant technique, sans espace, utilisé dans la formule
    private String label;       // libellé affiché à la saisie
    private String unite;
    private boolean obligatoire = true;
    private int ordre;

    public EpreuveParametre() {}

    public EpreuveParametre(String nomVariable, String label, String unite) {
        this.nomVariable = nomVariable;
        this.label = label;
        this.unite = unite;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEpreuveId() { return epreuveId; }
    public void setEpreuveId(int epreuveId) { this.epreuveId = epreuveId; }

    public String getNomVariable() { return nomVariable; }
    public void setNomVariable(String nomVariable) { this.nomVariable = nomVariable; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }

    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    @Override
    public String toString() {
        return label + (unite != null && !unite.isBlank() ? " (" + unite + ")" : "");
    }
}
