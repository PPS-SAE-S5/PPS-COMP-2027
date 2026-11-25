package com.pps.parapente.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Petit générateur de PDF
 */
public final class PdfSimple {

    /** Format A4 en points. */
    public static final float LARGEUR_PAGE = 595.28f;
    public static final float HAUTEUR_PAGE = 841.89f;

    /** Polices standard du PDF */
    public enum Police {
        NORMAL("F1", "Helvetica"),
        GRAS("F2", "Helvetica-Bold");

        private final String ressource;
        private final String nomPdf;

        Police(String ressource, String nomPdf) {
            this.ressource = ressource;
            this.nomPdf = nomPdf;
        }
    }

    /** Couleur RVB */
    public record Couleur(int r, int v, int b) {
        public static Couleur hex(String hex) {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            return new Couleur(Integer.parseInt(h.substring(0, 2), 16),
                    Integer.parseInt(h.substring(2, 4), 16),
                    Integer.parseInt(h.substring(4, 6), 16));
        }
    }

    /** Encodage WinAnsi des polices standard */
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");

    /** Largeurs des caractères 32 à 255 */
    private static final int[] LARGEURS_NORMAL = {
            278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
            1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
            333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
            556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584, 350,
            556, 350, 222, 556, 333, 1000, 556, 556, 333, 1000, 667, 333, 1000, 350, 611, 350,
            350, 222, 222, 333, 333, 350, 556, 1000, 333, 1000, 500, 333, 944, 350, 500, 667,
            278, 333, 556, 556, 556, 556, 260, 556, 333, 737, 370, 556, 584, 333, 737, 333,
            400, 584, 333, 333, 333, 556, 537, 278, 333, 333, 365, 556, 834, 834, 834, 611,
            667, 667, 667, 667, 667, 667, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278,
            722, 722, 778, 778, 778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611,
            556, 556, 556, 556, 556, 556, 889, 500, 556, 556, 556, 556, 278, 278, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 584, 611, 556, 556, 556, 556, 500, 556, 500
    };
    private static final int[] LARGEURS_GRAS = {
            278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
            975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
            333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
            611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584, 350,
            556, 350, 278, 556, 500, 1000, 556, 556, 333, 1000, 667, 333, 1000, 350, 611, 350,
            350, 278, 278, 500, 500, 350, 556, 1000, 333, 1000, 556, 333, 944, 350, 500, 667,
            278, 333, 556, 556, 556, 556, 280, 556, 333, 737, 370, 556, 584, 333, 737, 333,
            400, 584, 333, 333, 333, 611, 556, 278, 333, 333, 365, 556, 834, 834, 834, 611,
            722, 722, 722, 722, 722, 722, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278,
            722, 722, 778, 778, 778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611,
            556, 556, 556, 556, 556, 556, 889, 556, 556, 556, 556, 556, 278, 278, 278, 278,
            611, 611, 611, 611, 611, 611, 611, 584, 611, 611, 611, 611, 611, 556, 611, 556
    };

    private final List<ByteArrayOutputStream> pages = new ArrayList<>();
    private ByteArrayOutputStream pageCourante;
    private String titreDocument = "";

    private byte[] imageJpeg;
    private int imageLargeurPx;
    private int imageHauteurPx;

    public void setTitreDocument(String titre) {
        this.titreDocument = titre == null ? "" : titre;
    }

    // pages

    public void nouvellePage() {
        pageCourante = new ByteArrayOutputStream();
        pages.add(pageCourante);
    }

    public int nombrePages() {
        return pages.size();
    }

    /** Revient sur une page déjà créée */
    public void allerPage(int index) {
        pageCourante = pages.get(index);
    }

    // dessin

    /** Écrit une ligne de texte */
    public void texte(String texte, float x, float yBase, Police police, float taille, Couleur couleur) {
        if (texte == null || texte.isEmpty()) return;
        ByteArrayOutputStream p = page();
        ecrire(p, couleurRemplissage(couleur));
        ecrire(p, "BT /" + police.ressource + " " + n(taille) + " Tf " + n(x) + " " + n(HAUTEUR_PAGE - yBase) + " Td ");
        ecrireChaine(p, texte);
        ecrire(p, " Tj ET\n");
    }

    /** Texte aligné à droite */
    public void texteDroite(String texte, float xDroite, float yBase, Police police, float taille, Couleur couleur) {
        texte(texte, xDroite - largeur(texte, police, taille), yBase, police, taille, couleur);
    }

    /** Texte centré */
    public void texteCentre(String texte, float xCentre, float yBase, Police police, float taille, Couleur couleur) {
        texte(texte, xCentre - largeur(texte, police, taille) / 2, yBase, police, taille, couleur);
    }

    /** Rectangle */
    public void rectangle(float x, float yHaut, float l, float h, Couleur fond, Couleur bordure, float epaisseur) {
        rectangleArrondi(x, yHaut, l, h, 0, fond, bordure, epaisseur);
    }

