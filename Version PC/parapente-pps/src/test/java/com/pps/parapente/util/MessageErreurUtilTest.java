package com.pps.parapente.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de MessageErreurUtil.
 *
 * Logique pure (aucun mock). Les messages de base de données sont ceux de H2,
 * tels que les produit le moteur (« Unique index or primary key violation... »).
 * Le test vérifie surtout l'ORDRE des règles, qui est le point fragile :
 * synchronisation Supabase avant doublons, formule avant IllegalArgumentException, etc.
 */
class MessageErreurUtilTest {

    private static final String MESSAGE_TECHNIQUE_GENERIQUE =
            "Une erreur technique est survenue. Merci de réessayer ; "
                    + "si le problème persiste, contactez la personne en charge de l'application.";

    // =====================================================================
    // Cas de base
    // =====================================================================

    @Test
    @DisplayName("null => message d'erreur inconnue")
    void traduire_null() {
        assertEquals("Une erreur inconnue est survenue.", MessageErreurUtil.traduire(null));
    }

    @Test
    @DisplayName("Exception quelconque => message technique générique, sans fuite du message brut")
    void traduire_exceptionInconnue_generique() {
        String message = MessageErreurUtil.traduire(new RuntimeException("NullPointerException at Foo.bar(Foo.java:12)"));

        assertEquals(MESSAGE_TECHNIQUE_GENERIQUE, message);
        assertFalse(message.contains("Foo.java"));
    }

    @Test
    @DisplayName("Exception sans message => message technique générique")
    void traduire_exceptionSansMessage() {
        assertEquals(MESSAGE_TECHNIQUE_GENERIQUE, MessageErreurUtil.traduire(new RuntimeException()));
    }

    // =====================================================================
    // Réseau / Supabase
    // =====================================================================

    @Test
    @DisplayName("Timeout HTTP => « Supabase ne répond pas »")
    void traduire_timeoutHttp() {
        String message = MessageErreurUtil.traduire(new HttpTimeoutException("request timed out"));

        assertTrue(message.startsWith("Supabase ne répond pas"));
    }

    @Test
    @DisplayName("Timeout socket => « Supabase ne répond pas »")
    void traduire_timeoutSocket() {
        String message = MessageErreurUtil.traduire(new SocketTimeoutException("Read timed out"));

        assertTrue(message.startsWith("Supabase ne répond pas"));
    }

    @Test
    @DisplayName("Timeout enveloppé dans une autre exception (cause) => détecté")
    void traduire_timeoutDansLaCause() {
        Exception enveloppe = new RuntimeException("échec", new SocketTimeoutException());

        assertTrue(MessageErreurUtil.traduire(enveloppe).startsWith("Supabase ne répond pas"));
    }

    @Test
    @DisplayName("Connexion impossible (ConnectException, UnknownHost, NoRoute, UnresolvedAddress) => « Impossible de joindre Supabase »")
    void traduire_connexionImpossible() {
        Throwable[] erreurs = {
                new ConnectException("Connection refused"),
                new UnknownHostException("xxxx.supabase.co"),
                new NoRouteToHostException("No route to host"),
                new UnresolvedAddressException()
        };
        for (Throwable e : erreurs) {
            assertTrue(MessageErreurUtil.traduire(e).startsWith("Impossible de joindre Supabase"),
                    "type non reconnu : " + e.getClass().getSimpleName());
        }
    }

    @Test
    @DisplayName("Connexion impossible dans la cause d'une IOException => détectée")
    void traduire_connexionImpossibleDansLaCause() {
        Exception enveloppe = new IOException("échec d'envoi", new UnknownHostException("x"));

        assertTrue(MessageErreurUtil.traduire(enveloppe).startsWith("Impossible de joindre Supabase"));
    }

    @Test
    @DisplayName("URL Supabase invalide (IllegalArgumentException) => message sur l'adresse")
    void traduire_urlInvalide() {
        String[] messages = {
                "Illegal character in authority at index 8: https://exemple .supabase.co",
                "Expected scheme name at index 0: :foo",
                "URL must be absolute"
        };
        for (String texte : messages) {
            String message = MessageErreurUtil.traduire(new IllegalArgumentException(texte));
            assertTrue(message.startsWith("L'adresse (URL) Supabase n'est pas valide"), texte);
        }
    }

