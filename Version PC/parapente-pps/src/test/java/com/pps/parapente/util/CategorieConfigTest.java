package com.pps.parapente.util;

import com.pps.parapente.model.Categorie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de CategorieConfig.
 *
 * - analyser(...) (package-private) est testée directement : c'est là que se
 *   trouve toute la logique de lecture de la règle.
 * - deduire(...) et descriptionNumerosOpen() sont testées en injectant la règle
 *   avec definirRegle(...), ce qui évite de lire (ou de créer !) le fichier
 *   data/categories.properties.
 * - La règle est un état statique : on la réinitialise après chaque test pour
 *   ne pas polluer les autres classes de test.
 *
 * Ce fichier doit être dans le package com.pps.parapente.util (côté test).
 */
class CategorieConfigTest {

    @AfterEach
    void reinitialiser() {
        CategorieConfig.definirRegle("");
    }

    private static void assertPlage(int debut, int fin, int[] plage) {
        assertArrayEquals(new int[]{debut, fin}, plage);
    }

    // =====================================================================
    // analyser
    // =====================================================================

    @Test
    @DisplayName("analyser : null, vide ou blanc => aucune plage")
    void analyser_videOuNull() {
        assertTrue(CategorieConfig.analyser(null).isEmpty());
        assertTrue(CategorieConfig.analyser("").isEmpty());
        assertTrue(CategorieConfig.analyser("   ").isEmpty());
    }

    @Test
    @DisplayName("analyser : un numéro seul => plage [n, n]")
    void analyser_numeroSeul() {
        List<int[]> plages = CategorieConfig.analyser("46");

        assertEquals(1, plages.size());
        assertPlage(46, 46, plages.get(0));
    }

    @Test
    @DisplayName("analyser : plage début-fin")
    void analyser_plage() {
        List<int[]> plages = CategorieConfig.analyser("100-199");

        assertEquals(1, plages.size());
        assertPlage(100, 199, plages.get(0));
    }

    @Test
    @DisplayName("analyser : numéro et plage séparés par une virgule (exemple du fichier)")
    void analyser_numeroEtPlage() {
        List<int[]> plages = CategorieConfig.analyser("46, 100-199");

        assertEquals(2, plages.size());
        assertPlage(46, 46, plages.get(0));
        assertPlage(100, 199, plages.get(1));
    }

    @Test
    @DisplayName("analyser : le point-virgule est aussi un séparateur")
    void analyser_pointVirgule() {
        List<int[]> plages = CategorieConfig.analyser("5; 10-12, 20");

        assertEquals(3, plages.size());
        assertPlage(5, 5, plages.get(0));
        assertPlage(10, 12, plages.get(1));
        assertPlage(20, 20, plages.get(2));
    }

    @Test
    @DisplayName("analyser : plage inversée (199-100) normalisée en [100, 199]")
    void analyser_plageInversee() {
        List<int[]> plages = CategorieConfig.analyser("199-100");

        assertPlage(100, 199, plages.get(0));
    }

    @Test
    @DisplayName("analyser : espaces autour des nombres et du tiret tolérés")
    void analyser_espaces() {
        List<int[]> plages = CategorieConfig.analyser("  7 - 9 ,  12  ");

        assertEquals(2, plages.size());
        assertPlage(7, 9, plages.get(0));
        assertPlage(12, 12, plages.get(1));
    }

    @Test
    @DisplayName("analyser : séparateurs en trop (,,) ignorés")
    void analyser_separateursEnTrop() {
        List<int[]> plages = CategorieConfig.analyser(",,46,, ,");

        assertEquals(1, plages.size());
        assertPlage(46, 46, plages.get(0));
    }

    @Test
    @DisplayName("analyser : morceaux invalides ignorés, les valides conservés")
    void analyser_morceauxInvalidesIgnores() {
        List<int[]> plages = CategorieConfig.analyser("abc, 12, 5-x, -, 3.5, 99999999999");

        assertEquals(1, plages.size());
        assertPlage(12, 12, plages.get(0));
    }