    /** Rectangle aux coins arrondis */
    public void rectangleArrondi(float x, float yHaut, float l, float h, float rayon,
                                 Couleur fond, Couleur bordure, float epaisseur) {
        if (fond == null && bordure == null) return;
        ByteArrayOutputStream p = page();
        float x0 = x, x1 = x + l;
        float yB = HAUTEUR_PAGE - (yHaut + h), yH = HAUTEUR_PAGE - yHaut;
        float r = Math.max(0, Math.min(rayon, Math.min(l, h) / 2));
        StringBuilder sb = new StringBuilder("q ");
        if (fond != null) sb.append(couleurRemplissage(fond));
        if (bordure != null) sb.append(couleurTrait(bordure)).append(n(epaisseur)).append(" w ");
        if (r == 0) {
            sb.append(n(x0)).append(' ').append(n(yB)).append(' ').append(n(l)).append(' ').append(n(h)).append(" re ");
        } else {
            float k = 0.5523f * r;   // approximation d'un quart de cercle
            sb.append(n(x0 + r)).append(' ').append(n(yB)).append(" m ")
              .append(n(x1 - r)).append(' ').append(n(yB)).append(" l ")
              .append(courbe(x1 - r + k, yB, x1, yB + r - k, x1, yB + r))
              .append(n(x1)).append(' ').append(n(yH - r)).append(" l ")
              .append(courbe(x1, yH - r + k, x1 - r + k, yH, x1 - r, yH))
              .append(n(x0 + r)).append(' ').append(n(yH)).append(" l ")
              .append(courbe(x0 + r - k, yH, x0, yH - r + k, x0, yH - r))
              .append(n(x0)).append(' ').append(n(yB + r)).append(" l ")
              .append(courbe(x0, yB + r - k, x0 + r - k, yB, x0 + r, yB))
              .append("h ");
        }
        sb.append(fond != null && bordure != null ? "B" : fond != null ? "f" : "S").append(" Q\n");
        ecrire(p, sb.toString());
    }

    public void ligne(float x1, float y1, float x2, float y2, Couleur couleur, float epaisseur) {
        ecrire(page(), "q " + couleurTrait(couleur) + n(epaisseur) + " w " + n(x1) + " " + n(HAUTEUR_PAGE - y1)
                + " m " + n(x2) + " " + n(HAUTEUR_PAGE - y2) + " l S Q\n");
    }

    /**
     * Déclare l'image JPEG utilisable sur toutes les pages
     */
    public void definirImage(byte[] jpeg, int largeurPx, int hauteurPx) {
        this.imageJpeg = jpeg;
        this.imageLargeurPx = largeurPx;
        this.imageHauteurPx = hauteurPx;
    }

    public boolean aUneImage() {
        return imageJpeg != null;
    }

    public void image(float x, float yHaut, float l, float h) {
        if (imageJpeg == null) return;
        ecrire(page(), "q " + n(l) + " 0 0 " + n(h) + " " + n(x) + " " + n(HAUTEUR_PAGE - yHaut - h) + " cm /Im1 Do Q\n");
    }

    // mesure du texte

    /** Largeur du texte en points */
    public static float largeur(String texte, Police police, float taille) {
        if (texte == null || texte.isEmpty()) return 0;
        int[] largeurs = police == Police.GRAS ? LARGEURS_GRAS : LARGEURS_NORMAL;
        int total = 0;
        for (byte b : texte.getBytes(WIN_ANSI)) {
            int c = b & 0xFF;
            int w = c >= 32 ? largeurs[c - 32] : 0;
            total += w > 0 ? w : 556;
        }
        return total * taille / 1000f;
    }

    /** Découpe un texte en lignes ne dépassant pas la largeur maximale */
    public static List<String> decouper(String texte, Police police, float taille, float largeurMax) {
        List<String> lignes = new ArrayList<>();
        if (texte == null || texte.isBlank()) return lignes;
        StringBuilder ligne = new StringBuilder();
        for (String mot : texte.trim().split("\\s+")) {
            String essai = ligne.length() == 0 ? mot : ligne + " " + mot;
            if (largeur(essai, police, taille) <= largeurMax) {
                ligne.setLength(0);
                ligne.append(essai);
                continue;
            }
            if (ligne.length() > 0) {
                lignes.add(ligne.toString());
                ligne.setLength(0);
            }
            // mot plus long que la ligne entière : on le coupe
            String reste = mot;
            while (largeur(reste, police, taille) > largeurMax && reste.length() > 1) {
                int i = reste.length() - 1;
                while (i > 1 && largeur(reste.substring(0, i), police, taille) > largeurMax) i--;
                lignes.add(reste.substring(0, i));
                reste = reste.substring(i);
            }
            ligne.append(reste);
        }
        if (ligne.length() > 0) lignes.add(ligne.toString());
        return lignes;
    }

    /** Raccourcit le texte avec */
    public static String tronquer(String texte, Police police, float taille, float largeurMax) {
        if (texte == null || largeur(texte, police, taille) <= largeurMax) return texte;
        String t = texte;
        while (t.length() > 1 && largeur(t + "…", police, taille) > largeurMax) t = t.substring(0, t.length() - 1);
        return t.stripTrailing() + "…";
    }

    // écriture du fichier