    @Test
    @DisplayName("Message de synchronisation déjà en français => renvoyé tel quel")
    void traduire_messageSupabase_renvoyeTelQuel() {
        String texte = "Supabase a refusé la requête (HTTP 401) : clé API invalide.";

        assertEquals(texte, MessageErreurUtil.traduire(new RuntimeException(texte)));
    }

    @Test
    @DisplayName("Message contenant « synchronisation » => renvoyé tel quel")
    void traduire_messageSynchronisation_renvoyeTelQuel() {
        String texte = "La synchronisation a échoué : 3 pilotes non envoyés.";

        assertEquals(texte, MessageErreurUtil.traduire(new RuntimeException(texte)));
    }

    @Test
    @DisplayName("Priorité : un message Supabase contenant « duplicate key » n'est PAS pris pour un doublon local")
    void traduire_supabaseDuplicate_pasDoublonLocal() {
        String texte = "Supabase (HTTP 409) : duplicate key value violates unique constraint numero_licence";

        assertEquals(texte, MessageErreurUtil.traduire(new RuntimeException(texte)));
    }

    // =====================================================================
    // Doublons (base locale)
    // =====================================================================

    @Test
    @DisplayName("Doublon sur numero_pilote => numéro de pilote déjà attribué")
    void traduire_doublonNumeroPilote() {
        SQLException e = new SQLException(
                "Unique index or primary key violation: \"PUBLIC.IDX_PILOTES_NUMERO_PILOTE_INDEX_2 ON PUBLIC.PILOTES(NUMERO_PILOTE NULLS FIRST) VALUES 7\"");

        assertEquals("Ce numéro de pilote est déjà attribué à un autre pilote.", MessageErreurUtil.traduire(e));
    }

    @Test
    @DisplayName("Doublon sur numero_licence => numéro de licence déjà utilisé")
    void traduire_doublonNumeroLicence() {
        SQLException e = new SQLException(
                "Unique index or primary key violation: \"PUBLIC.CONSTRAINT_INDEX_4 ON PUBLIC.PILOTES(NUMERO_LICENCE NULLS FIRST) VALUES 'LIC-001'\"");

        assertEquals("Ce numéro de licence est déjà utilisé par un autre pilote.", MessageErreurUtil.traduire(e));
    }

    @Test
    @DisplayName("Doublon sur la table pilotes (sans nom de colonne) => message licence")
    void traduire_doublonTablePilotes() {
        SQLException e = new SQLException("Duplicate entry in table PILOTES");

        assertEquals("Ce numéro de licence est déjà utilisé par un autre pilote.", MessageErreurUtil.traduire(e));
    }

    @Test
    @DisplayName("Doublon sur identifiant ou table utilisateurs => identifiant de compte déjà utilisé")
    void traduire_doublonIdentifiant() {
        SQLException parColonne = new SQLException("Unique index or primary key violation: ... (IDENTIFIANT) ...");
        SQLException parTable = new SQLException("Unique index or primary key violation: PUBLIC.UTILISATEURS ...");

        String attendu = "Cet identifiant de compte est déjà utilisé. Choisissez-en un autre.";
        assertEquals(attendu, MessageErreurUtil.traduire(parColonne));
        assertEquals(attendu, MessageErreurUtil.traduire(parTable));
    }

    @Test
    @DisplayName("Autre doublon => message générique de doublon")
    void traduire_doublonGenerique() {
        SQLException e = new SQLException("Unique index or primary key violation: PUBLIC.EPREUVES(NOM)");

        assertEquals("Cette information existe déjà dans la base de données (doublon détecté).",
                MessageErreurUtil.traduire(e));
    }

    @Test
    @DisplayName("Doublon reconnu aussi pour une exception non-SQL contenant « duplicate »")
    void traduire_doublonSansSqlException() {
        assertEquals("Cette information existe déjà dans la base de données (doublon détecté).",
                MessageErreurUtil.traduire(new RuntimeException("Duplicate key")));
    }

    // =====================================================================
    // Autres erreurs de base de données
    // =====================================================================

    @Test
    @DisplayName("Contrainte de clé étrangère => impossible de supprimer, encore utilisé")
    void traduire_cleEtrangere() {
        String attendu = "Impossible de supprimer cet élément : il est encore utilisé ailleurs (résultats, inscriptions...).";

        assertEquals(attendu, MessageErreurUtil.traduire(
                new SQLException("Referential integrity constraint violation: \"FK_RESULTATS_PILOTE\"")));
        assertEquals(attendu, MessageErreurUtil.traduire(new SQLException("violates foreign key constraint")));
    }

