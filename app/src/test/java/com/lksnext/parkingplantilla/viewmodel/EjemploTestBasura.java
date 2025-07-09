package com.lksnext.parkingplantilla.viewmodel;

import org.junit.Test;
import static org.junit.Assert.*;

public class EjemploTestBasura {

    @Test
    public void testInutil_queSiemprePasa() {
        int a = 1;
        int b = 1;
        assertEquals(a, b);
    }

    @Test
    public void testBasura_queHaceNada() {
        String cadena = "basura";
        assertTrue(cadena.contains("b"));
    }



    @Test
    public void testConMuchasVariablesInutiles() {
        int x = 5;
        int y = 10;
        int z = x + y;
        int w = z * 2;
        String mensaje = "Resultado: " + w;
        assertNotNull(mensaje);
    }
}