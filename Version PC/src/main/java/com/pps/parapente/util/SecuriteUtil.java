package com.pps.parapente.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Petit utilitaire de hachage de mot de passe (SHA-256). Suffisant pour un usage club/local. */
public final class SecuriteUtil {

    private SecuriteUtil() {}

    public static String hacher(String motDePasseClair) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(motDePasseClair.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