    @Test
    @DisplayName("Champ NULL interdit => champ obligatoire non renseigné")
    void traduire_champObligatoire() {
        String attendu = "Un champ obligatoire n'a pas été renseigné. Merci de vérifier votre saisie.";

        assertEquals(attendu, MessageErreurUtil.traduire(new SQLException("NULL not allowed for column \"NOM\"")));
        assertEquals(attendu, MessageErreurUtil.traduire(new SQLException("violates not-null constraint")));
    }

    @Test
    @DisplayName("Erreurs de conversion => « pas au bon format »")
    void traduire_erreurConversion() {
        String attendu = "Une valeur saisie n'est pas au bon format (un nombre est attendu).";

        assertEquals(attendu, MessageErreurUtil.traduire(new SQLException("Data conversion error converting \"abc\"")));
        assertEquals(attendu, MessageErreurUtil.traduire(new SQLException("Invalid character value for cast")));
        assertEquals(attendu, MessageErreurUtil.traduire(new NumberFormatException("For input string: \"abc\"")));
    }

    @Test
    @DisplayName("NumberFormatException sans message => « valeur numérique attendue non valide »")
    void traduire_numberFormatExceptionSansMessage() {
        assertEquals("Une valeur numérique attendue n'est pas valide.",
                MessageErreurUtil.traduire(new NumberFormatException()));
    }

    @Test
    @DisplayName("Autre SQLException => base locale inaccessible")
    void traduire_sqlExceptionGenerique() {
        String message = MessageErreurUtil.traduire(new SQLException("Database may be already in use"));

        assertTrue(message.startsWith("Impossible d'accéder à la base de données locale"));
        assertFalse(message.contains("already in use"));
    }

    // =====================================================================
    // Formules
    // =====================================================================

    @Test
    @DisplayName("Variable inconnue / non définie dans une formule => message dédié")
    void traduire_variableInconnue() {
        String[] messages = {
                "Variable 'temps' is undefined",
                "variable temps not found",
                "Unknown variable: temsp"
        };
        for (String texte : messages) {
            String message = MessageErreurUtil.traduire(new RuntimeException(texte));
            assertTrue(message.startsWith("La formule utilise un nom de variable inconnu"), texte);
        }
    }

    @Test
    @DisplayName("Priorité : variable inconnue levée en IllegalArgumentException => message dédié, pas le message brut")
    void traduire_variableInconnueEnIllegalArgument() {
        String message = MessageErreurUtil.traduire(new IllegalArgumentException("Variable 'temps' not found"));

        assertTrue(message.startsWith("La formule utilise un nom de variable inconnu"));
        assertFalse(message.contains("not found"));
    }

    // =====================================================================
    // Messages volontairement en français (nos propres services)
    // =====================================================================

    @Test
    @DisplayName("IllegalArgumentException avec message français => renvoyé tel quel")
    void traduire_illegalArgument_messageConserve() {
        String texte = "Le nom de l'épreuve est obligatoire";

        assertEquals(texte, MessageErreurUtil.traduire(new IllegalArgumentException(texte)));
    }

    @Test
    @DisplayName("IllegalStateException avec message français => renvoyé tel quel")
    void traduire_illegalState_messageConserve() {
        String texte = "L'épreuve à dupliquer n'existe plus.";

        assertEquals(texte, MessageErreurUtil.traduire(new IllegalStateException(texte)));
    }

    @Test
    @DisplayName("IllegalArgument/IllegalState sans message ou blanc => « La demande n'a pas pu être traitée »")
    void traduire_illegalSansMessage() {
        String attendu = "La demande n'a pas pu être traitée.";

        assertEquals(attendu, MessageErreurUtil.traduire(new IllegalArgumentException()));
        assertEquals(attendu, MessageErreurUtil.traduire(new IllegalStateException("   ")));
    }

    @Test
    @DisplayName("Le message multi-lignes d'un atelier invalide (avec puces) est conservé")
    void traduire_messageMultiLignesConserve() {
        String texte = "L'atelier ne peut pas être enregistré, champs obligatoires manquants :\n  • lieu\n  • responsable";

        assertEquals(texte, MessageErreurUtil.traduire(new IllegalArgumentException(texte)));
    }
}