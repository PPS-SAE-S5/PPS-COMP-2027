package com.pps.parapente.model;

import java.util.List;

/**
 * Synthèse des scores d'un pilote 
 */
public record SuiviParticipant(Pilote pilote, List<LigneSuivi> lignes, double totalPoints, int rang, int nbPilotes, int epreuvesSaisies, int epreuvesAttendues) {

    /** Situation d'un pilote sur une épreuve */
    public enum Statut {
        SAISI("Saisi"),
        DISQUALIFIE("Disqualifié"),
        PAS_ENCORE_SAISI("Pas encore saisi");

        private final String libelle;

        Statut(String libelle) { this.libelle = libelle; }

        public String getLibelle() { return libelle; }
    }

    /**
     * @param points  null tant que rien n'est saisi
     * @param compteGeneral l'épreuve compte-t-elle dans le classement général ?
     */
    public record LigneSuivi(String epreuve, String valeurs, Double points, Statut statut, boolean compteGeneral) {}
}
