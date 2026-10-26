package com.pps.parapente.service;

import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.dao.PiloteDAO;
import com.pps.parapente.dao.ResultatDAO;
import com.pps.parapente.model.*;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.sql.SQLException;
import java.util.*;

/**
 * Cœur métier du calcul des points. Deux modes possibles, choisis librement par
 * le Responsable de l'épreuve lors de la création de celle-ci :
 *
 *  - FORMULE : évalue une expression mathématique (ex: "100 - temps*2 + nbrBalises*10").
 *              Variables disponibles : celles définies dans les paramètres de l'épreuve,
 *              plus automatiquement "age" et "poidsPilote" issus de la fiche du pilote.
 *
 *  - BAREME  : classe tous les pilotes ayant un résultat sur la "valeur clé" (ASC ou DESC),
 *              puis attribue les points du barème (rang -> points) correspondant à ce rang.
 *
 * Règle du club : un pilote inscrit qui n'a pas de résultat saisi obtient 0 point (géré
 * au niveau du ClassementService, pas ici, puisque ce service ne calcule que des résultats existants).
 */
public class CalculService {

    private final PiloteDAO piloteDAO = new PiloteDAO();
    private final ResultatDAO resultatDAO = new ResultatDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    /**
     * Calcule et enregistre le résultat d'UN pilote sur une épreuve en mode FORMULE.
     * À utiliser juste après la saisie des valeurs par le bénévole.
     */
    public Resultat calculerEtEnregistrerFormule(Epreuve epreuve, Pilote pilote, Map<String, Double> valeursSaisies,
                                                  boolean disqualifie, String saisiPar) throws SQLException {
        if (epreuve.getModeCalcul() != ModeCalcul.FORMULE) {
            throw new IllegalStateException("Cette épreuve n'est pas en mode FORMULE");
        }

        double points = 0;
        if (!disqualifie) {
            points = evaluerFormule(epreuve.getFormule(), construireVariables(pilote, valeursSaisies));
        }

        Resultat r = new Resultat();
        r.setEpreuveId(epreuve.getId());
        r.setPiloteId(pilote.getId());
        r.setValeurs(valeursSaisies);
        r.setPoints(points);
        r.setDisqualifie(disqualifie);
        r.setSaisiPar(saisiPar);
        resultatDAO.enregistrer(r);
        return r;
    }

    /**
     * Enregistre la saisie brute d'un pilote pour une épreuve en mode BAREME
     * (les points seront (re)calculés ensuite pour TOUT le monde via recalculerBareme,
     * car le barème dépend du classement relatif de tous les pilotes).
     */
    public void enregistrerValeurBareme(Epreuve epreuve, Pilote pilote, Map<String, Double> valeursSaisies,
                                         boolean disqualifie, String saisiPar) throws SQLException {
        Resultat r = new Resultat();
        r.setEpreuveId(epreuve.getId());
        r.setPiloteId(pilote.getId());
        r.setValeurs(valeursSaisies);
        r.setPoints(0);
        r.setDisqualifie(disqualifie);
        r.setSaisiPar(saisiPar);
        resultatDAO.enregistrer(r);
        recalculerBareme(epreuve);
    }

    /**
     * Recalcule les points de TOUS les résultats d'une épreuve en mode BAREME :
     * trie les pilotes selon la valeur clé (ASC = plus petit gagne, DESC = plus grand gagne),
     * puis applique la table de points par rang. Les disqualifiés restent à 0 et ne prennent pas de rang.
     */
    public void recalculerBareme(Epreuve epreuve) throws SQLException {
        if (epreuve.getModeCalcul() != ModeCalcul.BAREME) {
            throw new IllegalStateException("Cette épreuve n'est pas en mode BAREME");
        }
        if (epreuve.getValeurCle() == null || epreuve.getValeurCle().isBlank()) {
            throw new IllegalStateException("Aucune 'valeur clé' définie pour le classement de cette épreuve");
        }

        List<Resultat> resultats = resultatDAO.listerParEpreuve(epreuve.getId());

        List<Resultat> classables = new ArrayList<>();
        for (Resultat r : resultats) {
            if (!r.isDisqualifie() && r.getValeurs().containsKey(epreuve.getValeurCle())) {
                classables.add(r);
            }
        }

        Comparator<Resultat> comparateur = Comparator.comparingDouble(r -> r.getValeurs().get(epreuve.getValeurCle()));
        if (epreuve.getSensClassement() == SensClassement.DESC) {
            comparateur = comparateur.reversed();
        }
        classables.sort(comparateur);

        Map<Integer, Double> baremeParRang = new HashMap<>();
        for (BaremePoint bp : epreuve.getBareme()) baremeParRang.put(bp.getRang(), bp.getPoints());
        double dernierPointsBareme = epreuve.getBareme().isEmpty() ? 0 :
                epreuve.getBareme().get(epreuve.getBareme().size() - 1).getPoints();

        int rang = 1;
        for (Resultat r : classables) {
            double points = baremeParRang.getOrDefault(rang, dernierPointsBareme); // au-delà du barème -> dernier palier
            r.setPoints(points);
            resultatDAO.enregistrer(r);
            rang++;
        }
        // Les disqualifiés / résultats incomplets restent à 0 pt
        for (Resultat r : resultats) {
            if (r.isDisqualifie() || !r.getValeurs().containsKey(epreuve.getValeurCle())) {
                r.setPoints(0);
                resultatDAO.enregistrer(r);
            }
        }
    }

    /** Évalue une expression mathématique avec exp4j à partir des variables fournies. */
    public double evaluerFormule(String formule, Map<String, Double> variables) {
        if (formule == null || formule.isBlank()) {
            throw new IllegalArgumentException("Formule vide");
        }
        ExpressionBuilder builder = new ExpressionBuilder(formule);
        for (String variable : variables.keySet()) builder.variable(variable);
        Expression expr = builder.build();
        for (Map.Entry<String, Double> e : variables.entrySet()) {
            expr.setVariable(e.getKey(), e.getValue());
        }
        return expr.evaluate();
    }

    /**
     * Construit la table de variables disponibles pour une formule :
     * valeurs saisies manuellement + variables automatiques du profil pilote (age, poidsPilote).
     */
    private Map<String, Double> construireVariables(Pilote pilote, Map<String, Double> valeursSaisies) {
        Map<String, Double> variables = new HashMap<>(valeursSaisies);
        variables.put("age", (double) pilote.getAge());
        variables.put("poidsPilote", pilote.getPoids() != null ? pilote.getPoids() : 0.0);
        return variables;
    }
}
