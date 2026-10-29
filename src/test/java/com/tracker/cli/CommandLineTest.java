package com.tracker.cli;

import com.tracker.exception.TrackerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprueba la interpretación de los argumentos y los errores de sintaxis de la CLI.
 */
class CommandLineTest {

    /** Comprueba que se elimina el guion inicial de la operación. */
    @Test
    void operacionConGuionInicialSeInterpreta() {
        CommandLine linea = CommandLine.parsear(new String[]{"-listar"});

        assertEquals("listar", linea.operacion());
    }

    /** Comprueba que también se admite una operación sin guion. */
    @Test
    void operacionSinGuionInicialSeInterpreta() {
        CommandLine linea = CommandLine.parsear(new String[]{"listar"});

        assertEquals("listar", linea.operacion());
    }

    /** Comprueba el error de una opción al final que necesita un valor. */
    @Test
    void opcionSinValorLanzaExcepcion() {
        TrackerException error = assertThrows(TrackerException.class,
                () -> CommandLine.parsear(new String[]{"listar", "--estado"}));

        assertEquals("La opción --estado necesita un valor.", error.getMessage());
    }

    /** Comprueba que otra opción no se interpreta como el valor de la anterior. */
    @Test
    void opcionSeguidaDeOtraOpcionLanzaExcepcion() {
        TrackerException error = assertThrows(TrackerException.class,
                () -> CommandLine.parsear(new String[]{"listar", "--estado", "--empresa", "Indra"}));

        assertEquals("La opción --estado necesita un valor.", error.getMessage());
    }

    /** Comprueba que solo se admite un argumento posicional. */
    @Test
    void segundoArgumentoPosicionalLanzaExcepcion() {
        TrackerException error = assertThrows(TrackerException.class,
                () -> CommandLine.parsear(new String[]{"ver", "1", "sobrante"}));

        assertEquals("Argumento inesperado: 'sobrante'", error.getMessage());
    }

    /** Comprueba el error cuando no se ha indicado ninguna operación. */
    @Test
    void sinArgumentosLanzaExcepcion() {
        TrackerException error = assertThrows(TrackerException.class,
                () -> CommandLine.parsear(new String[]{}));

        assertEquals("No se indicó ninguna operación.", error.getMessage());
    }

    /** Comprueba que una entrada nula también informa de la operación ausente. */
    @Test
    void argumentosNulosLanzanExcepcion() {
        TrackerException error = assertThrows(TrackerException.class,
                () -> CommandLine.parsear(null));

        assertEquals("No se indicó ninguna operación.", error.getMessage());
    }

    /** Comprueba que se conserva el valor de cada opción, incluidos los espacios. */
    @Test
    void opcionesConValorSePuedenConsultar() {
        CommandLine linea = CommandLine.parsear(new String[]{
                "listar", "--estado", "entrevista", "--empresa", "NTT Data"});

        assertEquals("entrevista", linea.opcion("estado"));
        assertEquals("NTT Data", linea.opcion("empresa"));
        assertNull(linea.opcion("ordenar"));
    }

    /** Comprueba la recuperación del identificador posicional. */
    @Test
    void argumentoPosicionalSePuedeConsultar() {
        CommandLine linea = CommandLine.parsear(new String[]{"ver", "13"});

        assertEquals("13", linea.posicionalObligatorio("id"));
    }

    /** Comprueba el mensaje cuando falta un argumento posicional obligatorio. */
    @Test
    void argumentoPosicionalAusenteLanzaExcepcion() {
        CommandLine linea = CommandLine.parsear(new String[]{"ver"});

        TrackerException error = assertThrows(TrackerException.class,
                () -> linea.posicionalObligatorio("id"));

        assertEquals("Falta el argumento: id", error.getMessage());
    }
}
