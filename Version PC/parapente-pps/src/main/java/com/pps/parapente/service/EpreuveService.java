package com.pps.parapente.service;

import com.pps.parapente.dao.EpreuveDAO;
import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.model.BaremePoint;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.EpreuveParametre;
import com.pps.parapente.model.ModeCalcul;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Le responsable de l'épreuve peut créer autant d'épreuves qu'il le souhaite,
 * avec les règles de calcul qu'il souhaite (la liste fournie par le club n'est
 * qu'un exemple de départ, rien n'est figé ici).
 */
public class EpreuveService {

    private final EpreuveDAO epreuveDAO = new EpreuveDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    public List<Epreuve> listerToutes() throws SQLException {
        return epreuveDAO.listerToutes();
    }

    public void creer(Epreuve e) throws SQLException {
        valider(e);
        epreuveDAO.creer(e);
        // Par défaut, tous les pilotes déjà inscrits au championnat sont inscrits à la nouvelle épreuve
        inscriptionDAO.inscrireTousLesPilotes(e.getId());
    }

    public void mettreAJour(Epreuve e) throws SQLException {
        valider(e);
        epreuveDAO.mettreAJour(e);
    }

    /**
     * Duplique une épreuve (mais sans les scores des pilotes)
     */
    public Epreuve dupliquer(int idOriginal) throws SQLException {
        Epreuve original = epreuveDAO.trouverParId(idOriginal)
                .orElseThrow(() -> new IllegalStateException("L'épreuve à dupliquer n'existe plus."));
        List<Epreuve> existantes = epreuveDAO.listerToutes();

        Epreuve copie = new Epreuve();
        copie.setNom(nomDeCopie(original.getNom(), existantes));
        copie.setDescription(original.getDescription());
        copie.setModeCalcul(original.getModeCalcul());
        copie.setFormule(original.getFormule());
        copie.setValeurCle(original.getValeurCle());
        copie.setSensClassement(original.getSensClassement());
        copie.setAfficherClassement(original.isAfficherClassement());
        copie.setCompteDansGeneral(original.isCompteDansGeneral());
        copie.setActif(original.isActif());
        copie.setOrdre(existantes.stream().mapToInt(Epreuve::getOrdre).max().orElse(0) + 1);

        List<EpreuveParametre> parametres = new ArrayList<>();
        for (EpreuveParametre p : original.getParametres()) {
            EpreuveParametre np = new EpreuveParametre(p.getNomVariable(), p.getLabel(), p.getUnite());
            np.setObligatoire(p.isObligatoire());
            np.setOrdre(p.getOrdre());
            parametres.add(np);
        }
        copie.setParametres(parametres);

        List<BaremePoint> bareme = new ArrayList<>();
        for (BaremePoint b : original.getBareme()) {
            bareme.add(new BaremePoint(b.getRang(), b.getPoints()));
        }
        copie.setBareme(bareme);

        creer(copie);   // même traitement qu'une nouvelle épreuve avec toute la partie validation + inscription des pilotes déjà inscrits au championnat
        return copie;
    }

    /** exemple de rendu : X (copie), puis X (copie 2), X (copie 3)... si ce nom est déjà pris */
    private String nomDeCopie(String nomOriginal, List<Epreuve> existantes) {
        String base = nomOriginal + " (copie";
        String candidat = base + ")";
        int n = 2;
        while (true) {
            final String c = candidat;
            if (existantes.stream().noneMatch(e -> c.equalsIgnoreCase(e.getNom()))) return candidat;
            candidat = base + " " + n++ + ")";
        }
    }

    public void supprimer(int id) throws SQLException {
        epreuveDAO.supprimer(id);
    }

    private void valider(Epreuve e) {
        if (e.getNom() == null || e.getNom().isBlank())
            throw new IllegalArgumentException("Le nom de l'épreuve est obligatoire");
        if (e.getModeCalcul() == ModeCalcul.FORMULE && (e.getFormule() == null || e.getFormule().isBlank()))
            throw new IllegalArgumentException("La formule de calcul est obligatoire en mode Formule");
        if (e.getModeCalcul() == ModeCalcul.BAREME) {
            if (e.getValeurCle() == null || e.getValeurCle().isBlank())
                throw new IllegalArgumentException("La 'valeur clé' de classement est obligatoire en mode Barème");
            if (e.getBareme() == null || e.getBareme().isEmpty())
                throw new IllegalArgumentException("Le barème de points (rang -> points) est obligatoire en mode Barème");
        }
    }
}
