package com.pps.parapente.model;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Une ligne de l'historique des modifications de points (lecture seule une fois enregistrée).
 * Les informations du pilote et de l'épreuve sont recopiées au moment de l'action,
 * pour que l'historique reste lisible même si le pilote ou l'épreuve est supprimé ensuite.
 */
public class HistoriquePoint {
    private int id;
    private String uuid;
    private LocalDateTime dateAction;
    private String auteur;
    private String auteurRole;
    private String piloteLicence;
    private String piloteNom;
    private String epreuveUuid;
    private String epreuveNom;
    private String categorie;
    private TypeActionPoints typeAction;
    private Double pointsAvant;   // null pour une première saisie
    private Double pointsApres;
    private String motif;
    private String valeursJson;

    public HistoriquePoint() {}

    /** Écart de points apporté par l'action (positif = gain, négatif = perte). */
    public double getEcart() {
        double avant = pointsAvant != null ? pointsAvant : 0;
        double apres = pointsApres != null ? pointsApres : 0;
        return apres - avant;
    }

    /**
     * Phrase lisible du journal, construite à l'affichage à partir des colonnes. Exemples :
     *  Bénévole [User40] : Participant [Jean Dupont] gagne 700 pts en catégorie Pompier-H sur « Marche & Vol »
     *  Comité des pilotes [User11] : Participant [Jean Dupont] perd 550 pts en catégorie Pompier-H sur « Précision » pour « Descente dangereuse »
     */
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(libelleRole()).append(" [").append(auteur != null ? auteur : "inconnu").append("] : ");
        sb.append("Participant [").append(piloteNom != null ? piloteNom.trim() : "?").append("] ");

        double ecart = getEcart();
        if (typeAction == TypeActionPoints.SUPPRESSION) {
            sb.append("voit son résultat supprimé");
            if (ecart != 0) sb.append(" et perd ").append(formaterPoints(-ecart)).append(" pts");
        } else if (ecart > 0) {
            sb.append("gagne ").append(formaterPoints(ecart)).append(" pts");
        } else if (ecart < 0) {
            sb.append("perd ").append(formaterPoints(-ecart)).append(" pts");
        } else {
            sb.append("reste à ").append(formaterPoints(pointsApres != null ? pointsApres : 0)).append(" pts");
        }

        if (categorie != null && !categorie.isBlank()) sb.append(" en catégorie ").append(categorie);
        if (epreuveNom != null && !epreuveNom.isBlank()) sb.append(" sur « ").append(epreuveNom).append(" »");
        if (motif != null && !motif.isBlank()) sb.append(" pour « ").append(motif.trim()).append(" »");

        if (typeAction == TypeActionPoints.DISQUALIFICATION) sb.append(" (disqualification)");
        if (typeAction == TypeActionPoints.RECALCUL_BAREME) sb.append(" (recalcul du barème)");
        return sb.toString();
    }

    private String libelleRole() {
        if (auteurRole == null || auteurRole.isBlank()) return "Utilisateur";
        try {
            return Role.valueOf(auteurRole.trim()).getLibelle();
        } catch (IllegalArgumentException e) {
            return auteurRole;
        }
    }

    /** 700.0 -> "700", 12.5 -> "12,5" */
    public static String formaterPoints(double points) {
        if (points == Math.rint(points)) return String.valueOf((long) points);
        return String.format(Locale.FRANCE, "%.2f", points).replaceAll("0+$", "");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public String getAuteurRole() { return auteurRole; }
    public void setAuteurRole(String auteurRole) { this.auteurRole = auteurRole; }

    public String getPiloteLicence() { return piloteLicence; }
    public void setPiloteLicence(String piloteLicence) { this.piloteLicence = piloteLicence; }

    public String getPiloteNom() { return piloteNom; }
    public void setPiloteNom(String piloteNom) { this.piloteNom = piloteNom; }

    public String getEpreuveUuid() { return epreuveUuid; }
    public void setEpreuveUuid(String epreuveUuid) { this.epreuveUuid = epreuveUuid; }

    public String getEpreuveNom() { return epreuveNom; }
    public void setEpreuveNom(String epreuveNom) { this.epreuveNom = epreuveNom; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public TypeActionPoints getTypeAction() { return typeAction; }
    public void setTypeAction(TypeActionPoints typeAction) { this.typeAction = typeAction; }

    public Double getPointsAvant() { return pointsAvant; }
    public void setPointsAvant(Double pointsAvant) { this.pointsAvant = pointsAvant; }

    public Double getPointsApres() { return pointsApres; }
    public void setPointsApres(Double pointsApres) { this.pointsApres = pointsApres; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getValeursJson() { return valeursJson; }
    public void setValeursJson(String valeursJson) { this.valeursJson = valeursJson; }
}
