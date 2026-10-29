package com.tracker.service;

import com.tracker.exception.TrackerException;
import com.tracker.model.Candidatura;
import com.tracker.model.Estado;
import com.tracker.repository.FileCandidaturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprueba la persistencia y los errores del formato mediante ficheros temporales. */
class FileRepositoryTest {

    /** Rechaza un título sin contenido e identifica la línea del CSV. */
    @Test
    void lineaConTituloVacioLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("titulo-vacio.csv");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\n1;;Indra;pendiente;14/10/2026;;\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());

        assertEquals("Línea 2: el título es obligatorio", error.getMessage());
    }

    /** Los espacios tampoco cuentan como título. */
    @Test
    void lineaConTituloEnBlancoLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("titulo-blanco.csv");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\n1;   ;Indra;pendiente;14/10/2026;;\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());

        assertEquals("Línea 2: el título es obligatorio", error.getMessage());
    }

    /** Rechaza una empresa sin contenido e identifica la línea del CSV. */
    @Test
    void lineaConEmpresaVaciaLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("empresa-vacia.csv");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\n1;Dev;;pendiente;14/10/2026;;\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());

        assertEquals("Línea 2: la empresa es obligatoria", error.getMessage());
    }

    /** Los espacios tampoco cuentan como empresa. */
    @Test
    void lineaConEmpresaEnBlancoLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("empresa-blanca.csv");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\n1;Dev;   ;pendiente;14/10/2026;;\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());

        assertEquals("Línea 2: la empresa es obligatoria", error.getMessage());
    }

    /** Comprueba que se puede empezar a trabajar sin un fichero de datos previo. */
    @Test
    void ficheroInexistenteDevuelveListaVacia(@TempDir Path directorio) {
        assertTrue(new FileCandidaturaRepository(directorio.resolve("no-existe.txt")).findAll().isEmpty());
    }

    /** Comprueba la lectura tras guardar y la limpieza de separadores en el título. */
    @Test
    void guardarYLeerConservaLosDatos(@TempDir Path directorio) {
        FileCandidaturaRepository repositorio = new FileCandidaturaRepository(directorio.resolve("datos.txt"));
        Candidatura original = new Candidatura(1, "Dev; Java", "Indra", Estado.PENDIENTE,
                LocalDate.of(2026, 10, 14), "https://indra.es", "nota");

        repositorio.saveAll(List.of(original));
        Candidatura leida = repositorio.findAll().get(0);

        assertEquals("Dev, Java", leida.titulo());
        assertEquals(LocalDate.of(2026, 10, 14), leida.fechaPostulacion());
        assertEquals(Estado.PENDIENTE, leida.estado());
    }

    /** Comprueba que el error de fecha identifica la línea del fichero. */
    @Test
    void lineaConFechaInvalidaIndicaElNumeroDeLinea(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("malo.txt");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA + "\n1;Dev;Indra;pendiente;32/13/2026;;\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());
        assertTrue(error.getMessage().contains("Línea 2"));
    }

    /** Comprueba que una línea incompleta informa del número de línea y los campos. */
    @Test
    void lineaConPocosCamposLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("incompleto.txt");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA + "\n1;Dev;Indra\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());

        assertEquals("Línea 2 incorrecta: se esperaban 7 campos y hay 3", error.getMessage());
    }

    /** Comprueba que un estado desconocido se rechaza indicando la línea afectada. */
    @Test
    void lineaConEstadoInvalidoLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("estado-invalido.txt");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\n1;Dev;Indra;desconocido;14/10/2026;;\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());

        assertEquals("Línea 2: Estado no válido: 'desconocido'. Valores permitidos: "
                + "pendiente, entrevista, rechazado, aceptado", error.getMessage());
    }

    /** Comprueba que el identificador debe ser numérico. */
    @Test
    void lineaConIdNoNumericoLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("id-invalido.txt");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\nabc;Dev;Indra;pendiente;14/10/2026;;\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());

        assertEquals("Línea 2: el id 'abc' no es un número", error.getMessage());
    }

    /** Comprueba un fichero existente de cero bytes, distinto del fichero inexistente. */
    @Test
    void ficheroVacioDevuelveListaVacia(@TempDir Path directorio) throws IOException {
        Path fichero = Files.createFile(directorio.resolve("vacio.txt"));

        assertTrue(new FileCandidaturaRepository(fichero).findAll().isEmpty());
    }

    /** Comprueba que una fecha imposible no se corrige silenciosamente al leer. */
    @Test
    void lineaConDiaImposibleLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("dia-imposible.txt");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\n1;Dev;Indra;pendiente;31/02/2026;;\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());

        assertEquals("Línea 2: fecha '31/02/2026' no válida (formato dd/MM/yyyy)", error.getMessage());
    }

    /** Comprueba que febrero no tiene 29 días en un año no bisiesto. */
    @Test
    void lineaConFebreroNoBisiestoLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("no-bisiesto.txt");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\n1;Dev;Indra;pendiente;29/02/2026;;\n");

        assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());
    }

    /** Comprueba la lectura y escritura de una fecha válida de año bisiesto. */
    @Test
    void guardarYLeerConservaFechaBisiesta(@TempDir Path directorio) {
        FileCandidaturaRepository repositorio = new FileCandidaturaRepository(directorio.resolve("bisiesto.txt"));
        Candidatura original = new Candidatura(1, "Dev", "Indra", Estado.PENDIENTE,
                LocalDate.of(2024, 2, 29), "", "");

        repositorio.saveAll(List.of(original));

        assertEquals(List.of(original), repositorio.findAll());
    }

    /** Comprueba que los campos sobrantes no se descartan silenciosamente. */
    @Test
    void lineaConCamposSobrantesLanzaExcepcion(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("sobrantes.txt");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\n1;Dev;Indra;pendiente;14/10/2026;;;sobrante\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());

        assertEquals("Línea 2 incorrecta: se esperaban 7 campos y hay 8", error.getMessage());
    }

    /** Comprueba que los campos opcionales vacíos al final cuentan como campos. */
    @Test
    void lineaConOpcionalesVaciosConservaLosSieteCampos(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("opcionales-vacios.txt");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA
                + "\n1;Dev;Indra;pendiente;14/10/2026;;\n");

        List<Candidatura> resultado = new FileCandidaturaRepository(fichero).findAll();

        assertEquals(List.of(new Candidatura(1, "Dev", "Indra", Estado.PENDIENTE,
                LocalDate.of(2026, 10, 14), "", "")), resultado);
    }
}
