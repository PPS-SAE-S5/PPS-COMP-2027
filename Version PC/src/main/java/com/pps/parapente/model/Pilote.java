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
    private String caserne;
    private Double poids;
    private String email;
    private Integer anneeNaissance;
    private String categorie;
    private LocalDateTime dateInscription;

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

    public String getCaserne() { return caserne; }
    public void setCaserne(String caserne) { this.caserne = caserne; }

    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAnneeNaissance() { return anneeNaissance; }
    public void setAnneeNaissance(Integer anneeNaissance) { this.anneeNaissance = anneeNaissance; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
}
