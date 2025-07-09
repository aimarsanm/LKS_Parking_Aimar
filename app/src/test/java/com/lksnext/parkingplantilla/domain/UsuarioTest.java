package com.lksnext.parkingplantilla.domain;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UsuarioTest {

    private Usuario usuario;

    @Before
    public void setUp() {
        usuario = new Usuario();
    }

    // Pruebas de Caja Blanca: Cobertura de Sentencias

    @Test
    public void testDefaultConstructor() {
        Usuario newUser = new Usuario();
        assertNotNull("El constructor por defecto debe crear un objeto no nulo", newUser);
        assertNull("El nombre debe ser nulo después del constructor por defecto", newUser.getNombre());
        assertNull("El apellido debe ser nulo después del constructor por defecto", newUser.getApellido());
        assertNull("El email debe ser nulo después del constructor por defecto", newUser.getEmail());
        assertNull("La contraseña debe ser nula después del constructor por defecto", newUser.getPassword());
    }

    @Test
    public void testParameterizedConstructor() {
        Usuario newUser = new Usuario("Aimar", "Sanm", "aimar@example.com", "password123");
        assertNotNull("El constructor parametrizado debe crear un objeto no nulo", newUser);
        assertEquals("El nombre debe ser 'Aimar'", "Aimar", newUser.getNombre());
        assertEquals("El apellido debe ser 'Sanm'", "Sanm", newUser.getApellido());
        assertEquals("El email debe ser 'aimar@example.com'", "aimar@example.com", newUser.getEmail());
        assertEquals("La contraseña debe ser 'password123'", "password123", newUser.getPassword());
    }

    @Test
    public void testSettersAndGetters() {
        // Probar todos los setters y getters para una cobertura de línea completa
        usuario.setNombre("Aimar");
        assertEquals("getNombre debe devolver 'Aimar'", "Aimar", usuario.getNombre());

        usuario.setApellido("Sanm");
        assertEquals("getApellido debe devolver 'Sanm'", "Sanm", usuario.getApellido());

        usuario.setEmail("aimar@example.com");
        assertEquals("getEmail debe devolver 'aimar@example.com'", "aimar@example.com", usuario.getEmail());

        usuario.setPassword("password123");
        assertEquals("getPassword debe devolver 'password123'", "password123", usuario.getPassword());
    }

    // Pruebas de Caja Negra: Particiones de Equivalencia y Valores Límite

    // Pruebas para setNombre/getNombre
    @Test
    public void testNombre_Valid() {
        usuario.setNombre("Aimar");
        assertEquals("Aimar", usuario.getNombre());
    }

    @Test
    public void testNombre_Null() {
        usuario.setNombre(null);
        assertNull(usuario.getNombre());
    }

    @Test
    public void testNombre_Empty() {
        usuario.setNombre("");
        assertEquals("", usuario.getNombre());
    }

    // Pruebas para setApellido/getApellido
    @Test
    public void testApellido_Valid() {
        usuario.setApellido("Sanm");
        assertEquals("Sanm", usuario.getApellido());
    }

    @Test
    public void testApellido_Null() {
        usuario.setApellido(null);
        assertNull(usuario.getApellido());
    }

    @Test
    public void testApellido_Empty() {
        usuario.setApellido("");
        assertEquals("", usuario.getApellido());
    }

    // Pruebas para setEmail/getEmail
    @Test
    public void testEmail_Valid() {
        usuario.setEmail("test@example.com");
        assertEquals("test@example.com", usuario.getEmail());
    }

    @Test
    public void testEmail_Null() {
        usuario.setEmail(null);
        assertNull(usuario.getEmail());
    }

    @Test
    public void testEmail_Empty() {
        usuario.setEmail("");
        assertEquals("", usuario.getEmail());
    }

    // Pruebas para setPassword/getPassword
    @Test
    public void testPassword_Valid() {
        usuario.setPassword("password123");
        assertEquals("password123", usuario.getPassword());
    }

    @Test
    public void testPassword_Null() {
        usuario.setPassword(null);
        assertNull(usuario.getPassword());
    }

    @Test
    public void testPassword_Empty() {
        usuario.setPassword("");
        assertEquals("", usuario.getPassword());
    }
}

