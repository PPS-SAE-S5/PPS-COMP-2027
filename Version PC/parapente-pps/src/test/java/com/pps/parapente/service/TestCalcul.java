package com.pps.parapente.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class CalculServiceTest {
     private final CalculService service = new CalculService();

    @Test
    void formuleSimple() {
        double r = service.evaluerFormule("100 - temps*2 + nbrBalises*10",
                Map.of("temps", 5.0, "nbrBalises", 3.0));
        assertEquals(120.0, r, 0.0001);
    }

    @Test
    void formuleVideLeveUneException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.evaluerFormule("  ", Map.of()));
    }

    @Test
    void variableInconnueLeveUneException() {
        assertThrows(Exception.class,
                () -> service.evaluerFormule("x + y", Map.of("x", 1.0)));
    }
}