package com.pps.parapente.model;

import java.util.ArrayList;
import java.util.List;

/** Atelier : regroupe plusieurs épreuves sur un créneau (matin / après-midi) d'une journée. */
public class Atelier {
    private int id;
    private String uuid;
    private int journeeId;
    private String nom;
    private Creneau creneau = Creneau.MATIN;
    private String heureDebut;
    private String heureFin;
    private String lieu;
    private String responsable;
    private int ordre;
    /** Épreuves de l'atelier, dans l'ordre de passage. */
    private List<Epreuve> epreuves = new ArrayList<>();

    public Atelier() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public int getJourneeId() { return journeeId; }
    public void setJourneeId(int journeeId) { this.journeeId = journeeId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Creneau getCreneau() { return creneau; }
    public void setCreneau(Creneau creneau) { this.creneau = creneau; }

    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }

    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public List<Epreuve> getEpreuves() { return epreuves; }
    public void setEpreuves(List<Epreuve> epreuves) { this.epreuves = epreuves; }

    /** Formatage logique : 08:00 – 10:30, dès 08:00 ou chaîne vide. */
    public String getPlageHoraire() {
        boolean d = heureDebut != null && !heureDebut.isBlank();
        boolean f = heureFin != null && !heureFin.isBlank();
        if (d && f) return heureDebut + " – " + heureFin;
        if (d) return "dès " + heureDebut;
        if (f) return "jusqu'à " + heureFin;
        return "";
    }

    @Override
    public String toString() { return nom; }
}
