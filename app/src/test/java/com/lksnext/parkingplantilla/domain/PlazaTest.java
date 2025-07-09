package com.lksnext.parkingplantilla.domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlazaTest {
    @Test
    public void testConstructorYGetters() {
        Plaza plaza = new Plaza(123L, "cubierta");
        assertEquals(123L, plaza.getId());
        assertEquals("cubierta", plaza.getTipo());
    }

    @Test
    public void testSetters() {
        Plaza plaza = new Plaza();
        plaza.setId(456L);
        plaza.setTipo("descubierta");
        assertEquals(456L, plaza.getId());
        assertEquals("descubierta", plaza.getTipo());
    }
}

