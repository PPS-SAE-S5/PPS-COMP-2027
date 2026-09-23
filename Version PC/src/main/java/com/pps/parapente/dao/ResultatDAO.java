package com.pps.parapente.dao;

import com.pps.parapente.model.Resultat;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class ResultatDAO {

    public List<Resultat> listerParEpreuve(int epreuveId) throws SQLException {
        List<Resultat> liste = new ArrayList<>();
        String sql = "SELECT * FROM resultats WHERE epreuve_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, epreuveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    public Optional<Resultat> trouver(int epreuveId, int piloteId) throws SQLException {
        String sql = "SELECT * FROM resultats WHERE epreuve_id = ? AND pilote_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, epreuveId);
            ps.setInt(2, piloteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        }
        return Optional.empty();
    }

    /** Crée ou met à jour le résultat (unique par couple épreuve/pilote). */
    public void enregistrer(Resultat r) throws SQLException {
        Optional<Resultat> existant = trouver(r.getEpreuveId(), r.getPiloteId());
        JSONObject json = new JSONObject(r.getValeurs());
        if (existant.isPresent()) {
            String sql = "UPDATE resultats SET valeurs_json=?, points=?, disqualifie=?, saisi_par=?, date_saisie=CURRENT_TIMESTAMP " +
                    "WHERE epreuve_id=? AND pilote_id=?";
            try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, json.toString());
                ps.setDouble(2, r.getPoints());
                ps.setBoolean(3, r.isDisqualifie());
                ps.setString(4, r.getSaisiPar());
                ps.setInt(5, r.getEpreuveId());
                ps.setInt(6, r.getPiloteId());
                ps.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO resultats (epreuve_id, pilote_id, valeurs_json, points, disqualifie, saisi_par) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, r.getEpreuveId());
                ps.setInt(2, r.getPiloteId());
                ps.setString(3, json.toString());
                ps.setDouble(4, r.getPoints());
                ps.setBoolean(5, r.isDisqualifie());
                ps.setString(6, r.getSaisiPar());
                ps.executeUpdate();
            }
        }
    }

    public void supprimer(int epreuveId, int piloteId) throws SQLException {
        String sql = "DELETE FROM resultats WHERE epreuve_id=? AND pilote_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, epreuveId);
            ps.setInt(2, piloteId);
            ps.executeUpdate();
        }
    }

    /** Tous les résultats du championnat (utilisé pour le classement général). */
    public List<Resultat> listerTous() throws SQLException {
        List<Resultat> liste = new ArrayList<>();
        String sql = "SELECT * FROM resultats";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    private Resultat mapper(ResultSet rs) throws SQLException {
        Resultat r = new Resultat();
        r.setId(rs.getInt("id"));
        r.setEpreuveId(rs.getInt("epreuve_id"));
        r.setPiloteId(rs.getInt("pilote_id"));
        String json = rs.getString("valeurs_json");
        if (json != null && !json.isBlank()) {
            JSONObject obj = new JSONObject(json);
            LinkedHashMap<String, Double> valeurs = new LinkedHashMap<>();
            for (String key : obj.keySet()) {
                valeurs.put(key, obj.getDouble(key));
            }
            r.setValeurs(valeurs);
        }
        r.setPoints(rs.getDouble("points"));
        r.setDisqualifie(rs.getBoolean("disqualifie"));
        r.setSaisiPar(rs.getString("saisi_par"));
        Timestamp ts = rs.getTimestamp("date_saisie");
        if (ts != null) r.setDateSaisie(ts.toLocalDateTime());
        return r;
    }
}
