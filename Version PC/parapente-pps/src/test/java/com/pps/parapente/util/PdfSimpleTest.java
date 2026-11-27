package com.pps.parapente.util;

import com.pps.parapente.util.PdfSimple.Couleur;
import com.pps.parapente.util.PdfSimple.Police;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de PdfSimple.
 *
 * Logique pure, sans mock ni dépendance externe :
 * - mesure et découpe du texte (méthodes statiques) : on vérifie des propriétés
 *   (largeur max respectée, aucun texte perdu) plus quelques valeurs exactes
 *   déduites des tables de largeurs Helvetica ;
 * - génération : le PDF produit n'est pas compressé, on peut donc lire le
 *   contenu des pages et vérifier la structure (objets, table xref, /Count...).
 *   Un PDF est décodé en ISO-8859-1 pour que 1 octet = 1 caractère : les
 *   positions de la table xref (en octets) sont alors des indices de chaîne.
 *
 * Ce fichier doit être dans le package com.pps.parapente.util (côté test).
 */
class PdfSimpleTest {

    private static final Couleur ROUGE = new Couleur(255, 0, 0);
    private static final Couleur BLEU = new Couleur(0, 0, 255);
    private static final Couleur NOIR = new Couleur(0, 0, 0);

    // ---------- helpers ----------

    private static byte[] octets(PdfSimple pdf) throws IOException {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        pdf.ecrire(sortie);
        return sortie.toByteArray();
    }

    private static String rendu(PdfSimple pdf) throws IOException {
        return new String(octets(pdf), StandardCharsets.ISO_8859_1);
    }

    /** Contenu (flux) de chaque page, dans l'ordre des pages. */
    private static List<String> contenusPages(String pdf) {
        List<String> contenus = new ArrayList<>();
        Matcher m = Pattern.compile("<< /Length (\\d+) >>\\nstream\\n").matcher(pdf);
        while (m.find()) {
            int longueur = Integer.parseInt(m.group(1));
            contenus.add(pdf.substring(m.end(), m.end() + longueur));
        }
        return contenus;
    }

    /** Vérifie startxref, l'en-tête xref, /Size et que chaque entrée pointe sur « N 0 obj ». */
    private static void assertXrefCoherente(byte[] octets) {
        String pdf = new String(octets, StandardCharsets.ISO_8859_1);

        Matcher startxref = Pattern.compile("startxref\\n(\\d+)\\n%%EOF").matcher(pdf);
        assertTrue(startxref.find(), "startxref introuvable");
        int debutXref = Integer.parseInt(startxref.group(1));
        assertTrue(pdf.startsWith("xref\n", debutXref), "startxref ne pointe pas sur « xref »");

        Matcher entete = Pattern.compile("xref\\n0 (\\d+)\\n").matcher(pdf);
        assertTrue(entete.find(debutXref));
        int nbEntrees = Integer.parseInt(entete.group(1)); // objets + entrée libre n°0
        assertTrue(pdf.contains("/Size " + nbEntrees + " "), "/Size incohérent avec la table xref");

        int debutEntrees = entete.end();
        assertEquals("0000000000 65535 f \n", pdf.substring(debutEntrees, debutEntrees + 20));
        for (int numero = 1; numero < nbEntrees; numero++) {
            int pos = debutEntrees + 20 * numero;
            int offset = Integer.parseInt(pdf.substring(pos, pos + 10));
            assertTrue(pdf.startsWith(numero + " 0 obj\n", offset),
                    "l'entrée xref de l'objet " + numero + " ne pointe pas sur son objet");
            assertTrue(pdf.startsWith(" 00000 n \n", pos + 10));
        }
    }

    // =====================================================================
    // Couleur.hex
    // =====================================================================

    @Test
    @DisplayName("Couleur.hex : avec ou sans « # », majuscules ou minuscules")
    void couleurHex_formatsValides() {
        assertEquals(new Couleur(27, 58, 92), Couleur.hex("#1b3a5c"));
        assertEquals(new Couleur(27, 58, 92), Couleur.hex("1B3A5C"));
        assertEquals(new Couleur(255, 255, 255), Couleur.hex("#ffffff"));
        assertEquals(new Couleur(0, 0, 0), Couleur.hex("#000000"));
    }

