package com.pps.parapente.sync;

/** Résumé chiffré d'une synchronisation, affiché à l'écran pour que l'utilisateur sache ce qui a bougé. */
public class RapportSync {
    public int utilisateursRecus, utilisateursEnvoyes;
    public int pilotesRecus, pilotesEnvoyes;
    public int epreuvesRecues, epreuvesEnvoyees;
    public int inscriptionsRecues, inscriptionsEnvoyees;
    public int resultatsRecus, resultatsEnvoyes;
    public int planningRecu, planningEnvoye, planningSupprime;   // journées + ateliers
    public int pilotesSupprimes, epreuvesSupprimees, inscriptionsSupprimees, resultatsSupprimes, utilisateursSupprimes;

    /** Message d'alerte non bloquant (ex: Supabase a refusé une suppression). Null si tout s'est bien passé. */
    public String avertissement;

    /** Ajoute un avertissement à la suite d'un éventuel avertissement existant. */
    public void ajouterAvertissement(String message) {
        avertissement = (avertissement == null || avertissement.isBlank()) ? message : avertissement + "\n" + message;
    }

    public String resume() {
        StringBuilder sb = new StringBuilder(resumeBase());
        int total = pilotesSupprimes + epreuvesSupprimees + inscriptionsSupprimees + resultatsSupprimes + utilisateursSupprimes + planningSupprime;
        if (total > 0) {
            sb.append(String.format("%n%nSuppressions (pour rendre les deux bases identiques) :%n" +
                            "Pilotes : %d, Épreuves : %d, Inscriptions : %d, Résultats : %d, Comptes : %d, Journées/ateliers : %d",
                    pilotesSupprimes, epreuvesSupprimees, inscriptionsSupprimees, resultatsSupprimes, utilisateursSupprimes, planningSupprime));
        }
        if (avertissement != null && !avertissement.isBlank()) {
            sb.append(String.format("%n%n⚠ %s", avertissement));
        }
        return sb.toString();
    }

    private String resumeBase() {
        return String.format(
                "Comptes utilisateurs : %d reçus, %d envoyés%n" +
                "Pilotes : %d reçus, %d envoyés%n" +
                "Épreuves (+ variables/barèmes) : %d reçues, %d envoyées%n" +
                "Inscriptions : %d reçues, %d envoyées%n" +
                "Planning (journées + ateliers) : %d reçus, %d envoyés%n" +
                "Résultats : %d reçus, %d envoyés",
                utilisateursRecus, utilisateursEnvoyes,
                pilotesRecus, pilotesEnvoyes, epreuvesRecues, epreuvesEnvoyees,
                inscriptionsRecues, inscriptionsEnvoyees, planningRecu, planningEnvoye, resultatsRecus, resultatsEnvoyes
        );
    }
}
