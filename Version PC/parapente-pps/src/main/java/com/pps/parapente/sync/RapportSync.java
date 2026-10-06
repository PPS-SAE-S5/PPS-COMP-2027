package com.pps.parapente.sync;

/** Résumé chiffré d'une synchronisation, affiché à l'écran pour que l'utilisateur sache ce qui a bougé. */
public class RapportSync {
    public int utilisateursRecus, utilisateursEnvoyes;
    public int pilotesRecus, pilotesEnvoyes;
    public int epreuvesRecues, epreuvesEnvoyees;
    public int inscriptionsRecues, inscriptionsEnvoyees;
    public int resultatsRecus, resultatsEnvoyes;
    public int pilotesSupprimes, epreuvesSupprimees, inscriptionsSupprimees, resultatsSupprimes;

    public String resume() {
        String base = resumeBase();
        int total = pilotesSupprimes + epreuvesSupprimees + inscriptionsSupprimees + resultatsSupprimes;
        if (total == 0) return base;
        return base + String.format("%n%nSuppressions (pour rendre les deux bases identiques) :%n" +
                        "Pilotes : %d, Épreuves : %d, Inscriptions : %d, Résultats : %d",
                pilotesSupprimes, epreuvesSupprimees, inscriptionsSupprimees, resultatsSupprimes);
    }

    private String resumeBase() {
        return String.format(
                "Comptes utilisateurs : %d reçus, %d envoyés%n" +
                "Pilotes : %d reçus, %d envoyés%n" +
                "Épreuves (+ variables/barèmes) : %d reçues, %d envoyées%n" +
                "Inscriptions : %d reçues, %d envoyées%n" +
                "Résultats : %d reçus, %d envoyés",
                utilisateursRecus, utilisateursEnvoyes,
                pilotesRecus, pilotesEnvoyes, epreuvesRecues, epreuvesEnvoyees,
                inscriptionsRecues, inscriptionsEnvoyees, resultatsRecus, resultatsEnvoyes
        );
    }
}
