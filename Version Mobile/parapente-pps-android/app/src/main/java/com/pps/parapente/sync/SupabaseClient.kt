package com.pps.parapente.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Appels REST bruts vers l'API PostgREST exposée automatiquement par Supabase. Aucune
 * bibliothèque Supabase nécessaire (HttpURLConnection + org.json, tous deux déjà inclus
 * dans Android) : donc aucune nouvelle dépendance Gradle.
 */
class SupabaseClient(url: String, private val cleAnon: String) {

    private val baseUrl = url.trimEnd('/')

    suspend fun recuperer(table: String, select: String): JSONArray = withContext(Dispatchers.IO) {
        val encode = URLEncoder.encode(select, "UTF-8")
        val conn = ouvrir("$baseUrl/rest/v1/$table?select=$encode", "GET")
        val corps = lire(conn)
        verifier(conn, corps, "lecture de la table « $table »")
        JSONArray(corps)
    }

    /** Insère ou met à jour (upsert) selon la ou les colonnes de conflit, renvoie la ligne enregistrée. */
    suspend fun envoyer(table: String, ligne: JSONObject, colonneConflit: String): JSONObject = withContext(Dispatchers.IO) {
        val encode = URLEncoder.encode(colonneConflit, "UTF-8")
        val conn = ouvrir("$baseUrl/rest/v1/$table?on_conflict=$encode", "POST")
        conn.setRequestProperty("Prefer", "resolution=merge-duplicates,return=representation")
        ecrire(conn, JSONArray().put(ligne).toString())
        val corps = lire(conn)
        verifier(conn, corps, "écriture dans la table « $table »")
        val resultat = JSONArray(corps)
        if (resultat.length() == 0) {
            throw RuntimeException("Supabase n'a renvoyé aucune ligne après l'enregistrement dans « $table ».")
        }
        resultat.getJSONObject(0)
    }

    /** Supprime toutes les lignes correspondant à un filtre PostgREST simple (ex: "epreuve_id=eq.12"). */
    suspend fun supprimerOu(table: String, filtre: String): Unit = withContext(Dispatchers.IO) {
        val conn = ouvrir("$baseUrl/rest/v1/$table?$filtre", "DELETE")
        val corps = lire(conn)
        verifier(conn, corps, "suppression dans la table « $table »")
    }

    /** Insertion simple (sans upsert) d'une ou plusieurs lignes. */
    suspend fun inserer(table: String, lignes: JSONArray): Unit = withContext(Dispatchers.IO) {
        if (lignes.length() == 0) return@withContext
        val conn = ouvrir("$baseUrl/rest/v1/$table", "POST")
        ecrire(conn, lignes.toString())
        val corps = lire(conn)
        verifier(conn, corps, "écriture dans la table « $table »")
    }

    private fun ouvrir(urlStr: String, methode: String): HttpURLConnection {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = methode
        conn.connectTimeout = 10_000
        conn.readTimeout = 25_000
        conn.setRequestProperty("apikey", cleAnon)
        conn.setRequestProperty("Authorization", "Bearer $cleAnon")
        conn.setRequestProperty("Content-Type", "application/json")
        return conn
    }

    private fun ecrire(conn: HttpURLConnection, corps: String) {
        conn.doOutput = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(corps) }
    }

    private fun lire(conn: HttpURLConnection): String {
        val flux = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return flux?.let { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() } ?: ""
    }

    private fun verifier(conn: HttpURLConnection, corps: String, contexte: String) {
        if (conn.responseCode >= 300) {
            val message = try { JSONObject(corps).optString("message", corps) } catch (e: Exception) { corps }
            throw RuntimeException("Échec de connexion à Supabase ($contexte, code ${conn.responseCode}) : $message")
        }
    }
}
