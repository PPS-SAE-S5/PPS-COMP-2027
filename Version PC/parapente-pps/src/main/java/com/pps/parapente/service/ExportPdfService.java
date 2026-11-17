package com.pps.parapente.service;

import com.pps.parapente.model.Atelier;
import com.pps.parapente.model.Creneau;
import com.pps.parapente.model.Epreuve;
import com.pps.parapente.model.Journee;
import com.pps.parapente.util.PdfSimple;
import com.pps.parapente.util.PdfSimple.Couleur;
import com.pps.parapente.util.PdfSimple.Police;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Export du planning d'une journée en PDF
 */
public class ExportPdfService {

    // Charte (reprend les couleurs de style.css)
    private static final Couleur BLEU_NUIT = Couleur.hex("#1b3a5c");
    private static final Couleur TEXTE = Couleur.hex("#243447");
    private static final Couleur GRIS = Couleur.hex("#566779");
    private static final Couleur GRIS_CLAIR = Couleur.hex("#8a99a8");
    private static final Couleur BORDURE = Couleur.hex("#d5dde6");
    private static final Couleur FOND_LIGNE = Couleur.hex("#f4f6f8");
    private static final Couleur BLANC = new Couleur(255, 255, 255);
    private static final Couleur BLEU_PALE = Couleur.hex("#b9c5d2");

    private static final Couleur FOND_MATIN = Couleur.hex("#e4eefb");
    private static final Couleur TEXTE_MATIN = Couleur.hex("#1c4a85");
    private static final Couleur FOND_APRES_MIDI = Couleur.hex("#fcecd6");
    private static final Couleur TEXTE_APRES_MIDI = Couleur.hex("#7a4608");
    private static final Couleur ACCENT_APRES_MIDI = Couleur.hex("#d9480f");

    // Mise en page (points)
    private static final float MARGE = 40;
    private static final float LARGEUR_UTILE = PdfSimple.LARGEUR_PAGE - 2 * MARGE;
    private static final float BAS_ZONE = PdfSimple.HAUTEUR_PAGE - 56;   // en dessous : pied de page
    private static final float PAD = 12;          // marge intérieure des cartes
    private static final float HAUT_EPREUVE = 17; // hauteur d'une ligne d'épreuve
    private static final float INTERLIGNE_INFO = 13;

    private static final String CHAMPIONNAT = "Championnat de France de Parapente sapeur-pompiers";
    private static final DateTimeFormatter DATE_LONGUE = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter DATE_EXPORT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private static byte[] logoJpeg;      // mis en cache après le premier export
    private static int logoTaillePx;

    /** Génère le PDF de la journée et l'enregistre */
    public void exporterJournee(Journee journee, List<Atelier> ateliers, Path fichier) throws IOException {
        byte[] pdf = genererJournee(journee, ateliers, LocalDateTime.now());
        Files.write(fichier, pdf);
    }

    /** Nom de fichier proposé */
    public static String nomFichierParDefaut(Journee journee) {
        String nom = "Planning - " + (journee.getNom() == null ? "journée" : journee.getNom().trim());
        if (journee.getDateJournee() != null) nom += " - " + journee.getDateJournee().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return nom.replaceAll("[\\\\/:*?\"<>|]", "-") + ".pdf";
    }


    /** Construit le PDF en mémoire */
    byte[] genererJournee(Journee journee, List<Atelier> ateliers, LocalDateTime dateExport) throws IOException {
        Mise m = new Mise(journee);
        m.pdf.setTitreDocument("Planning - " + journee.getNom());
        chargerLogo(m.pdf);

        m.pdf.nouvellePage();
        m.y = enTete(m.pdf, journee, ateliers);

        for (Creneau creneau : Creneau.values()) {
            List<Atelier> duCreneau = ateliers.stream().filter(a -> a.getCreneau() == creneau).toList();
            dessinerCreneau(m, creneau, duCreneau);
        }

        piedsDePage(m.pdf, dateExport);
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        m.pdf.ecrire(sortie);
        return sortie.toByteArray();
    }

    /** État de la mise en page : document + position verticale */
    private static final class Mise {
        final PdfSimple pdf = new PdfSimple();
        final Journee journee;
        float y;

        Mise(Journee journee) { this.journee = journee; }

        float restant() { return BAS_ZONE - y; }

        void pageSuivante() {
            pdf.nouvellePage();
            y = enTeteSuite(pdf, journee);
        }
    }

    // en-têtes

