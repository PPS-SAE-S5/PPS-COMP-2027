package com.pps.parapente.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

import com.pps.parapente.model.Categorie;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.ModeCalcul;
import com.pps.parapente.model.Pilote;
import com.pps.parapente.model.SensClassement;

/**
 * Base commune des tests de DAO : chaque test travaille sur une base H2 en mémoire
 * neuve, créée avec le vrai schema.sql. Le fichier data/ n'est jamais touché.
 */
abstract class DaoTestBase {

    private Connection connexion;
    private MockedStatic<DatabaseManager> databaseManager;

    @BeforeEach
    void ouvrirBaseEnMemoire() throws SQLException {
        connexion = DriverManager.getConnection(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        databaseManager = mockStatic(DatabaseManager.class,
                inv -> inv.getMethod().getName().equals("getConnection")
                        ? connexion
                        : inv.callRealMethod());
        creerSchema();
    }

    @AfterEach
    void fermerBase() throws SQLException {
        databaseManager.close();
        connexion.close();
    }

    /** Même script que initialiserSchema(), mais les données initiales (comptes par défaut) sont facultatives. */
    private void creerSchema() throws SQLException {
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/db/schema.sql")) {
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("(?m)^\\s*--.*$", "");
            try (Statement st = connexion.createStatement()) {
                for (String instruction : script.split(";")) {
                    String sql = instruction.trim();
                    if (sql.isEmpty()) continue;
                    try {
                        st.execute(sql);
                    } catch (SQLException e) {
                        String debut = sql.toUpperCase();
                        if (!debut.startsWith("MERGE") && !debut.startsWith("INSERT")) throw e;
                    }
                }
            }
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    protected Pilote nouveauPilote(String licence, String nom) throws SQLException {
        Pilote p = new Pilote();
        p.setNumeroLicence(licence);
        p.setNom(nom);
        p.setPrenom("Jean");
        p.setGenre("H");
        p.setCategorie(Categorie.POMPIER);
        new PiloteDAO().creer(p);
        return p;
    }

    protected Epreuve nouvelleEpreuve(String nom) throws SQLException {
        Epreuve e = new Epreuve();
        e.setNom(nom);
        e.setModeCalcul(ModeCalcul.FORMULE);
        e.setFormule("x");
        e.setSensClassement(SensClassement.DESC);
        e.setActif(true);
        e.setParametres(new ArrayList<>());
        e.setBareme(new ArrayList<>());
        new EpreuveDAO().creer(e);
        return e;
    }
}