package com.pps.parapente.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Journée de compétition (ex: "Journée 1" ou "Finale") qui contient des ateliers. */
public class Journee {
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int id;
    private String uuid;
    private String nom;
    private LocalDate dateJournee;
    private int ordre;
    private List<Atelier> ateliers = new ArrayList<>();

    public Journee() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public LocalDate getDateJournee() { return dateJournee; }
    public void setDateJournee(LocalDate dateJournee) { this.dateJournee = dateJournee; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public List<Atelier> getAteliers() { return ateliers; }
    public void setAteliers(List<Atelier> ateliers) { this.ateliers = ateliers; }

    public String getDateFormatee() {
        return dateJournee == null ? "" : dateJournee.format(FORMAT_DATE);
    }

    @Override
    public String toString() {
        return dateJournee == null ? nom : nom + " · " + getDateFormatee();
    }
}
