package com.pps.parapente.model;

/**
 * Compte de connexion à l'application.
 */
public class Utilisateur {
    private int id;
    private String identifiant;
    private String motDePasseHache;
    private Role role;
    private Integer piloteId; // nullable : lien vers un pilote si le compte est un compte "Pilote"
    private boolean actif = true;

    public Utilisateur() {}

    public Utilisateur(String identifiant, String motDePasseHache, Role role) {
        this.identifiant = identifiant;
        this.motDePasseHache = motDePasseHache;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIdentifiant() { return identifiant; }
    public void setIdentifiant(String identifiant) { this.identifiant = identifiant; }

    public String getMotDePasseHache() { return motDePasseHache; }
    public void setMotDePasseHache(String motDePasseHache) { this.motDePasseHache = motDePasseHache; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Integer getPiloteId() { return piloteId; }
    public void setPiloteId(Integer piloteId) { this.piloteId = piloteId; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
