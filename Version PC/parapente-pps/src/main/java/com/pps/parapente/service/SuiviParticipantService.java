package com.pps.parapente.service;

import com.pps.parapente.dao.EpreuveDAO;
import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.dao.PiloteDAO;
import com.pps.parapente.dao.ResultatDAO;
import com.pps.parapente.model.*;
import com.pps.parapente.model.SuiviParticipant.LigneSuivi;
import com.pps.parapente.model.SuiviParticipant.Statut;

import java.sql.SQLException;
import java.util.*;

/** Prépare les données */
public class SuiviParticipantService {

    private final PiloteDAO piloteDAO = new PiloteDAO();
    private final EpreuveDAO epreuveDAO = new EpreuveDAO();
    private final ResultatDAO resultatDAO = new ResultatDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final ClassementService classementService = new ClassementService();

    public List<Pilote> listerPilotes() throws SQLException {
        List<Pilote> pilotes = new ArrayList<>(piloteDAO.listerTous());
        pilotes.sort(Comparator.comparing((Pilote p) -> texte(p.getNom()).toLowerCase())
                .thenComparing(p -> texte(p.getPrenom()).toLowerCase()));
        return pilotes;
    }

    public SuiviParticipant detailler(Pilote pilote) throws SQLException {
        List<Epreuve> epreuves = epreuveDAO.listerToutes();
        Set<Integer> inscrites = new HashSet<>(inscriptionDAO.listerEpreuvesDuPilote(pilote.getId()));

        Map<Integer, Resultat> resultats = new HashMap<>();
        for (Resultat r : resultatDAO.listerTous()) {
            if (r.getPiloteId() == pilote.getId()) resultats.put(r.getEpreuveId(), r);
        }

        List<LigneSuivi> lignes = new ArrayList<>();
        int saisies = 0;
        int attendues = 0;
        for (Epreuve e : epreuves) {
            Resultat r = resultats.get(e.getId());

            if (!e.isActif() && r == null) continue;
            if (!inscrites.contains(e.getId()) && r == null) continue;

            attendues++;
            if (r == null) {
                lignes.add(new LigneSuivi(e.getNom(), "", null, Statut.PAS_ENCORE_SAISI, e.isCompteDansGeneral()));
            } else {
                saisies++;
                lignes.add(new LigneSuivi(e.getNom(), formaterValeurs(e, r), r.getPoints(),
                        r.isDisqualifie() ? Statut.DISQUALIFIE : Statut.SAISI, e.isCompteDansGeneral()));
            }
        }

        // Total & rang 
        List<LigneClassement> classement = classementService.calculerClassementGeneral(FiltreGenre.MIXTE);
        double total = 0;
        int rang = 0;
        for (LigneClassement l : classement) {
            if (l.getPilote().getId() == pilote.getId()) {
                total = l.getTotalPoints();
                rang = l.getRang();
                break;
            }
        }
        return new SuiviParticipant(pilote, lignes, total, rang, classement.size(), saisies, attendues);
    }

    /** Formate les valeurs d'une épreuve pour l'affichage. Exemple : Temps de marche et nbr balises */
    private String formaterValeurs(Epreuve e, Resultat r) {
        Map<String, EpreuveParametre> parNom = new HashMap<>();
        for (EpreuveParametre p : e.getParametres()) parNom.put(p.getNomVariable(), p);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> v : r.getValeurs().entrySet()) {
            if (sb.length() > 0) sb.append(" ; ");
            EpreuveParametre p = parNom.get(v.getKey());
            String libelle = p == null ? v.getKey() : p.getLabel();
            sb.append(libelle).append(" : ").append(nombre(v.getValue()));
            if (p != null && p.getUnite() != null && !p.getUnite().isBlank() && !libelle.contains("(")) {
                sb.append(' ').append(p.getUnite());
            }
        }
        return sb.toString();
    }

    private String nombre(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private String texte(String s) {
        return s == null ? "" : s;
    }
}