    @Test
    @DisplayName("Couleur.hex : valeur invalide => exception (pas de couleur silencieusement fausse)")
    void couleurHex_invalide() {
        assertThrows(NumberFormatException.class, () -> Couleur.hex("#zzzzzz"));
        assertThrows(RuntimeException.class, () -> Couleur.hex("#fff")); // forme courte non supportée
    }

    // =====================================================================
    // largeur
    // =====================================================================

    @Test
    @DisplayName("largeur : null ou vide => 0")
    void largeur_nullOuVide() {
        assertEquals(0f, PdfSimple.largeur(null, Police.NORMAL, 10));
        assertEquals(0f, PdfSimple.largeur("", Police.NORMAL, 10));
    }

    @Test
    @DisplayName("largeur : valeurs Helvetica connues (A = 667, chiffre = 556, espace = 278)")
    void largeur_valeursConnues() {
        assertEquals(6.67f, PdfSimple.largeur("A", Police.NORMAL, 10), 1e-3f);
        assertEquals(26.68f, PdfSimple.largeur("AA", Police.NORMAL, 20), 1e-3f);
        assertEquals(55.6f, PdfSimple.largeur("0123456789", Police.NORMAL, 10), 1e-3f);
        assertEquals(3.336f, PdfSimple.largeur(" ", Police.NORMAL, 12), 1e-3f);
    }

    @Test
    @DisplayName("largeur : la police grasse est plus large (A = 722 en gras)")
    void largeur_gras() {
        assertEquals(7.22f, PdfSimple.largeur("A", Police.GRAS, 10), 1e-3f);
        assertTrue(PdfSimple.largeur("Parapente", Police.GRAS, 10)
                > PdfSimple.largeur("Parapente", Police.NORMAL, 10));
    }

    @Test
    @DisplayName("largeur : proportionnelle à la taille et additive (pas de crénage)")
    void largeur_proportionnelleEtAdditive() {
        String texte = "Championnat 2026";
        assertEquals(2 * PdfSimple.largeur(texte, Police.NORMAL, 10),
                PdfSimple.largeur(texte, Police.NORMAL, 20), 1e-3f);
        assertEquals(PdfSimple.largeur("A", Police.NORMAL, 10) + PdfSimple.largeur("B", Police.NORMAL, 10),
                PdfSimple.largeur("AB", Police.NORMAL, 10), 1e-3f);
    }

    @Test
    @DisplayName("largeur : une lettre accentuée a la largeur de sa lettre de base (é = e, à = a)")
    void largeur_accents() {
        for (Police police : Police.values()) {
            assertEquals(PdfSimple.largeur("e", police, 10), PdfSimple.largeur("é", police, 10), 1e-3f);
            assertEquals(PdfSimple.largeur("a", police, 10), PdfSimple.largeur("à", police, 10), 1e-3f);
        }
    }

    @Test
    @DisplayName("largeur : caractère hors Windows-1252 mesuré comme « ? » (comme il sera écrit), sans exception")
    void largeur_caractereNonEncodable() {
        assertEquals(PdfSimple.largeur("?", Police.NORMAL, 10),
                PdfSimple.largeur("日", Police.NORMAL, 10), 1e-3f);
    }

    // =====================================================================
    // decouper
    // =====================================================================

    @Test
    @DisplayName("decouper : null, vide ou blanc => aucune ligne")
    void decouper_vide() {
        assertTrue(PdfSimple.decouper(null, Police.NORMAL, 10, 100).isEmpty());
        assertTrue(PdfSimple.decouper("", Police.NORMAL, 10, 100).isEmpty());
        assertTrue(PdfSimple.decouper("   \n\t ", Police.NORMAL, 10, 100).isEmpty());
    }

    @Test
    @DisplayName("decouper : texte qui tient => une seule ligne")
    void decouper_tientSurUneLigne() {
        assertEquals(List.of("Terrain A"), PdfSimple.decouper("Terrain A", Police.NORMAL, 10, 500));
    }

