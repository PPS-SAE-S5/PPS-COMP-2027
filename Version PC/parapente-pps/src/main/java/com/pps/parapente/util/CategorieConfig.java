package com.pps.parapente.util;

import com.pps.parapente.model.Categorie;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Règle qui déduit la catégorie d'un pilote (pompier / open) à partir de son numéro de pilote
 */
public final class CategorieConfig {

    private static final Path FICHIER = Paths.get("data", "categories.properties");
    private static final String CLE = "numeros.open";

    private static final String CONTENU_PAR_DEFAUT = """
            # Catégories des pilotes (issue #46)
            # ----------------------------------
            # Numéros de pilote réservés aux participants « open » (invités, hors championnat).
            # Tous les autres numéros sont des « pompiers » (participants au championnat).
            #
            # Séparez les numéros par des virgules ; une plage s'écrit début-fin (bornes comprises).
            # Exemple : numeros.open = 46, 100-199
            #
            # La catégorie est proposée automatiquement à l'inscription quand on saisit le numéro,
            # et reste modifiable sur la fiche du pilote. Changer ce fichier ne modifie pas les pilotes
            # déjà enregistrés. Redémarrez l'application après une modification.
            numeros.open =
            """;

    /** Plages [début, fin] des numéros open */
    private static List<int[]> plagesOpen;

    private CategorieConfig() {}

    /** Catégorie déduite du numéro de pilote */
    public static Categorie deduire(Integer numeroPilote) {
        if (numeroPilote == null) return Categorie.POMPIER;
        for (int[] plage : plages()) {
            if (numeroPilote >= plage[0] && numeroPilote <= plage[1]) return Categorie.OPEN;
        }
        return Categorie.POMPIER;
    }

    /** Texte de la règle pour l'afficher à l'utilisateur */
    public static String descriptionNumerosOpen() {
        StringBuilder sb = new StringBuilder();
        for (int[] p : plages()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p[0] == p[1] ? String.valueOf(p[0]) : p[0] + "-" + p[1]);
        }
        return sb.toString();
    }

    private static synchronized List<int[]> plages() {
        if (plagesOpen == null) {
            plagesOpen = analyser(lireRegle());
        }
        return plagesOpen;
    }

    private static String lireRegle() {
        try {
            if (!Files.exists(FICHIER)) {
                if (FICHIER.getParent() != null) Files.createDirectories(FICHIER.getParent());
                Files.writeString(FICHIER, CONTENU_PAR_DEFAUT, StandardCharsets.UTF_8);
                return "";
            }
            Properties props = new Properties();
            try (Reader in = Files.newBufferedReader(FICHIER, StandardCharsets.UTF_8)) {
                props.load(in);
            }
            return props.getProperty(CLE, "");
        } catch (IOException | RuntimeException e) {
            return "";   // fichier illisible : tout le monde est pompier, la catégorie reste modifiable à la main
        }
    }

    /** Analyse : les morceaux invalides sont ignorés */
    static List<int[]> analyser(String regle) {
        List<int[]> plages = new ArrayList<>();
        if (regle == null) return plages;
        for (String morceau : regle.split("[,;]")) {
            String m = morceau.trim();
            if (m.isEmpty()) continue;
            try {
                int tiret = m.indexOf('-', 1);
                if (tiret > 0) {
                    int debut = Integer.parseInt(m.substring(0, tiret).trim());
                    int fin = Integer.parseInt(m.substring(tiret + 1).trim());
                    plages.add(new int[]{Math.min(debut, fin), Math.max(debut, fin)});
                } else {
                    int n = Integer.parseInt(m);
                    plages.add(new int[]{n, n});
                }
            } catch (NumberFormatException ignore) {
                // morceau mal écrit : ignoré
            }
        }
        return plages;
    }

    /** Pour les tests : remplace la règle lue dans le fichier */
    static synchronized void definirRegle(String regle) {
        plagesOpen = analyser(regle);
    }
}