    /** Bandeau de la première page */
    private static float enTete(PdfSimple pdf, Journee journee, List<Atelier> ateliers) {
        float haut = MARGE, h = 92;
        pdf.rectangleArrondi(MARGE, haut, LARGEUR_UTILE, h, 8, BLEU_NUIT, null, 0);

        float xTexte = MARGE + 20;
        if (pdf.aUneImage()) {
            float t = 68;
            pdf.rectangleArrondi(MARGE + 12, haut + (h - t) / 2, t, t, 8, BLANC, null, 0);
            pdf.image(MARGE + 14, haut + (h - t) / 2 + 2, t - 4, t - 4);
            xTexte = MARGE + 12 + t + 16;
        }
        float largeurTexte = MARGE + LARGEUR_UTILE - 16 - xTexte;
        pdf.texte(CHAMPIONNAT.toUpperCase(Locale.FRENCH), xTexte, haut + 25, Police.GRAS, 7.5f, BLEU_PALE);
        pdf.texte(PdfSimple.tronquer("Planning · " + journee.getNom(), Police.GRAS, 21, largeurTexte),
                xTexte, haut + 52, Police.GRAS, 21, BLANC);
        String date = journee.getDateJournee() == null ? "Date non renseignée" : majuscule(journee.getDateJournee().format(DATE_LONGUE));
        pdf.texte(date, xTexte, haut + 73, Police.NORMAL, 12, BLANC);

        // Résumé sous le bandeau
        int nbEpreuves = ateliers.stream().mapToInt(a -> a.getEpreuves().size()).sum();
        String resume = pluriel(ateliers.size(), "atelier", "ateliers") + "  ·  " + pluriel(nbEpreuves, "épreuve", "épreuves");
        float y = haut + h + 22;
        pdf.texte(resume, MARGE, y, Police.NORMAL, 10, GRIS);
        return y + 18;
    }

    /** Rappel en haut des pages suivantes */
    private static float enTeteSuite(PdfSimple pdf, Journee journee) {
        String titre = "Planning · " + journee.getNom()
                + (journee.getDateJournee() == null ? "" : "  —  " + majuscule(journee.getDateJournee().format(DATE_LONGUE)));
        pdf.texte(PdfSimple.tronquer(titre, Police.GRAS, 11, LARGEUR_UTILE), MARGE, MARGE + 4, Police.GRAS, 11, BLEU_NUIT);
        pdf.texteDroite("(suite)", MARGE + LARGEUR_UTILE, MARGE + 4, Police.NORMAL, 9, GRIS);
        pdf.ligne(MARGE, MARGE + 12, MARGE + LARGEUR_UTILE, MARGE + 12, BORDURE, 0.8f);
        return MARGE + 30;
    }

    private static void piedsDePage(PdfSimple pdf, LocalDateTime dateExport) {
        int total = pdf.nombrePages();
        for (int i = 0; i < total; i++) {
            pdf.allerPage(i);
            float y = PdfSimple.HAUTEUR_PAGE - 30;
            pdf.ligne(MARGE, y - 12, MARGE + LARGEUR_UTILE, y - 12, BORDURE, 0.6f);
            pdf.texte(CHAMPIONNAT + "  ·  exporté le " + dateExport.format(DATE_EXPORT), MARGE, y, Police.NORMAL, 8, GRIS_CLAIR);
            pdf.texteDroite("Page " + (i + 1) + " / " + total, MARGE + LARGEUR_UTILE, y, Police.NORMAL, 8, GRIS_CLAIR);
        }
    }

    // créneaux

    private static final float HAUT_TITRE_CRENEAU = 34;

    private void dessinerCreneau(Mise m, Creneau creneau, List<Atelier> ateliers) {
        float premiereHauteur = ateliers.isEmpty() ? 40 : hauteurCarte(ateliers.get(0), ateliers.get(0).getEpreuves().size());
        // le titre du créneau ne reste jamais seul en bas de page
        if (m.restant() < HAUT_TITRE_CRENEAU + Math.min(premiereHauteur, 160)) m.pageSuivante();
        titreCreneau(m, creneau, ateliers.size(), false);

        if (ateliers.isEmpty()) {
            m.pdf.rectangleArrondi(MARGE, m.y, LARGEUR_UTILE, 32, 6, null, BORDURE, 0.8f);
            m.pdf.texte("Aucun atelier sur ce créneau.", MARGE + PAD, m.y + 20, Police.NORMAL, 10, GRIS);
            m.y += 32 + 22;
            return;
        }
        for (Atelier a : ateliers) {
            dessinerAtelier(m, a, creneau);
        }
        m.y += 10;
    }

