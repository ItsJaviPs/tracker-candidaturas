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

    /** Rechaza una errata en lugar de ignorar el filtro. */
    @Test
    void opcionDesconocidaLanzaExcepcion() {
        TrackerException error = assertThrows(TrackerException.class,
                () -> CommandLine.parsear(new String[]{"-listar", "--estdo", "entrevista"}));

        assertEquals("La opción --estdo no está permitida para 'listar'.", error.getMessage());
    }

    /** Una opción conocida también debe corresponder a la operación solicitada. */
    @Test
    void opcionDeOtraOperacionLanzaExcepcion() {
        TrackerException error = assertThrows(TrackerException.class,
                () -> CommandLine.parsear(new String[]{"-borrar", "1", "--estado", "pendiente"}));

        assertEquals("La opción --estado no está permitida para 'borrar'.", error.getMessage());
    }

    /** Conserva todos los argumentos de alta y los dos alias de la operación. */
    @Test
    void anadirConAmbosAliasAdmiteTodasSusOpciones() {
        for (String operacion : new String[]{"-añadir", "-anadir"}) {
            CommandLine linea = CommandLine.parsear(new String[]{operacion,
                    "--titulo", "Dev Java", "--empresa", "Indra", "--estado", "pendiente",
                    "--fecha", "29/10/2026", "--enlace", "https://example.com", "--notas", "Mi nota"});

            assertEquals("Dev Java", linea.opcion("titulo"));
            assertEquals("Indra", linea.opcion("empresa"));
            assertEquals("pendiente", linea.opcion("estado"));
            assertEquals("29/10/2026", linea.opcion("fecha"));
            assertEquals("https://example.com", linea.opcion("enlace"));
            assertEquals("Mi nota", linea.opcion("notas"));
        }
    }

    /** Conserva la ordenación y la normalización de las opciones en mayúsculas. */
    @Test
    void listarAdmiteFiltrosYOrdenEnMayusculas() {
        CommandLine linea = CommandLine.parsear(new String[]{"-LISTAR",
                "--ESTADO", "entrevista", "--EMPRESA", "Indra", "--ORDENAR", "fecha"});

        assertEquals("entrevista", linea.opcion("estado"));
        assertEquals("Indra", linea.opcion("empresa"));
        assertEquals("fecha", linea.opcion("ordenar"));
    }

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
