package com.pps.parapente.model;

import java.time.LocalDateTime;

/**
 * Pilote inscrit au championnat (informations d'inscription demandées par le club).
 */
public class Pilote {
    private int id;
    private String numeroLicence;
    private String nom;
    private String prenom;
    private String departement;
    private Double poids;
    private String email;
    private Integer anneeNaissance;
    private String genre;
    private LocalDateTime dateInscription;
    /** Numéro de pilote (dossard) attribué par l'organisation ; facultatif. */
    private Integer numeroPilote;
    /** Pompier (championnat) ou open (hors championnat), déduite du numéro de pilote à l'inscription. */
    private Categorie categorie = Categorie.POMPIER;

    public Pilote() {}

    public int getAge() {
        if (anneeNaissance == null) return 0;
        return LocalDateTime.now().getYear() - anneeNaissance;
    }

    public String getNomComplet() {
        return (prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroLicence() { return numeroLicence; }
    public void setNumeroLicence(String numeroLicence) { this.numeroLicence = numeroLicence; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }

    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAnneeNaissance() { return anneeNaissance; }
    public void setAnneeNaissance(Integer anneeNaissance) { this.anneeNaissance = anneeNaissance; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getNumeroPilote() { return numeroPilote; }
    public void setNumeroPilote(Integer numeroPilote) { this.numeroPilote = numeroPilote; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie != null ? categorie : Categorie.POMPIER; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    /** Affichage par défaut dans les listes déroulantes (ComboBox) et autres contrôles JavaFX. */
    @Override
    public String toString() {
        return getNomComplet() + (numeroLicence != null && !numeroLicence.isBlank() ? " (n°" + numeroLicence + ")" : "");
    }
}
