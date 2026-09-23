package com.pps.parapente.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gère la connexion à la base H2 embarquée, stockée en LOCAL dans un fichier
 * (dossier "data/" à côté de l'application). Aucune connexion internet n'est requise :
 * cela répond à l'exigence "stockage des données en local en cas d'absence d'internet".
 */
public final class DatabaseManager {

    // Fichier local : data/pps_championnat.mv.db
    private static final String DATA_DIR = "data";
    private static final String DB_NAME = "pps_championnat";
    private static final String URL = "jdbc:h2:file:./" + DATA_DIR + "/" + DB_NAME + ";AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static Connection connexionUnique;

    private DatabaseManager() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connexionUnique == null || connexionUnique.isClosed()) {
            try {
                Files.createDirectories(Paths.get(DATA_DIR));
            } catch (IOException e) {
                throw new SQLException("Impossible de créer le dossier de données local", e);
            }
            connexionUnique = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connexionUnique;
    }

    /**
     * Initialise le schéma (tables) à partir de db/schema.sql si elles n'existent pas encore.
     * Appelé une fois au démarrage de l'application.
     */
    public static void initialiserSchema() {
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/db/schema.sql")) {
            if (in == null) {
                throw new RuntimeException("Fichier /db/schema.sql introuvable dans les ressources");
            }
            String script = new String(in.readAllBytes());
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                // H2 accepte l'exécution de plusieurs instructions séparées par ';' via execute()
                for (String instruction : script.split(";")) {
                    String sql = instruction.trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation de la base locale", e);
        }
    }

    public static Path getDataDirectory() {
        return Paths.get(DATA_DIR).toAbsolutePath();
    }
}
