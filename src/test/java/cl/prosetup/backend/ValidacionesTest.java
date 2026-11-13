package cl.prosetup.backend;

import cl.prosetup.backend.util.Validaciones;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidacionesTest {

    @Test
    void runValidoAceptaFormatosConYSinPuntos() {
        assertTrue(Validaciones.runValido("12.345.678-5"));
        assertTrue(Validaciones.runValido("123456785"));
        assertTrue(Validaciones.runValido("11111111-1"));
    }

    @Test
    void runConDigitoVerificadorIncorrectoSeRechaza() {
        assertFalse(Validaciones.runValido("12.345.678-9"));
        assertFalse(Validaciones.runValido("abc"));
        assertFalse(Validaciones.runValido(null));
    }

    @Test
    void soloSeAceptanDominiosPermitidos() {
        assertTrue(Validaciones.correoPermitido("alguien@duoc.cl"));
        assertTrue(Validaciones.correoPermitido("profe@profesor.duoc.cl"));
        assertTrue(Validaciones.correoPermitido("persona@gmail.com"));
        assertFalse(Validaciones.correoPermitido("persona@hotmail.com"));
        assertFalse(Validaciones.correoPermitido(null));
    }

    @Test
    void passwordDebeTenerEntre4y10Caracteres() {
        assertTrue(Validaciones.passwordValida("1234"));
        assertTrue(Validaciones.passwordValida("1234567890"));
        assertFalse(Validaciones.passwordValida("123"));
        assertFalse(Validaciones.passwordValida("12345678901"));
    }
}
