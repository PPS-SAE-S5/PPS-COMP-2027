package com.pps.parapente.service;

import com.pps.parapente.model.Atelier;
import com.pps.parapente.model.Creneau;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Journee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de ExportPdfService.
 *
 * On ne mocke pas PdfSimple : on génère de vrais PDF en mémoire et on vérifie
 * leur structure (en-tête %PDF, marqueur %%EOF, nombre de pages) ainsi que le
 * comportement face aux cas limites (journée vide, date absente, textes très
 * longs, beaucoup d'épreuves => pagination).
 *
 * genererJournee(...) est package-private : cette classe est dans le même
 * package que le service.
 *
 * Hypothèses : Journee.getDateJournee() renvoie un LocalDate ; Atelier expose
 * getCreneau(), getEpreuves(), getNom(), getLieu(), getResponsable(),
 * getPlageHoraire() ; Creneau a au moins MATIN et un second créneau.
 */
class ExportPdfServiceTest {

    private static final LocalDateTime DATE_EXPORT = LocalDateTime.of(2026, 11, 20, 9, 30);

    private final ExportPdfService service = new ExportPdfService();

    // ---------- helpers ----------

    private Journee journee(String nom, LocalDate date) {
        Journee j = mock(Journee.class);
        when(j.getNom()).thenReturn(nom);
        when(j.getDateJournee()).thenReturn(date);
        return j;
    }

    private Journee journeeStandard() {
        return journee("Finale", LocalDate.of(2026, 6, 15));
    }

    private Atelier atelier(Creneau creneau, String nom, String lieu, String responsable, int nbEpreuves) {
        List<Epreuve> epreuves = new ArrayList<>();
        for (int i = 1; i <= nbEpreuves; i++) {
            Epreuve e = mock(Epreuve.class);
            when(e.getNom()).thenReturn("Épreuve " + i);
            epreuves.add(e);
        }
        Atelier a = mock(Atelier.class);
        when(a.getCreneau()).thenReturn(creneau);
        when(a.getNom()).thenReturn(nom);
        when(a.getLieu()).thenReturn(lieu);
        when(a.getResponsable()).thenReturn(responsable);
        when(a.getPlageHoraire()).thenReturn("08:00 - 12:00");
        when(a.getEpreuves()).thenReturn(epreuves);
        return a;
    }

    private Creneau autreCreneau() {
        return Arrays.stream(Creneau.values()).filter(c -> c != Creneau.MATIN).findFirst().orElseThrow();
    }

    private static String enTexte(byte[] pdf) {
        return new String(pdf, StandardCharsets.ISO_8859_1);
    }

    /**
     * Compte les pages en cherchant les objets "/Type /Page" (et non "/Pages").
     * Suppose que PdfSimple écrit un PDF non compressé en objets simples.
     * Si ce n'est pas le cas, utilisez plutôt PDFBox (voir remarque).
     */
    private static int nombrePages(byte[] pdf) {
        Matcher m = Pattern.compile("/Type\\s*/Page(?!s)").matcher(enTexte(pdf));
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    private static void assertPdfValide(byte[] pdf) {
        assertNotNull(pdf);
        assertTrue(pdf.length > 100, "PDF anormalement petit");
        String texte = enTexte(pdf);
        assertTrue(texte.startsWith("%PDF-"), "doit commencer par %PDF-");
        assertTrue(texte.contains("%%EOF"), "doit se terminer par %%EOF");
    }

    // ---------- nomFichierParDefaut ----------

    @Test
    @DisplayName("Nom de fichier : « Planning - nom - jj-MM-aaaa.pdf »")
    void nomFichier_nomEtDate() {
        assertEquals("Planning - Finale - 15-06-2026.pdf",
                ExportPdfService.nomFichierParDefaut(journee("Finale", LocalDate.of(2026, 6, 15))));
    }

    @Test
    @DisplayName("Nom de fichier : sans date, pas de suffixe de date")
    void nomFichier_sansDate() {
        assertEquals("Planning - Finale.pdf",
                ExportPdfService.nomFichierParDefaut(journee("Finale", null)));
    }

    @Test
    @DisplayName("Nom de fichier : nom null => « journée »")
    void nomFichier_nomNull() {
        assertEquals("Planning - journée.pdf",
                ExportPdfService.nomFichierParDefaut(journee(null, null)));
    }

    @Test
    @DisplayName("Nom de fichier : espaces autour du nom supprimés")
    void nomFichier_nomTrimme() {
        assertEquals("Planning - Finale.pdf",
                ExportPdfService.nomFichierParDefaut(journee("  Finale  ", null)));
    }

    @Test
    @DisplayName("Nom de fichier : caractères interdits (\\ / : * ? \" < > |) remplacés par « - »")
    void nomFichier_caracteresInterdits() {
        String nom = ExportPdfService.nomFichierParDefaut(journee("Jour 1/2: test?", null));

        assertEquals("Planning - Jour 1-2- test-.pdf", nom);
        assertFalse(nom.matches(".*[\\\\/:*?\"<>|].*"));
    }

    // ---------- genererJournee ----------

    @Test
    @DisplayName("Journée standard : PDF valide d'au moins une page")
    void generer_journeeStandard_pdfValide() throws IOException {
        List<Atelier> ateliers = List.of(
                atelier(Creneau.MATIN, "Atelier vitesse", "Terrain A", "M. Dupont", 3),
                atelier(autreCreneau(), "Atelier précision", "Terrain B", "Mme Martin", 2));

        byte[] pdf = service.genererJournee(journeeStandard(), ateliers, DATE_EXPORT);

        assertPdfValide(pdf);
        assertTrue(nombrePages(pdf) >= 1);
    }

    @Test
    @DisplayName("Aucun atelier : PDF valide d'une seule page (créneaux vides)")
    void generer_aucunAtelier_unePage() throws IOException {
        byte[] pdf = service.genererJournee(journeeStandard(), List.of(), DATE_EXPORT);

        assertPdfValide(pdf);
        assertEquals(1, nombrePages(pdf));
    }

    @Test
    @DisplayName("Date de journée absente : pas d'exception")
    void generer_dateAbsente_ok() throws IOException {
        Journee j = journee("Sans date", null);

        byte[] pdf = service.genererJournee(j, List.of(
                atelier(Creneau.MATIN, "Atelier", "Lieu", "Resp", 1)), DATE_EXPORT);

        assertPdfValide(pdf);
    }

    @Test
    @DisplayName("Atelier sans épreuve, lieu et responsable vides : PDF généré")
    void generer_atelierVide_ok() throws IOException {
        Atelier a = atelier(Creneau.MATIN, "Atelier vide", "", "", 0);

        byte[] pdf = service.genererJournee(journeeStandard(), List.of(a), DATE_EXPORT);

        assertPdfValide(pdf);
    }

    @Test
    @DisplayName("Lieu et responsable null : pas d'exception (affichés « — »)")
    void generer_lieuEtResponsableNull_ok() throws IOException {
        Atelier a = atelier(Creneau.MATIN, "Atelier", null, null, 2);

        byte[] pdf = service.genererJournee(journeeStandard(), List.of(a), DATE_EXPORT);

        assertPdfValide(pdf);
    }

    @Test
    @DisplayName("Textes très longs (nom d'atelier, lieu, épreuve) : découpés/tronqués sans erreur")
    void generer_textesTresLongs_ok() throws IOException {
        String long300 = "Très long libellé ".repeat(20);
        Atelier a = atelier(Creneau.MATIN, long300, long300, long300, 2);

        byte[] pdf = service.genererJournee(journee(long300, LocalDate.of(2026, 6, 15)), List.of(a), DATE_EXPORT);

        assertPdfValide(pdf);
    }

    @Test
    @DisplayName("Beaucoup d'ateliers : pagination sur plusieurs pages")
    void generer_beaucoupDAteliers_plusieursPages() throws IOException {
        List<Atelier> ateliers = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            ateliers.add(atelier(i % 2 == 0 ? Creneau.MATIN : autreCreneau(),
                    "Atelier " + i, "Terrain " + i, "Responsable " + i, 4));
        }

        byte[] pdf = service.genererJournee(journeeStandard(), ateliers, DATE_EXPORT);

        assertPdfValide(pdf);
        assertTrue(nombrePages(pdf) > 1, "25 ateliers doivent déborder sur plusieurs pages");
    }

    @Test
    @DisplayName("Un atelier avec énormément d'épreuves : la carte continue sur les pages suivantes")
    void generer_atelierGeant_carteContinueSurPagesSuivantes() throws IOException {
        Atelier geant = atelier(Creneau.MATIN, "Atelier géant", "Terrain", "Resp", 120);

        byte[] pdf = service.genererJournee(journeeStandard(), List.of(geant), DATE_EXPORT);

        assertPdfValide(pdf);
        assertTrue(nombrePages(pdf) >= 3, "120 épreuves dépassent largement une page");
    }

    @Test
    @DisplayName("Plus il y a de contenu, plus il y a de pages")
    void generer_pagesCroissentAvecLeContenu() throws IOException {
        byte[] petit = service.genererJournee(journeeStandard(),
                List.of(atelier(Creneau.MATIN, "A", "L", "R", 2)), DATE_EXPORT);
        byte[] gros = service.genererJournee(journeeStandard(),
                List.of(atelier(Creneau.MATIN, "A", "L", "R", 80)), DATE_EXPORT);

        assertTrue(nombrePages(gros) > nombrePages(petit));
    }

    @Test
    @DisplayName("Deux exports successifs fonctionnent (cache du logo statique)")
    void generer_deuxFois_ok() throws IOException {
        List<Atelier> ateliers = List.of(atelier(Creneau.MATIN, "A", "L", "R", 2));

        assertPdfValide(service.genererJournee(journeeStandard(), ateliers, DATE_EXPORT));
        assertPdfValide(service.genererJournee(journeeStandard(), ateliers, DATE_EXPORT));
    }

    // ---------- exporterJournee ----------

    @Test
    @DisplayName("exporterJournee : écrit un PDF valide dans le fichier cible")
    void exporter_ecritLeFichier(@TempDir Path dossier) throws IOException {
        Path fichier = dossier.resolve("planning.pdf");
        List<Atelier> ateliers = List.of(atelier(Creneau.MATIN, "Atelier", "Terrain", "Resp", 3));

        service.exporterJournee(journeeStandard(), ateliers, fichier);

        assertTrue(Files.exists(fichier));
        assertPdfValide(Files.readAllBytes(fichier));
    }

    @Test
    @DisplayName("exporterJournee : dossier inexistant => IOException")
    void exporter_dossierInexistant_ioException(@TempDir Path dossier) {
        Path fichier = dossier.resolve("inexistant").resolve("planning.pdf");

        assertThrows(IOException.class,
                () -> service.exporterJournee(journeeStandard(), List.of(), fichier));
    }

    @Test
    @DisplayName("exporterJournee : écrase un fichier existant")
    void exporter_ecraseFichierExistant(@TempDir Path dossier) throws IOException {
        Path fichier = dossier.resolve("planning.pdf");
        Files.writeString(fichier, "ancien contenu");

        service.exporterJournee(journeeStandard(), List.of(), fichier);

        assertPdfValide(Files.readAllBytes(fichier));
    }
}