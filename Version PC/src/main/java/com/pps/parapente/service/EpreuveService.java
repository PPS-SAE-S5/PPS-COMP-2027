package com.pps.parapente.service;

import com.pps.parapente.dao.EpreuveDAO;
import com.pps.parapente.dao.InscriptionDAO;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.ModeCalcul;

import java.sql.SQLException;
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
