package com.pps.parapente.util;

/**
 * Traduit les erreurs techniques (base de données, calcul de formule...) en messages
 * compréhensibles en français pour les utilisateurs du club. Aucun message d'erreur
 * technique brut (SQL, Java, anglais) ne doit jamais être affiché directement à l'écran.
 */
public final class MessageErreurUtil {

    private MessageErreurUtil() {}

    public static String traduire(Throwable e) {
        if (e == null) return "Une erreur inconnue est survenue.";

        String messageOriginal = e.getMessage() != null ? e.getMessage() : "";
        String m = messageOriginal.toLowerCase();

        if (m.contains("unique index or primary key violation") || m.contains("duplicate")) {
            if (m.contains("numero_licence") || m.contains("pilotes")) {
                return "Ce numéro de licence est déjà utilisé par un autre pilote.";
            }
            if (m.contains("identifiant") || m.contains("utilisateurs")) {
                return "Cet identifiant de compte est déjà utilisé. Choisissez-en un autre.";
            }
            return "Cette information existe déjà dans la base de données (doublon détecté).";
        }
        if (m.contains("referential integrity constraint violation") || m.contains("foreign key")) {
            return "Impossible de supprimer cet élément : il est encore utilisé ailleurs (résultats, inscriptions...).";
        }
        if (m.contains("null not allowed") || m.contains("not-null")) {
            return "Un champ obligatoire n'a pas été renseigné. Merci de vérifier votre saisie.";
        }
        if (m.contains("data conversion error") || m.contains("invalid character value")
                || m.contains("for input string")) {
            return "Une valeur saisie n'est pas au bon format (un nombre est attendu).";
        }
        if (m.contains("variable") && (m.contains("undefined") || m.contains("not found") || m.contains("unknown"))) {
            return "La formule utilise un nom de variable inconnu ou mal orthographié. " +
                    "Vérifiez qu'il correspond exactement à une variable définie pour cette épreuve.";
        }
        if (e instanceof NumberFormatException) {
            return "Une valeur numérique attendue n'est pas valide.";
        }
        if (e instanceof java.sql.SQLException) {
            return "Impossible d'accéder à la base de données locale (fichier peut-être ouvert ailleurs). " +
                    "Fermez les autres instances de l'application puis réessayez.";
        }
        if (e instanceof IllegalStateException || e instanceof IllegalArgumentException) {
            // Ces exceptions sont levées volontairement avec des messages déjà en français
            // et déjà lisibles dans nos propres services (CalculService, EpreuveService, PiloteService).
            return messageOriginal.isBlank() ? "La demande n'a pas pu être traitée." : messageOriginal;
        }

        return "Une erreur technique est survenue. Merci de réessayer ; " +
                "si le problème persiste, contactez la personne en charge de l'application.";
    }
}
