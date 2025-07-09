package com.lksnext.parkingplantilla.domain;

import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlazaTest {

    // Test for default constructor Plaza()
    @Test
    public void testDefaultConstructor() {
        // White Box: Line Coverage - Execute the constructor
        Plaza plaza = new Plaza();

        // Black Box: Equivalence Partitioning (No input) - Verify default state
        assertNotNull("El objeto plaza no debería ser nulo", plaza);
        assertEquals("El id por defecto debería ser 0", 0, plaza.id);
        assertNull("El tipo por defecto debería ser nulo", plaza.tipo);
    }

    // Tests for constructor Plaza(String tipo)
    @Test
    public void testTipoConstructor_ValidTipo() {
        // White Box: Line Coverage - Execute the constructor with a valid string
        // Black Box: Equivalence Partitioning - Partition: Valid non-empty string
        String tipo = "Coche";
        Plaza plaza = new Plaza(tipo);

        assertNotNull("El objeto plaza no debería ser nulo", plaza);
        assertEquals("El id por defecto debería ser 0", 0, plaza.id);
        assertEquals("El tipo debería ser el proporcionado", tipo, plaza.tipo);
    }

    @Test
    public void testTipoConstructor_NullTipo() {
        // White Box: Line Coverage - Execute the constructor
        // Black Box: Equivalence Partitioning - Partition: Null string
        String tipo = null;
        Plaza plaza = new Plaza(tipo);

        assertNotNull("El objeto plaza no debería ser nulo", plaza);
        assertEquals("El id por defecto debería ser 0", 0, plaza.id);
        assertNull("El tipo debería ser nulo", plaza.tipo);
    }

    @Test
    public void testTipoConstructor_EmptyTipo() {
        // White Box: Line Coverage - Execute the constructor
        // Black Box: Equivalence Partitioning - Partition: Empty string (Boundary case)
        String tipo = "";
        Plaza plaza = new Plaza(tipo);

        assertNotNull("El objeto plaza no debería ser nulo", plaza);
        assertEquals("El id por defecto debería ser 0", 0, plaza.id);
        assertEquals("El tipo debería ser una cadena vacía", "", plaza.tipo);
    }

    // Tests for constructor Plaza(long id, String tipo)
    @Test
    public void testIdAndTipoConstructor_PositiveIdValidTipo() {
        // White Box: Line Coverage - Execute the constructor
        // Black Box: Equivalence Partitioning - Partitions: Positive ID, Valid non-empty string
        // Black Box: Boundary Value - Boundary: id = 1
        long id = 1L;
        String tipo = "Moto";
        Plaza plaza = new Plaza(id, tipo);

        assertNotNull("El objeto plaza no debería ser nulo", plaza);
        assertEquals("El id debería ser el proporcionado", id, plaza.id);
        assertEquals("El tipo debería ser el proporcionado", tipo, plaza.tipo);
    }

    @Test
    public void testIdAndTipoConstructor_ZeroIdValidTipo() {
        // White Box: Line Coverage - Execute the constructor
        // Black Box: Equivalence Partitioning - Partitions: Zero ID, Valid non-empty string
        // Black Box: Boundary Value - Boundary: id = 0
        long id = 0L;
        String tipo = "Bicicleta";
        Plaza plaza = new Plaza(id, tipo);

        assertNotNull("El objeto plaza no debería ser nulo", plaza);
        assertEquals("El id debería ser 0", id, plaza.id);
        assertEquals("El tipo debería ser el proporcionado", tipo, plaza.tipo);
    }

    @Test
    public void testIdAndTipoConstructor_MaxIdValidTipo() {
        // White Box: Line Coverage - Execute the constructor
        // Black Box: Equivalence Partitioning - Partitions: Positive ID, Valid non-empty string
        // Black Box: Boundary Value - Boundary: id = Long.MAX_VALUE
        long id = Long.MAX_VALUE;
        String tipo = "Patinete";
        Plaza plaza = new Plaza(id, tipo);

        assertNotNull("El objeto plaza no debería ser nulo", plaza);
        assertEquals("El id debería ser Long.MAX_VALUE", id, plaza.id);
        assertEquals("El tipo debería ser el proporcionado", tipo, plaza.tipo);
    }

    @Test
    public void testIdAndTipoConstructor_PositiveIdNullTipo() {
        // White Box: Line Coverage - Execute the constructor
        // Black Box: Equivalence Partitioning - Partitions: Positive ID, Null string
        long id = 10L;
        String tipo = null;
        Plaza plaza = new Plaza(id, tipo);

        assertNotNull("El objeto plaza no debería ser nulo", plaza);
        assertEquals("El id debería ser el proporcionado", id, plaza.id);
        assertNull("El tipo debería ser nulo", plaza.tipo);
    }

    @Test
    public void testIdAndTipoConstructor_PositiveIdEmptyTipo() {
        // White Box: Line Coverage - Execute the constructor
        // Black Box: Equivalence Partitioning - Partitions: Positive ID, Empty string (Boundary)
        long id = 20L;
        String tipo = "";
        Plaza plaza = new Plaza(id, tipo);

        assertNotNull("El objeto plaza no debería ser nulo", plaza);
        assertEquals("El id debería ser el proporcionado", id, plaza.id);
        assertEquals("El tipo debería ser una cadena vacía", "", plaza.tipo);
    }
}
