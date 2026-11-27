package com.pps.parapente.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de SecuriteUtil.
 *
 * Les valeurs attendues sont les vecteurs de test publics de SHA-256
 * (référence indépendante de l'implémentation), plus une comparaison avec
 * MessageDigest pour les textes accentués.
 *
 * Ce fichier doit être dans le package com.pps.parapente.util (côté test).
 */
class SecuriteUtilTest {

    private static String sha256Reference(String texte) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(texte.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Test
    @DisplayName("Vecteurs de test SHA-256 connus (chaîne vide, « abc », « hello », « password »)")
    void hacher_vecteursConnus() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", SecuriteUtil.hacher(""));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", SecuriteUtil.hacher("abc"));
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", SecuriteUtil.hacher("hello"));
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", SecuriteUtil.hacher("password"));
    }

    @Test
    @DisplayName("Format : 64 caractères hexadécimaux en minuscules")
    void hacher_format() {
        String hache = SecuriteUtil.hacher("MotDePasse!2026");

        assertEquals(64, hache.length());
        assertTrue(hache.matches("[0-9a-f]{64}"));
    }

    @Test
    @DisplayName("Format : toujours 64 caractères (les octets < 0x10 gardent leur zéro initial)")
    void hacher_zerosInitiauxConserves() {
        for (int i = 0; i < 2000; i++) {
            String hache = SecuriteUtil.hacher("mot-de-passe-" + i);
            assertEquals(64, hache.length(), "longueur incorrecte pour l'entrée " + i);
            assertTrue(hache.matches("[0-9a-f]{64}"));
        }
    }

    @Test
    @DisplayName("Déterministe : même entrée => même haché")
    void hacher_deterministe() {
        assertEquals(SecuriteUtil.hacher("secret123"), SecuriteUtil.hacher("secret123"));
    }

    @Test
    @DisplayName("Entrées différentes => hachés différents (sensible à la casse et aux espaces)")
    void hacher_entreesDifferentes() {
        Set<String> hachés = new HashSet<>();
        hachés.add(SecuriteUtil.hacher("secret"));
        hachés.add(SecuriteUtil.hacher("Secret"));
        hachés.add(SecuriteUtil.hacher("secret "));
        hachés.add(SecuriteUtil.hacher(" secret"));
        hachés.add(SecuriteUtil.hacher("secret1"));

        assertEquals(5, hachés.size());
    }

    @Test
    @DisplayName("Le haché ne contient pas le mot de passe en clair")
    void hacher_neContientPasLeClair() {
        assertFalse(SecuriteUtil.hacher("secret123").contains("secret123"));
    }

    @Test
    @DisplayName("Textes accentués et symboles encodés en UTF-8 (identique à MessageDigest + UTF-8)")
    void hacher_utf8() throws Exception {
        String[] textes = {"Élodie", "mot de passe é à ç", "pässwörd", "密码", "€uro 🙂"};
        for (String texte : textes) {
            assertEquals(sha256Reference(texte), SecuriteUtil.hacher(texte), "écart pour : " + texte);
        }
    }

    @Test
    @DisplayName("Mot de passe très long : pas de limite, haché de taille fixe")
    void hacher_motDePasseLong() {
        String hache = SecuriteUtil.hacher("a".repeat(100_000));

        assertEquals(64, hache.length());
    }

    @Test
    @DisplayName("Entrée null : NullPointerException (comportement actuel)")
    void hacher_null() {
        assertThrows(NullPointerException.class, () -> SecuriteUtil.hacher(null));
    }
}