    public void ecrire(OutputStream sortie) throws IOException {
        if (pages.isEmpty()) nouvellePage();

        ByteArrayOutputStream f = new ByteArrayOutputStream();
        List<Integer> positions = new ArrayList<>();

        // Numérotation des objets : 1 catalogue, 2 arbre des pages, 3-4 polices, 5 infos, 6 image, puis pour chaque page : l'objet page et son contenu
        int premierObjetPage = aUneImage() ? 7 : 6;
        int nbObjets = premierObjetPage - 1 + pages.size() * 2;

        ecrire(f, "%PDF-1.4\n");
        f.write(new byte[]{'%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n'});

        objet(f, positions, 1, "<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) kids.append(premierObjetPage + i * 2).append(" 0 R ");
        objet(f, positions, 2, "<< /Type /Pages /Kids [ " + kids + "] /Count " + pages.size() + " >>");

        objet(f, positions, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /" + Police.NORMAL.nomPdf + " /Encoding /WinAnsiEncoding >>");
        objet(f, positions, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /" + Police.GRAS.nomPdf + " /Encoding /WinAnsiEncoding >>");

        String date = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        objet(f, positions, 5, "<< /Title " + texteUnicode(titreDocument)
                + " /Producer " + texteUnicode("PPS - Championnat de France de Parapente sapeur-pompiers")
                + " /CreationDate (D:" + date + ") >>");

        if (aUneImage()) {
            positions.add(f.size());
            ecrire(f, "6 0 obj\n<< /Type /XObject /Subtype /Image /Width " + imageLargeurPx + " /Height " + imageHauteurPx
                    + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length " + imageJpeg.length
                    + " >>\nstream\n");
            f.write(imageJpeg);
            ecrire(f, "\nendstream\nendobj\n");
        }

        String ressources = "<< /Font << /F1 3 0 R /F2 4 0 R >>" + (aUneImage() ? " /XObject << /Im1 6 0 R >>" : "") + " >>";
        for (int i = 0; i < pages.size(); i++) {
            int numPage = premierObjetPage + i * 2;
            objet(f, positions, numPage, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + n(LARGEUR_PAGE) + " "
                    + n(HAUTEUR_PAGE) + "] /Resources " + ressources + " /Contents " + (numPage + 1) + " 0 R >>");
            byte[] contenu = pages.get(i).toByteArray();
            positions.add(f.size());
            ecrire(f, (numPage + 1) + " 0 obj\n<< /Length " + contenu.length + " >>\nstream\n");
            f.write(contenu);
            ecrire(f, "\nendstream\nendobj\n");
        }

        int debutXref = f.size();
        StringBuilder xref = new StringBuilder("xref\n0 " + (nbObjets + 1) + "\n0000000000 65535 f \n");
        for (int pos : positions) xref.append(String.format(Locale.ROOT, "%010d 00000 n \n", pos));
        xref.append("trailer\n<< /Size ").append(nbObjets + 1).append(" /Root 1 0 R /Info 5 0 R >>\nstartxref\n")
            .append(debutXref).append("\n%%EOF\n");
        ecrire(f, xref.toString());

        f.writeTo(sortie);
    }

    // outils internes

    private ByteArrayOutputStream page() {
        if (pageCourante == null) nouvellePage();
        return pageCourante;
    }

    private static void objet(ByteArrayOutputStream f, List<Integer> positions, int numero, String contenu) {
        positions.add(f.size());
        ecrire(f, numero + " 0 obj\n" + contenu + "\nendobj\n");
    }

    private static void ecrire(ByteArrayOutputStream f, String s) {
        f.writeBytes(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    /** Chaîne PDF littérale en WinAnsi */
    private static void ecrireChaine(ByteArrayOutputStream p, String texte) {
        p.write('(');
        for (byte b : texte.getBytes(WIN_ANSI)) {
            if (b == '(' || b == ')' || b == '\\') p.write('\\');
            if (b == '\n' || b == '\r') b = ' ';
            p.write(b);
        }
        p.write(')');
    }

    /** Texte des métadonnées */
    private static String texteUnicode(String s) {
        StringBuilder sb = new StringBuilder("<FEFF");
        for (char c : s.toCharArray()) sb.append(String.format("%04X", (int) c));
        return sb.append('>').toString();
    }

    private static String couleurRemplissage(Couleur c) {
        return n(c.r() / 255f) + " " + n(c.v() / 255f) + " " + n(c.b() / 255f) + " rg ";
    }

    private static String couleurTrait(Couleur c) {
        return n(c.r() / 255f) + " " + n(c.v() / 255f) + " " + n(c.b() / 255f) + " RG ";
    }

    private static String courbe(float x1, float y1, float x2, float y2, float x3, float y3) {
        return n(x1) + " " + n(y1) + " " + n(x2) + " " + n(y2) + " " + n(x3) + " " + n(y3) + " c ";
    }

    /** Nombre au format PDF */
    private static String n(float v) {
        String s = String.format(Locale.ROOT, "%.2f", v);
        if (s.contains(".")) s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s.equals("-0") ? "0" : s;
    }
}
