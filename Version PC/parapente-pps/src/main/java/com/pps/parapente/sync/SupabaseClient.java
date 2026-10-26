package com.pps.parapente.sync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Appels REST bruts vers l'API PostgREST exposée automatiquement par Supabase au-dessus
 * de la base PostgreSQL. Aucune bibliothèque Supabase nécessaire : le protocole est du HTTP/JSON
 * classique, géré ici avec le client HTTP intégré au JDK (java.net.http), donc aucune nouvelle
 * dépendance Maven n'est nécessaire.
 */
public class SupabaseClient {

    private final String url;
    private final String cleAnon;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public SupabaseClient(String url, String cleAnon) {
        this.url = (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
        this.cleAnon = cleAnon;
    }

    /** Récupère toutes les lignes d'une table. "select" peut inclure des relations imbriquées
     *  (ex: "*,epreuve_parametres(*),bareme_points(*)") grâce aux clés étrangères déjà définies. */
    public JSONArray recuperer(String table, String select) throws Exception {
        String requete = url + "/rest/v1/" + table + "?select=" + URLEncoder.encode(select, StandardCharsets.UTF_8);
        HttpRequest req = requeteBase(requete).GET().build();
        HttpResponse<String> rep = http.send(req, HttpResponse.BodyHandlers.ofString());
        verifier(rep, "lecture de la table « " + table + " »");
        return new JSONArray(rep.body());
    }

    /**
     * Insère ou met à jour (upsert) une ligne en se basant sur la ou les colonnes indiquées
     * (doivent correspondre à une contrainte UNIQUE côté Supabase), et renvoie la ligne telle
     * qu'enregistrée côté serveur — utile pour récupérer son "id" numérique auto-généré.
     */
    public JSONObject envoyer(String table, JSONObject ligne, String colonneConflit) throws Exception {
        String requete = url + "/rest/v1/" + table + "?on_conflict=" + URLEncoder.encode(colonneConflit, StandardCharsets.UTF_8);
        HttpRequest req = requeteBase(requete)
                .header("Prefer", "resolution=merge-duplicates,return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(new JSONArray(List.of(ligne)).toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> rep = http.send(req, HttpResponse.BodyHandlers.ofString());
        verifier(rep, "écriture dans la table « " + table + " »");
        JSONArray resultat = new JSONArray(rep.body());
        if (resultat.isEmpty()) {
            throw new RuntimeException("Supabase n'a renvoyé aucune ligne après l'enregistrement dans « " + table + " ».");
        }
        return resultat.getJSONObject(0);
    }

    /** Supprime toutes les lignes correspondant à un filtre PostgREST simple (ex: "epreuve_id=eq.12"). */
    public void supprimerOu(String table, String filtre) throws Exception {
        String requete = url + "/rest/v1/" + table + "?" + filtre;
        HttpRequest req = requeteBase(requete).DELETE().build();
        HttpResponse<String> rep = http.send(req, HttpResponse.BodyHandlers.ofString());
        verifier(rep, "suppression dans la table « " + table + " »");
    }

    /** Insertion simple (sans upsert) d'une ou plusieurs lignes. */
    public void inserer(String table, JSONArray lignes) throws Exception {
        if (lignes.isEmpty()) return;
        String requete = url + "/rest/v1/" + table;
        HttpRequest req = requeteBase(requete)
                .POST(HttpRequest.BodyPublishers.ofString(lignes.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> rep = http.send(req, HttpResponse.BodyHandlers.ofString());
        verifier(rep, "écriture dans la table « " + table + " »");
    }

    private HttpRequest.Builder requeteBase(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(25))
                .header("apikey", cleAnon)
                .header("Authorization", "Bearer " + cleAnon)
                .header("Content-Type", "application/json");
    }

    private void verifier(HttpResponse<String> rep, String contexte) {
        if (rep.statusCode() >= 300) {
            throw new RuntimeException(
                    "Échec de connexion à Supabase (" + contexte + ", code " + rep.statusCode() + ") : "
                            + extraireMessage(rep.body())
            );
        }
    }

    private String extraireMessage(String corps) {
        try {
            JSONObject json = new JSONObject(corps);
            return json.optString("message", corps);
        } catch (Exception e) {
            return corps;
        }
    }
}
