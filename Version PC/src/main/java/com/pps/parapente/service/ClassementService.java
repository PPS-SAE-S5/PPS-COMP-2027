package com.pps.parapente.service;

import com.pps.parapente.dao.EpreuveDAO;
import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.dao.PiloteDAO;
import com.pps.parapente.dao.ResultatDAO;
import com.pps.parapente.model.*;

import java.sql.SQLException;
import java.util.*;

/**
 * Calcule le classement général du championnat.
 * Règle du club : "toute personne inscrite doit passer les épreuves (sinon 0 pt)"
 * -> pour chaque épreuve active qui compte dans le classement général, si le pilote
 * est inscrit mais n'a pas de résultat, il obtient 0 point pour cette épreuve.
 */
public class ClassementService {

    private final EpreuveDAO epreuveDAO = new EpreuveDAO();
    private final PiloteDAO piloteDAO = new PiloteDAO();
    private final ResultatDAO resultatDAO = new ResultatDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    public List<LigneClassement> calculerClassementGeneral() throws SQLException {
        List<Pilote> pilotes = piloteDAO.listerTous();
        List<Epreuve> epreuves = epreuveDAO.listerToutes();
        List<Epreuve> epreuvesComptees = epreuves.stream()
                .filter(Epreuve::isActif)
                .filter(Epreuve::isCompteDansGeneral)
                .toList();

        Map<Integer, LigneClassement> lignesParPilote = new LinkedHashMap<>();
        for (Pilote p : pilotes) lignesParPilote.put(p.getId(), new LigneClassement(p));

        for (Epreuve epreuve : epreuvesComptees) {
            List<Resultat> resultats = resultatDAO.listerParEpreuve(epreuve.getId());
            Map<Integer, Double> pointsParPilote = new HashMap<>();
            for (Resultat r : resultats) pointsParPilote.put(r.getPiloteId(), r.getPoints());

            List<Integer> inscrits = inscriptionDAO.listerPilotesInscrits(epreuve.getId());
            for (Integer piloteId : inscrits) {
                LigneClassement ligne = lignesParPilote.get(piloteId);
                if (ligne == null) continue; // pilote supprimé entre-temps
                double points = pointsParPilote.getOrDefault(piloteId, 0.0); // pas de résultat = 0 pt
                ligne.getPointsParEpreuve().put(epreuve.getId(), points);
                ligne.setTotalPoints(ligne.getTotalPoints() + points);
            }
        }

        List<LigneClassement> classement = new ArrayList<>(lignesParPilote.values());
        classement.sort(Comparator.comparingDouble(LigneClassement::getTotalPoints).reversed());
        int rang = 1;
        for (LigneClassement ligne : classement) {
            ligne.setRang(rang++);
        }
        return classement;
    }

    /** Classement d'une seule épreuve (pour affichage détaillé), respecte le flag "afficherClassement". */
    public List<Resultat> calculerClassementEpreuve(Epreuve epreuve) throws SQLException {
        List<Resultat> resultats = resultatDAO.listerParEpreuve(epreuve.getId());
        List<Pilote> pilotes = piloteDAO.listerTous();
        Map<Integer, Pilote> piloteParId = new HashMap<>();
        for (Pilote p : pilotes) piloteParId.put(p.getId(), p);
        for (Resultat r : resultats) r.setPilote(piloteParId.get(r.getPiloteId()));

        resultats.sort(Comparator.comparingDouble(Resultat::getPoints).reversed());
        return resultats;
    }
}