    @Test
    @DisplayName("analyser : les doublons ne sont pas fusionnés")
    void analyser_doublonsConserves() {
        assertEquals(2, CategorieConfig.analyser("5, 5").size());
    }

    // =====================================================================
    // deduire
    // =====================================================================

    @Test
    @DisplayName("deduire : numéro null => pompier")
    void deduire_numeroNull_pompier() {
        CategorieConfig.definirRegle("1-1000");

        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(null));
    }

    @Test
    @DisplayName("deduire : aucune règle => tout le monde est pompier")
    void deduire_aucuneRegle_pompier() {
        CategorieConfig.definirRegle("");

        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(46));
        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(150));
    }

    @Test
    @DisplayName("deduire : numéro isolé => open uniquement pour ce numéro")
    void deduire_numeroIsole() {
        CategorieConfig.definirRegle("46");

        assertEquals(Categorie.OPEN, CategorieConfig.deduire(46));
        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(45));
        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(47));
    }

    @Test
    @DisplayName("deduire : bornes de la plage comprises")
    void deduire_bornesIncluses() {
        CategorieConfig.definirRegle("100-199");

        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(99));
        assertEquals(Categorie.OPEN, CategorieConfig.deduire(100));
        assertEquals(Categorie.OPEN, CategorieConfig.deduire(150));
        assertEquals(Categorie.OPEN, CategorieConfig.deduire(199));
        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(200));
    }

    @Test
    @DisplayName("deduire : plusieurs règles cumulées (exemple du fichier : 46, 100-199)")
    void deduire_plusieursRegles() {
        CategorieConfig.definirRegle("46, 100-199");

        assertEquals(Categorie.OPEN, CategorieConfig.deduire(46));
        assertEquals(Categorie.OPEN, CategorieConfig.deduire(120));
        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(47));
        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(1));
    }

    @Test
    @DisplayName("deduire : une plage inversée fonctionne comme la plage normale")
    void deduire_plageInversee() {
        CategorieConfig.definirRegle("199-100");

        assertEquals(Categorie.OPEN, CategorieConfig.deduire(150));
    }

    @Test
    @DisplayName("deduire : règle entièrement invalide => tout le monde pompier")
    void deduire_regleInvalide_pompier() {
        CategorieConfig.definirRegle("n'importe quoi");

        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(46));
    }

    @Test
    @DisplayName("definirRegle remplace la règle précédente")
    void definirRegle_remplaceLaPrecedente() {
        CategorieConfig.definirRegle("46");
        assertEquals(Categorie.OPEN, CategorieConfig.deduire(46));

        CategorieConfig.definirRegle("50");
        assertEquals(Categorie.POMPIER, CategorieConfig.deduire(46));
        assertEquals(Categorie.OPEN, CategorieConfig.deduire(50));
    }

    // =====================================================================
    // descriptionNumerosOpen
    // =====================================================================

    @Test
    @DisplayName("descriptionNumerosOpen : numéros et plages séparés par « , »")
    void description_numerosEtPlages() {
        CategorieConfig.definirRegle("46, 100-199");

        assertEquals("46, 100-199", CategorieConfig.descriptionNumerosOpen());
    }

    @Test
    @DisplayName("descriptionNumerosOpen : plage d'un seul numéro affichée comme un numéro")
    void description_plageDUnSeulNumero() {
        CategorieConfig.definirRegle("46-46");

        assertEquals("46", CategorieConfig.descriptionNumerosOpen());
    }

    @Test
    @DisplayName("descriptionNumerosOpen : normalise les plages inversées et les séparateurs")
    void description_normalise() {
        CategorieConfig.definirRegle("199-100; 5");

        assertEquals("100-199, 5", CategorieConfig.descriptionNumerosOpen());
    }

    @Test
    @DisplayName("descriptionNumerosOpen : aucune règle => chaîne vide")
    void description_aucuneRegle() {
        CategorieConfig.definirRegle("");

        assertEquals("", CategorieConfig.descriptionNumerosOpen());
    }
}