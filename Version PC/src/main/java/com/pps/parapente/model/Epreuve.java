package com.pps.parapente.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Épreuve du championnat, créée librement par le Responsable de l'épreuve.
 * La liste fournie par le club (atterrissage de précision, marche & vol, cross, checkpoint...)
 * n'est qu'un exemple : ici tout est paramétrable, y compris le mode de calcul des points.
 */
public class Epreuve {
    private int id;
    private String nom;
    private String description;
    private ModeCalcul modeCalcul = ModeCalcul.FORMULE;
    private String formule;              // ex: "100 - temps*2 + nbrBalises*10 - poidsSac*0.5"
    private String valeurCle;            // nom de variable utilisée pour classer en mode BAREME
    private SensClassement sensClassement = SensClassement.ASC;
    private boolean afficherClassement = true;
    private boolean compteDansGeneral = true;
    private boolean actif = true;
    private int ordre;

    private List<EpreuveParametre> parametres = new ArrayList<>();
    private List<BaremePoint> bareme = new ArrayList<>();

    public Epreuve() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ModeCalcul getModeCalcul() { return modeCalcul; }
    public void setModeCalcul(ModeCalcul modeCalcul) { this.modeCalcul = modeCalcul; }

    public String getFormule() { return formule; }
    public void setFormule(String formule) { this.formule = formule; }

    public String getValeurCle() { return valeurCle; }
    public void setValeurCle(String valeurCle) { this.valeurCle = valeurCle; }

    public SensClassement getSensClassement() { return sensClassement; }
    public void setSensClassement(SensClassement sensClassement) { this.sensClassement = sensClassement; }

    public boolean isAfficherClassement() { return afficherClassement; }
    public void setAfficherClassement(boolean afficherClassement) { this.afficherClassement = afficherClassement; }

    public boolean isCompteDansGeneral() { return compteDansGeneral; }
    public void setCompteDansGeneral(boolean compteDansGeneral) { this.compteDansGeneral = compteDansGeneral; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public List<EpreuveParametre> getParametres() { return parametres; }
    public void setParametres(List<EpreuveParametre> parametres) { this.parametres = parametres; }

    public List<BaremePoint> getBareme() { return bareme; }
    public void setBareme(List<BaremePoint> bareme) { this.bareme = bareme; }

    @Override
    public String toString() { return nom; }
}
