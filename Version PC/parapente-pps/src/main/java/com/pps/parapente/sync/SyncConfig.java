package com.pps.parapente.sync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Paramètres de connexion à Supabase, enregistrés dans data/sync.properties
 * (à côté de la base locale). Volontairement hors du code source : le club peut
 * changer d'URL/clé sans recompiler l'application, et sans mettre la clé dans le dépôt Git.
 */
public class SyncConfig {

    private static final Path FICHIER = Paths.get("data", "sync.properties");
    public static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String url = "";
    private String cleAnon = "";
    private boolean syncAuto = false;
    private String derniereSync = "";

    public static SyncConfig charger() {
        SyncConfig config = new SyncConfig();
        if (Files.exists(FICHIER)) {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(FICHIER)) {
                props.load(in);
                config.url = props.getProperty("supabase.url", "");
                config.cleAnon = props.getProperty("supabase.anonKey", "");
                config.syncAuto = Boolean.parseBoolean(props.getProperty("supabase.autoSync", "false"));
                config.derniereSync = props.getProperty("supabase.derniereSync", "");
            } catch (IOException ignorée) {
                // Fichier illisible : on repart sur une configuration vide plutôt que de bloquer l'application.
            }
        }
        return config;
    }

    public void sauvegarder() {
        Properties props = new Properties();
        props.setProperty("supabase.url", url == null ? "" : url.trim());
        props.setProperty("supabase.anonKey", cleAnon == null ? "" : cleAnon.trim());
        props.setProperty("supabase.autoSync", String.valueOf(syncAuto));
        props.setProperty("supabase.derniereSync", derniereSync == null ? "" : derniereSync);
        try {
            if (FICHIER.getParent() != null) Files.createDirectories(FICHIER.getParent());
            try (OutputStream out = Files.newOutputStream(FICHIER)) {
                props.store(out, "Configuration de synchronisation Supabase - PPS Championnat (ne pas partager publiquement)");
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'enregistrer la configuration de synchronisation.", e);
        }
    }

    public boolean estConfigure() {
        return url != null && !url.isBlank() && cleAnon != null && !cleAnon.isBlank();
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCleAnon() { return cleAnon; }
    public void setCleAnon(String cleAnon) { this.cleAnon = cleAnon; }

    public boolean isSyncAuto() { return syncAuto; }
    public void setSyncAuto(boolean syncAuto) { this.syncAuto = syncAuto; }

    public String getDerniereSync() { return derniereSync; }
    public void setDerniereSync(String derniereSync) { this.derniereSync = derniereSync; }
}