    private void titreCreneau(Mise m, Creneau creneau, int nbAteliers, boolean suite) {
        boolean matin = creneau == Creneau.MATIN;
        String libelle = creneau.getLibelle().toUpperCase(Locale.FRENCH) + (suite ? " (suite)" : "");
        float l = PdfSimple.largeur(libelle, Police.GRAS, 10) + 24;
        m.pdf.rectangleArrondi(MARGE, m.y, l, 20, 10, matin ? FOND_MATIN : FOND_APRES_MIDI, null, 0);
        m.pdf.texte(libelle, MARGE + 12, m.y + 14, Police.GRAS, 10, matin ? TEXTE_MATIN : TEXTE_APRES_MIDI);
        if (!suite) {
            m.pdf.texte(pluriel(nbAteliers, "atelier", "ateliers"), MARGE + l + 10, m.y + 14, Police.NORMAL, 9.5f, GRIS);
        }
        m.y += HAUT_TITRE_CRENEAU;
    }

    // ateliers

    private static final float LARGEUR_COL_HORAIRES = 110;

    /** Lignes de texte d'une carte, calculées une fois pour la mesure et le dessin. */
    private record Infos(List<String> nom, List<String> lieu, List<String> responsable) {}

    private static Infos infos(Atelier a) {
        float largeurCol = (LARGEUR_UTILE - 2 * PAD - 8 - LARGEUR_COL_HORAIRES) / 2 - 10;
        return new Infos(
                PdfSimple.decouper(a.getNom(), Police.GRAS, 13.5f, LARGEUR_UTILE - 2 * PAD - 8),
                lignesOuTiret(a.getLieu(), largeurCol),
                lignesOuTiret(a.getResponsable(), largeurCol));
    }

    private static List<String> lignesOuTiret(String valeur, float largeur) {
        List<String> l = PdfSimple.decouper(valeur, Police.NORMAL, 10.5f, largeur);
        return l.isEmpty() ? List.of("—") : l;
    }

    /** Hauteur de la carte si elle contient les {@code nbEpreuves} premières épreuves */
    private float hauteurCarte(Atelier a, int nbEpreuves) {
        return hauteurSansEpreuves(infos(a)) + Math.max(1, nbEpreuves) * HAUT_EPREUVE + PAD;
    }

    private static float hauteurSansEpreuves(Infos inf) {
        int lignesInfo = Math.max(1, Math.max(inf.lieu().size(), inf.responsable().size()));
        return PAD + inf.nom().size() * 17 + 6          // nom
                + 11 + lignesInfo * INTERLIGNE_INFO + 10    // libellés + valeurs
                + 1 + 10 + 14;                              // séparateur + « ÉPREUVES »
    }

    private void dessinerAtelier(Mise m, Atelier a, Creneau creneau) {
        Infos inf = infos(a);
        List<Epreuve> epreuves = a.getEpreuves();
        int debut = 0;
        boolean suite = false;

        float hautDeNouvellePage = MARGE + 30 + HAUT_TITRE_CRENEAU;
        while (true) {
            float fixe = hauteurSansEpreuves(inf);
            int restantes = epreuves.size() - debut;
            float complete = fixe + Math.max(1, restantes) * HAUT_EPREUVE + PAD;

            int nb;
            if (complete <= m.restant()) {
                nb = restantes;
            } else {
                boolean hautDePage = m.y <= hautDeNouvellePage + 1;
                if (!hautDePage && complete <= BAS_ZONE - hautDeNouvellePage) {
                    // tiendra entière sur la page suivante : on ne la coupe pas
                    m.pageSuivante();
                    titreCreneau(m, creneau, 0, true);
                    continue;
                }
                // carte plus grande qu'une page : ses épreuves continuent sur la page suivante
                nb = (int) Math.floor((m.restant() - fixe - PAD) / HAUT_EPREUVE);
                if (nb < Math.min(5, restantes)) {   // pas une carte avec 1 ou 2 épreuves seulement en bas de page
                    if (hautDePage) {
                        nb = Math.max(nb, Math.min(1, restantes));
                    } else {
                        m.pageSuivante();
                        titreCreneau(m, creneau, 0, true);
                        continue;
                    }
                }
            }

            float h = fixe + Math.max(1, nb) * HAUT_EPREUVE + PAD;
            dessinerCarte(m, a, inf, creneau, epreuves.subList(debut, debut + nb), debut, suite, h);
            m.y += h + 12;
            debut += nb;
            if (debut >= epreuves.size()) break;
            m.pageSuivante();
            titreCreneau(m, creneau, 0, true);
            suite = true;
        }
    }

