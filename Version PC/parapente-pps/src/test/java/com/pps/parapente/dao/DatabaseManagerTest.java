package com.pps.parapente.dao;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DatabaseManagerTest {

    @Test
    void dossierDeDonneesEstUnCheminAbsoluTermineParData() {
        Path dossier = DatabaseManager.getDataDirectory();

        assertTrue(dossier.isAbsolute());
        assertEquals("data", dossier.getFileName().toString());
    }
}