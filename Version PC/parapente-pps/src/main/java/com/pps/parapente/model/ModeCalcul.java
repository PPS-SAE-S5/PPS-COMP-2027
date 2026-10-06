package com.pps.parapente.model;

/**
 * Mode de calcul des points d'une épreuve :
 * - FORMULE : une expression mathématique est évaluée à partir des variables saisies (et de l'âge / poids du pilote)
 * - BAREME  : les pilotes sont classés sur une variable clé, puis les points sont attribués selon un barème par rang
 */
public enum ModeCalcul {
    FORMULE,
    BAREME
}