    private void dessinerCarte(Mise m, Atelier a, Infos inf, Creneau creneau, List<Epreuve> epreuves,
                               int numeroDepart, boolean suite, float h) {
        PdfSimple pdf = m.pdf;
        float x = MARGE, y = m.y;
        Couleur accent = creneau == Creneau.MATIN ? TEXTE_MATIN : ACCENT_APRES_MIDI;

        pdf.rectangleArrondi(x, y, LARGEUR_UTILE, h, 7, BLANC, BORDURE, 0.8f);
        pdf.rectangleArrondi(x, y, 5, h, 2.5f, accent, null, 0);   // liseré de couleur du créneau

        float xi = x + PAD + 8;
        float yi = y + PAD;

        // Nom de l'atelier
        for (int i = 0; i < inf.nom().size(); i++) {
            String ligne = inf.nom().get(i) + (suite && i == inf.nom().size() - 1 ? " (suite)" : "");
            pdf.texte(ligne, xi, yi + 13 + i * 17, Police.GRAS, 13.5f, BLEU_NUIT);
        }
        yi += inf.nom().size() * 17 + 6;

        // Horaires | Lieu | Responsable
        float largeurCol = (LARGEUR_UTILE - 2 * PAD - 8 - LARGEUR_COL_HORAIRES) / 2;
        float[] xs = {xi, xi + LARGEUR_COL_HORAIRES, xi + LARGEUR_COL_HORAIRES + largeurCol};
        String[] libelles = {"HORAIRES", "LIEU", "RESPONSABLE"};
        String horaires = a.getPlageHoraire().isBlank() ? "—" : a.getPlageHoraire();
        List<List<String>> valeurs = List.of(List.of(horaires), inf.lieu(), inf.responsable());
        for (int c = 0; c < 3; c++) {
            pdf.texte(libelles[c], xs[c], yi + 7, Police.GRAS, 7, GRIS_CLAIR);
            List<String> v = valeurs.get(c);
            for (int i = 0; i < v.size(); i++) {
                pdf.texte(v.get(i), xs[c], yi + 11 + 10 + i * INTERLIGNE_INFO,
                        c == 0 ? Police.GRAS : Police.NORMAL, 10.5f, TEXTE);
            }
        }
        int lignesInfo = Math.max(1, Math.max(inf.lieu().size(), inf.responsable().size()));
        yi += 11 + lignesInfo * INTERLIGNE_INFO + 10;

        pdf.ligne(xi, yi, x + LARGEUR_UTILE - PAD, yi, BORDURE, 0.6f);
        yi += 1 + 10;

        int total = a.getEpreuves().size();
        pdf.texte("ÉPREUVES" + (total > 0 ? " (" + total + ")" : ""), xi, yi + 7, Police.GRAS, 7, GRIS_CLAIR);
        yi += 14;

        if (epreuves.isEmpty()) {
            pdf.texte("Aucune épreuve dans cet atelier.", xi, yi + 11, Police.NORMAL, 10, GRIS);
            return;
        }
        float largeurNom = LARGEUR_UTILE - 2 * PAD - 8 - 34;
        for (int i = 0; i < epreuves.size(); i++) {
            float yl = yi + i * HAUT_EPREUVE;
            pdf.rectangleArrondi(xi, yl, LARGEUR_UTILE - 2 * PAD - 8, HAUT_EPREUVE - 3, 4, FOND_LIGNE, null, 0);
            String numero = String.valueOf(numeroDepart + i + 1);
            pdf.rectangleArrondi(xi + 4, yl + 1.5f, 11, 11, 5.5f, BLEU_NUIT, null, 0);
            pdf.texteCentre(numero, xi + 9.5f, yl + 10, Police.GRAS, 7, BLANC);
            String nom = PdfSimple.tronquer(epreuves.get(i).getNom(), Police.NORMAL, 10.5f, largeurNom);
            pdf.texte(nom, xi + 24, yl + 10.5f, Police.NORMAL, 10.5f, TEXTE);
        }
    }

    // outils

    /** Charge le logo de l'application */
    private static synchronized void chargerLogo(PdfSimple pdf) {
        if (logoJpeg == null) {
            try (InputStream in = ExportPdfService.class.getResourceAsStream("/com/pps/parapente/view/logo.png")) {
                if (in == null) return;
                BufferedImage source = ImageIO.read(in);
                if (source == null) return;
                int taille = 240;
                BufferedImage rgb = new BufferedImage(taille, taille, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgb.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, taille, taille);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(source, 0, 0, taille, taille, null);
                g.dispose();
                logoJpeg = jpeg(rgb);
                logoTaillePx = taille;
            } catch (IOException | RuntimeException e) {
                return;   // logo absent ou illisible : export sans logo
            }
        }
        pdf.definirImage(logoJpeg, logoTaillePx, logoTaillePx);
    }

    private static byte[] jpeg(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(sortie)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.92f);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return sortie.toByteArray();
    }

    private static String pluriel(int n, String singulier, String plurielMot) {
        if (n == 0) return (singulier.startsWith("é") ? "aucune " : "aucun ") + singulier;
        return n + " " + (n > 1 ? plurielMot : singulier);
    }

    private static String majuscule(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(Locale.FRENCH) + s.substring(1);
    }
}
