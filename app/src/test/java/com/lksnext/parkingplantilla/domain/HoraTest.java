package com.lksnext.parkingplantilla.domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class HoraTest {

    // Tests para el constructor por defecto
    @Test
    public void testConstructorPorDefecto() {
        Hora hora = new Hora();
        assertNotNull(hora);
    }

    // Tests para el constructor con hora y minuto de inicio
    @Test
    public void testConstructorConInicio() {
        // Partición de equivalencia: valores válidos
        Hora hora1 = new Hora(10, 30);
        assertEquals(10, hora1.getHoraInicio());
        assertEquals(30, hora1.getMinuto());

        // Valores límite
        Hora hora2 = new Hora(0, 0);
        assertEquals(0, hora2.getHoraInicio());
        assertEquals(0, hora2.getMinuto());

        Hora hora3 = new Hora(23, 59);
        assertEquals(23, hora3.getHoraInicio());
        assertEquals(59, hora3.getMinuto());
    }

    // Tests para el constructor con hora y minuto de inicio y fin
    @Test
    public void testConstructorCompleto() {
        // Partición de equivalencia: valores válidos
        Hora hora1 = new Hora(8, 15, 18, 45);
        assertEquals(8, hora1.getHoraInicio());
        assertEquals(15, hora1.getMinuto());
        assertEquals(18, hora1.getHoraFin());
        assertEquals(45, hora1.getMinutoFin());

        // Valores límite
        Hora hora2 = new Hora(0, 0, 23, 59);
        assertEquals(0, hora2.getHoraInicio());
        assertEquals(0, hora2.getMinuto());
        assertEquals(23, hora2.getHoraFin());
        assertEquals(59, hora2.getMinutoFin());
    }

    // Test para getters
    @Test
    public void testGetters() {
        Hora hora = new Hora(14, 20, 15, 30);
        assertEquals(14, hora.getHoraInicio());
        assertEquals(20, hora.getMinuto());
        assertEquals(15, hora.getHoraFin());
        assertEquals(30, hora.getMinutoFin());
    }

    // Test para toMinutosInicio
    @Test
    public void testToMinutosInicio() {
        // Partición de equivalencia y cobertura de líneas
        Hora hora1 = new Hora(1, 10);
        assertEquals(70, hora1.toMinutosInicio());

        // Valores límite
        Hora hora2 = new Hora(0, 0);
        assertEquals(0, hora2.toMinutosInicio());

        Hora hora3 = new Hora(23, 59);
        assertEquals(1439, hora3.toMinutosInicio());
    }

    // Test para toMinutosFin
    @Test
    public void testToMinutosFin() {
        // Partición de equivalencia y cobertura de líneas
        Hora hora1 = new Hora(1, 1, 2, 20);
        assertEquals(140, hora1.toMinutosFin());

        // Valores límite
        Hora hora2 = new Hora(1, 1, 0, 0);
        assertEquals(0, hora2.toMinutosFin());

        Hora hora3 = new Hora(1, 1, 23, 59);
        assertEquals(1439, hora3.toMinutosFin());
    }

    // Test para toHoraMinutos
    @Test
    public void testToHoraMinutos() {
        // Cobertura de decisión para el formato (un solo dígito vs dos dígitos)
        Hora hora1 = new Hora(9, 5);
        assertEquals("09:05", hora1.toHoraMinutos());

        Hora hora2 = new Hora(15, 30);
        assertEquals("15:30", hora2.toHoraMinutos());
    }

    // Test para toHoraMinutosFin
    @Test
    public void testToHoraMinutosFin() {
        // Cobertura de decisión para el formato
        Hora hora1 = new Hora(1, 1, 8, 7);
        assertEquals("08:07", hora1.toHoraMinutosFin());

        Hora hora2 = new Hora(1, 1, 16, 40);
        assertEquals("16:40", hora2.toHoraMinutosFin());
    }

    // Test para toString
    @Test
    public void testToString() {
        // Cobertura de decisión para el formato
        Hora hora1 = new Hora(7, 8, 9, 10);
        assertEquals("07:08 - 09:10", hora1.toString());

        Hora hora2 = new Hora(11, 12, 13, 14);
        assertEquals("11:12 - 13:14", hora2.toString());
    }

    // Tests para compatibilidad con Firestore
    @Test
    public void testFirestoreCompatibility() {
        Hora hora = new Hora();
        hora.setMinuto(25);
        assertEquals(25, hora.getMinutoFirestore());
    }
}