    @Test
    @DisplayName("decouper : espaces multiples, tabulations et retours à la ligne normalisés en un espace")
    void decouper_normaliseLesEspaces() {
        assertEquals(List.of("a b c"), PdfSimple.decouper("  a   b\n c ", Police.NORMAL, 10, 500));
    }

    @Test
    @DisplayName("decouper : coupe aux espaces au dernier mot qui tient")
    void decouper_coupeAuxMots() {
        float max = PdfSimple.largeur("aaa bbb", Police.NORMAL, 10);

        assertEquals(List.of("aaa bbb", "ccc"), PdfSimple.decouper("aaa bbb ccc", Police.NORMAL, 10, max));
    }

    @Test
    @DisplayName("decouper : chaque ligne respecte la largeur max et aucun mot n'est perdu")
    void decouper_proprietes() {
        String texte = "Le vol en parapente est une activité merveilleuse à pratiquer";
        float max = 100;

        List<String> lignes = PdfSimple.decouper(texte, Police.NORMAL, 10, max);

        assertTrue(lignes.size() > 1);
        for (String ligne : lignes) {
            assertTrue(PdfSimple.largeur(ligne, Police.NORMAL, 10) <= max, "ligne trop large : " + ligne);
        }
        assertEquals(texte, String.join(" ", lignes));
    }

    @Test
    @DisplayName("decouper : mot plus long que la ligne => coupé en morceaux qui tiennent")
    void decouper_motTropLong() {
        float max = PdfSimple.largeur("abcde", Police.NORMAL, 10);

        List<String> lignes = PdfSimple.decouper("abcdefghij", Police.NORMAL, 10, max);

        assertEquals(List.of("abcde", "fghij"), lignes);
        assertEquals("abcdefghij", String.join("", lignes));
    }

    @Test
    @DisplayName("decouper : mot trop long après un mot normal => le mot normal reste sur sa ligne")
    void decouper_motTropLongApresUnMot() {
        float max = PdfSimple.largeur("abcde", Police.NORMAL, 10);

        List<String> lignes = PdfSimple.decouper("ab abcdefghij", Police.NORMAL, 10, max);

        assertEquals("ab", lignes.get(0));
        assertEquals("abcdefghij", String.join("", lignes.subList(1, lignes.size())));
        for (String ligne : lignes) {
            assertTrue(PdfSimple.largeur(ligne, Police.NORMAL, 10) <= max);
        }
    }

    @Test
    @DisplayName("decouper : largeur plus petite qu'un caractère => une lettre par ligne, sans boucle infinie")
    void decouper_largeurMinuscule() {
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(5), () ->
                assertEquals(List.of("a", "b", "c"), PdfSimple.decouper("abc", Police.NORMAL, 10, 1)));
    }

    // =====================================================================
    // tronquer
    // =====================================================================

    @Test
    @DisplayName("tronquer : null => null ; texte qui tient => inchangé")
    void tronquer_nullEtTexteCourt() {
        assertNull(PdfSimple.tronquer(null, Police.NORMAL, 10, 100));
        assertEquals("", PdfSimple.tronquer("", Police.NORMAL, 10, 100));
        assertEquals("Court", PdfSimple.tronquer("Court", Police.NORMAL, 10, 500));
    }

    @Test
    @DisplayName("tronquer : texte trop long => préfixe + « … » qui tient dans la largeur")
    void tronquer_texteLong() {
        String texte = "Championnat de France de Parapente sapeur-pompiers";
        float max = 100;

        String resultat = PdfSimple.tronquer(texte, Police.NORMAL, 10, max);

        assertTrue(resultat.endsWith("…"));
        assertTrue(resultat.length() < texte.length());
        assertTrue(texte.startsWith(resultat.substring(0, resultat.length() - 1)));
        assertTrue(PdfSimple.largeur(resultat, Police.NORMAL, 10) <= max);
    }

    @Test
    @DisplayName("tronquer : l'espace avant la coupure est retiré (« Bonjour… », pas « Bonjour … »)")
    void tronquer_retireEspaceFinal() {
        float max = PdfSimple.largeur("Bonjour …", Police.NORMAL, 10);

        assertEquals("Bonjour…", PdfSimple.tronquer("Bonjour monde", Police.NORMAL, 10, max));
    }

    @Test
    @DisplayName("tronquer : largeur minuscule => garde au moins le premier caractère")
    void tronquer_largeurMinuscule() {
        assertEquals("B…", PdfSimple.tronquer("Bonjour", Police.NORMAL, 10, 1));
    }

    // =====================================================================
    // Pages
    // =====================================================================

    @Test
    @DisplayName("nombrePages : compte les pages créées")
    void pages_compte() {
        PdfSimple pdf = new PdfSimple();
        assertEquals(0, pdf.nombrePages());

        pdf.nouvellePage();
        pdf.nouvellePage();
        pdf.nouvellePage();

        assertEquals(3, pdf.nombrePages());
    }

    @Test
    @DisplayName("Dessiner sans avoir créé de page => une page est créée automatiquement")
    void pages_creationAutomatique() {
        PdfSimple pdf = new PdfSimple();

        pdf.texte("Bonjour", 10, 20, Police.NORMAL, 12, NOIR);

        assertEquals(1, pdf.nombrePages());
    }

    @Test
    @DisplayName("allerPage : dessine sur une page précédente sans toucher aux autres")
    void pages_allerPage() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();
        pdf.texte("P1", 10, 20, Police.NORMAL, 12, NOIR);
        pdf.nouvellePage();
        pdf.texte("P2", 10, 20, Police.NORMAL, 12, NOIR);

        pdf.allerPage(0);
        pdf.texte("P1bis", 10, 40, Police.NORMAL, 12, NOIR);

        List<String> contenus = contenusPages(rendu(pdf));
        assertEquals(2, contenus.size());
        assertTrue(contenus.get(0).contains("(P1)"));
        assertTrue(contenus.get(0).contains("(P1bis)"));
        assertFalse(contenus.get(0).contains("(P2)"));
        assertTrue(contenus.get(1).contains("(P2)"));
        assertFalse(contenus.get(1).contains("(P1bis)"));
    }

    @Test
    @DisplayName("allerPage : index inexistant => exception")
    void pages_allerPageInvalide() {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        assertThrows(IndexOutOfBoundsException.class, () -> pdf.allerPage(5));
    }

    // =====================================================================
    // Dessin : contenu des pages
    // =====================================================================

    @Test
    @DisplayName("texte : couleur, police, taille et position (axe Y inversé) écrits dans le flux")
    void texte_commandesPdf() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.texte("Bonjour", 10, 20, Police.NORMAL, 12, ROUGE);

        String contenu = contenusPages(rendu(pdf)).get(0);
        assertTrue(contenu.contains("1 0 0 rg BT /F1 12 Tf 10 821.89 Td (Bonjour) Tj ET"), contenu);
    }

    @Test
    @DisplayName("texte : police grasse => ressource /F2")
    void texte_gras() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.texte("Titre", 10, 20, Police.GRAS, 14, NOIR);

        assertTrue(contenusPages(rendu(pdf)).get(0).contains("/F2 14 Tf"));
    }

    @Test
    @DisplayName("texte : null ou vide => rien n'est écrit")
    void texte_nullOuVide_rienEcrit() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.texte(null, 10, 20, Police.NORMAL, 12, NOIR);
        pdf.texte("", 10, 20, Police.NORMAL, 12, NOIR);

        assertEquals("", contenusPages(rendu(pdf)).get(0));
    }

    @Test
    @DisplayName("texte : parenthèses et antislash échappés (sinon le PDF serait corrompu)")
    void texte_echappement() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.texte("a(b)c\\d", 10, 20, Police.NORMAL, 12, NOIR);

        assertTrue(contenusPages(rendu(pdf)).get(0).contains("(a\\(b\\)c\\\\d) Tj"));
    }

    @Test
    @DisplayName("texte : retour à la ligne remplacé par un espace")
    void texte_retourALaLigne() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.texte("a\nb\rc", 10, 20, Police.NORMAL, 12, NOIR);

        assertTrue(contenusPages(rendu(pdf)).get(0).contains("(a b c) Tj"));
    }

    @Test
    @DisplayName("texte : accents et « € » encodés en Windows-1252 (WinAnsi)")
    void texte_accentsWinAnsi() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.texte("Été 5€", 10, 20, Police.NORMAL, 12, NOIR);

        // En ISO-8859-1 : É = 0xC9, é = 0xE9 (identiques en Windows-1252), € = 0x80 en Windows-1252
        assertTrue(contenusPages(rendu(pdf)).get(0).contains("(\u00C9t\u00E9 5\u0080) Tj"));
    }

    @Test
    @DisplayName("texteDroite : le texte se termine à la position demandée (x = xDroite - largeur)")
    void texteDroite_position() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.texteDroite("A", 100, 50, Police.NORMAL, 10, NOIR);

        // largeur de « A » à 10 pt = 6.67 -> x = 93.33 ; y = 841.89 - 50 = 791.89
        assertTrue(contenusPages(rendu(pdf)).get(0).contains("93.33 791.89 Td (A)"));
    }

    @Test
    @DisplayName("texteCentre : le texte est centré sur la position demandée (x = xCentre - largeur / 2)")
    void texteCentre_position() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.texteCentre("AA", 100, 50, Police.NORMAL, 10, NOIR);

        // largeur de « AA » à 10 pt = 13.34 -> x = 100 - 6.67 = 93.33
        assertTrue(contenusPages(rendu(pdf)).get(0).contains("93.33 791.89 Td (AA)"));
    }

    @Test
    @DisplayName("rectangle : fond seul => remplissage « f »")
    void rectangle_fondSeul() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.rectangle(10, 20, 100, 50, ROUGE, null, 0);

        assertTrue(contenusPages(rendu(pdf)).get(0)
                .contains("q 1 0 0 rg 10 771.89 100 50 re f Q"));
    }

    @Test
    @DisplayName("rectangle : bordure seule => trait « S » avec épaisseur")
    void rectangle_bordureSeule() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.rectangle(10, 20, 100, 50, null, BLEU, 2);

        assertTrue(contenusPages(rendu(pdf)).get(0)
                .contains("q 0 0 1 RG 2 w 10 771.89 100 50 re S Q"));
    }

    @Test
    @DisplayName("rectangle : fond et bordure => remplissage + trait « B »")
    void rectangle_fondEtBordure() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.rectangle(10, 20, 100, 50, ROUGE, BLEU, 1);

        String contenu = contenusPages(rendu(pdf)).get(0);
        assertTrue(contenu.contains(" re B Q"), contenu);
    }

    @Test
    @DisplayName("rectangle : ni fond ni bordure => rien n'est écrit")
    void rectangle_rien() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.rectangle(10, 20, 100, 50, null, null, 1);
        pdf.rectangleArrondi(10, 20, 100, 50, 8, null, null, 1);

        assertEquals("", contenusPages(rendu(pdf)).get(0));
    }

    @Test
    @DisplayName("rectangleArrondi : trace des courbes de Bézier et ferme le chemin")
    void rectangleArrondi_courbes() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.rectangleArrondi(10, 20, 100, 50, 8, ROUGE, null, 0);

        String contenu = contenusPages(rendu(pdf)).get(0);
        assertEquals(4, contenu.split(" c ", -1).length - 1, "4 coins arrondis attendus");
        assertTrue(contenu.contains("h f Q"));
        assertFalse(contenu.contains(" re "));
    }

    @Test
    @DisplayName("rectangleArrondi : rayon plus grand que le rectangle => limité, pas d'exception")
    void rectangleArrondi_rayonTropGrand() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        assertDoesNotThrow(() -> pdf.rectangleArrondi(10, 20, 20, 10, 999, ROUGE, null, 0));
        assertTrue(contenusPages(rendu(pdf)).get(0).contains(" c "));
    }

    @Test
    @DisplayName("ligne : segment avec couleur de trait et épaisseur")
    void ligne_commandes() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.ligne(0, 0, 100, 0, NOIR, 1);

        assertTrue(contenusPages(rendu(pdf)).get(0)
                .contains("q 0 0 0 RG 1 w 0 841.89 m 100 841.89 l S Q"));
    }

    // =====================================================================
    // Image
    // =====================================================================

    @Test
    @DisplayName("aUneImage : faux tant qu'aucune image n'est définie")
    void image_aUneImage() {
        PdfSimple pdf = new PdfSimple();
        assertFalse(pdf.aUneImage());

        pdf.definirImage(new byte[]{1, 2, 3}, 240, 240);

        assertTrue(pdf.aUneImage());
    }

    @Test
    @DisplayName("image : sans image définie, rien n'est dessiné")
    void image_sansImage_rienDessine() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        pdf.image(10, 20, 50, 40);

        assertEquals("", contenusPages(rendu(pdf)).get(0));
    }

    @Test
    @DisplayName("image : avec image définie, commande de dessin /Im1 positionnée (axe Y inversé)")
    void image_dessin() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.definirImage(new byte[]{1, 2, 3}, 240, 240);
        pdf.nouvellePage();

        pdf.image(10, 20, 50, 40);

        assertTrue(contenusPages(rendu(pdf)).get(0).contains("q 50 0 0 40 10 781.89 cm /Im1 Do Q"));
    }

    // =====================================================================
    // Structure du fichier PDF
    // =====================================================================

    @Test
    @DisplayName("PDF sans aucune page : une page vide est produite, fichier valide")
    void ecrire_sansPage_produitUnePage() throws IOException {
        PdfSimple pdf = new PdfSimple();

        byte[] octets = octets(pdf);
        String texte = new String(octets, StandardCharsets.ISO_8859_1);

        assertTrue(texte.startsWith("%PDF-1.4\n"));
        assertTrue(texte.endsWith("%%EOF\n"));
        assertTrue(texte.contains("/Count 1"));
        assertXrefCoherente(octets);
    }

    @Test
    @DisplayName("En-tête : commentaire binaire (octets > 127) pour que les outils traitent le fichier comme binaire")
    void ecrire_enteteBinaire() throws IOException {
        byte[] octets = octets(new PdfSimple());

        assertEquals('%', octets[9]);
        assertEquals((byte) 0xE2, octets[10]);
        assertEquals((byte) 0xE3, octets[11]);
        assertEquals((byte) 0xCF, octets[12]);
        assertEquals((byte) 0xD3, octets[13]);
    }

    @Test
    @DisplayName("Structure : catalogue, arbre des pages, polices Helvetica et Helvetica-Bold, format A4")
    void ecrire_structureDeBase() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        String texte = rendu(pdf);

        assertTrue(texte.contains("/Type /Catalog /Pages 2 0 R"));
        assertTrue(texte.contains("/BaseFont /Helvetica /Encoding /WinAnsiEncoding"));
        assertTrue(texte.contains("/BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding"));
        assertTrue(texte.contains("/MediaBox [0 0 595.28 841.89]"));
        assertTrue(texte.contains("/Font << /F1 3 0 R /F2 4 0 R >>"));
        assertFalse(texte.contains("/Subtype /Image"));
        assertFalse(texte.contains("/XObject"));
    }

    @Test
    @DisplayName("Pages : /Count et /Kids (objets 6, 8, 10 sans image)")
    void ecrire_pagesSansImage() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();
        pdf.nouvellePage();
        pdf.nouvellePage();

        String texte = rendu(pdf);

        assertTrue(texte.contains("/Count 3"));
        assertTrue(texte.contains("/Kids [ 6 0 R 8 0 R 10 0 R ]"));
    }

    @Test
    @DisplayName("Pages : avec image, la numérotation des pages est décalée (objets 7, 9)")
    void ecrire_pagesAvecImage() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.definirImage(new byte[]{1, 2, 3}, 240, 200);
        pdf.nouvellePage();
        pdf.nouvellePage();

        String texte = rendu(pdf);

        assertTrue(texte.contains("/Kids [ 7 0 R 9 0 R ]"));
        assertTrue(texte.contains("/Subtype /Image"));
        assertTrue(texte.contains("/Width 240 /Height 200"));
        assertTrue(texte.contains("/Filter /DCTDecode /Length 3 >>\nstream\n"));
        assertTrue(texte.contains("/XObject << /Im1 6 0 R >>"));
    }

    @Test
    @DisplayName("Table xref cohérente : sans image, une page")
    void ecrire_xref_unePage() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();
        pdf.texte("Bonjour", 10, 20, Police.NORMAL, 12, NOIR);

        assertXrefCoherente(octets(pdf));
    }

    @Test
    @DisplayName("Table xref cohérente : sans image, plusieurs pages")
    void ecrire_xref_plusieursPages() throws IOException {
        PdfSimple pdf = new PdfSimple();
        for (int i = 0; i < 4; i++) {
            pdf.nouvellePage();
            pdf.texte("Page " + (i + 1), 10, 20, Police.GRAS, 12, NOIR);
        }

        assertXrefCoherente(octets(pdf));
    }

    @Test
    @DisplayName("Table xref cohérente : avec image binaire et plusieurs pages")
    void ecrire_xref_avecImage() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.definirImage(new byte[]{(byte) 0xFF, (byte) 0xD8, 0, 10, 13, (byte) 0xFF, (byte) 0xD9}, 10, 10);
        pdf.nouvellePage();
        pdf.image(10, 10, 20, 20);
        pdf.nouvellePage();

        assertXrefCoherente(octets(pdf));
    }

    @Test
    @DisplayName("Chaque flux déclare une /Length égale à sa taille réelle")
    void ecrire_lengthDesFluxCorrecte() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();
        pdf.texte("Bonjour le monde", 10, 20, Police.NORMAL, 12, ROUGE);
        pdf.rectangle(10, 30, 100, 20, BLEU, null, 0);

        String texte = rendu(pdf);
        Matcher m = Pattern.compile("<< /Length (\\d+) >>\\nstream\\n").matcher(texte);
        assertTrue(m.find());
        int longueur = Integer.parseInt(m.group(1));

        assertTrue(texte.startsWith("\nendstream\nendobj\n", m.end() + longueur));
    }

    // =====================================================================
    // Métadonnées
    // =====================================================================

    @Test
    @DisplayName("Titre du document encodé en UTF-16 hexadécimal (BOM FEFF)")
    void metadonnees_titre() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.setTitreDocument("Planning");
        pdf.nouvellePage();

        assertTrue(rendu(pdf).contains("/Title <FEFF0050006C0061006E006E0069006E0067>"));
    }

    @Test
    @DisplayName("Titre avec accents : « Été » => 00C9 0074 00E9")
    void metadonnees_titreAccentue() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.setTitreDocument("Été");
        pdf.nouvellePage();

        assertTrue(rendu(pdf).contains("/Title <FEFF00C9007400E9>"));
    }

    @Test
    @DisplayName("Titre null => titre vide, sans exception")
    void metadonnees_titreNull() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.setTitreDocument(null);
        pdf.nouvellePage();

        assertTrue(rendu(pdf).contains("/Title <FEFF>"));
    }

    @Test
    @DisplayName("Producteur et date de création présents (D:aaaaMMjjHHmmss)")
    void metadonnees_producteurEtDate() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();

        String texte = rendu(pdf);

        assertTrue(texte.contains("/Producer <FEFF"));
        assertTrue(Pattern.compile("/CreationDate \\(D:\\d{14}\\)").matcher(texte).find());
    }

    // =====================================================================
    // Réutilisation
    // =====================================================================

    @Test
    @DisplayName("Écrire deux fois le même document donne le même contenu de pages")
    void ecrire_deuxFois_memeContenu() throws IOException {
        PdfSimple pdf = new PdfSimple();
        pdf.nouvellePage();
        pdf.texte("Bonjour", 10, 20, Police.NORMAL, 12, NOIR);

        byte[] premier = octets(pdf);
        byte[] second = octets(pdf);

        assertEquals(premier.length, second.length);
        assertEquals(contenusPages(new String(premier, StandardCharsets.ISO_8859_1)),
                contenusPages(new String(second, StandardCharsets.ISO_8859_1)));
    }
}