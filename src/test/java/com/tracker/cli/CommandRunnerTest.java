package com.tracker.cli;

import com.tracker.exception.TrackerException;
import com.tracker.model.Candidatura;
import com.tracker.model.Estado;
import com.tracker.repository.FileCandidaturaRepository;
import com.tracker.service.CandidaturaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprueba la salida de la CLI y su integración con el servicio y un fichero temporal.
 */
class CommandRunnerTest {

    @TempDir
    private Path directorio;

    private FileCandidaturaRepository repositorio;
    private ByteArrayOutputStream contenidoSalida;
    private PrintStream salida;
    private CommandRunner ejecutor;

    /** Prepara un fichero y una salida independientes para cada prueba. */
    @BeforeEach
    void preparar() {
        repositorio = new FileCandidaturaRepository(directorio.resolve("candidaturas.txt"));
        contenidoSalida = new ByteArrayOutputStream();
        salida = new PrintStream(contenidoSalida, true, StandardCharsets.UTF_8);
        ejecutor = new CommandRunner(new CandidaturaService(repositorio), salida);
    }

    /** Cierra la salida creada por la prueba. */
    @AfterEach
    void cerrarSalida() {
        salida.close();
    }

    /** Comprueba el mensaje del listado sin candidaturas. */
    @Test
    void listarVacioMuestraMensaje() {
        ejecutor.ejecutar(new String[]{"-listar"});

        assertEquals("No hay candidaturas que mostrar." + System.lineSeparator(), textoSalida());
    }

    /** Comprueba que ambos filtros se aplican y se muestran solo las coincidencias. */
    @Test
    void listarConFiltrosMuestraSoloLasCoincidencias() {
        repositorio.saveAll(List.of(
                candidatura(1, "Java seleccionado", "Indra", Estado.ENTREVISTA),
                candidatura(2, "QA pendiente", "Indra", Estado.PENDIENTE),
                candidatura(3, "Soporte externo", "Accenture", Estado.ENTREVISTA)));

        ejecutor.ejecutar(new String[]{"-listar", "--estado", "entrevista", "--empresa", "indra"});

        String texto = textoSalida();
        assertTrue(texto.contains("Java seleccionado"));
        assertTrue(texto.contains("Indra"));
        assertTrue(texto.contains("entrevista"));
        assertTrue(texto.contains("14/10/2026"));
        assertTrue(texto.contains("1 candidatura(s)."));
        assertFalse(texto.contains("QA pendiente"));
        assertFalse(texto.contains("Soporte externo"));
    }

    /** Comprueba el mensaje de alta y todos los datos guardados en el fichero. */
    @Test
    void anadirGuardaCandidaturaYConfirmaSuId() {
        ejecutor.ejecutar(new String[]{"-anadir", "--titulo", "Dev Java", "--empresa", "Indra",
                "--estado", "pendiente", "--fecha", "14/10/2026",
                "--enlace", "https://example.com/oferta", "--notas", "Primera solicitud"});

        assertEquals("Candidatura añadida con id 1." + System.lineSeparator(), textoSalida());
        assertEquals(List.of(new Candidatura(1, "Dev Java", "Indra", Estado.PENDIENTE,
                LocalDate.of(2026, 10, 14), "https://example.com/oferta", "Primera solicitud")),
                repositorio.findAll());
    }

    /** Comprueba el detalle completo, incluidos los guiones de los campos vacíos. */
    @Test
    void verMuestraElDetalleDeLaCandidatura() {
        repositorio.saveAll(List.of(candidatura(7, "Dev Java", "Indra", Estado.ENTREVISTA)));

        ejecutor.ejecutar(new String[]{"-ver", "7"});

        assertEquals(String.join(System.lineSeparator(),
                "ID:          7", "Título:      Dev Java", "Empresa:     Indra",
                "Estado:      entrevista", "Postulación: 14/10/2026",
                "Enlace:      -", "Notas:       -", ""), textoSalida());
    }

    /** Comprueba que el borrado se guarda y conserva las demás candidaturas. */
    @Test
    void borrarEliminaSoloLaCandidaturaIndicada() {
        Candidatura conservada = candidatura(2, "QA", "Accenture", Estado.PENDIENTE);
        repositorio.saveAll(List.of(candidatura(1, "Dev Java", "Indra", Estado.ENTREVISTA), conservada));

        ejecutor.ejecutar(new String[]{"-borrar", "1"});

        assertEquals("Candidatura 1 ('Dev Java' en Indra) eliminada." + System.lineSeparator(), textoSalida());
        assertEquals(List.of(conservada), repositorio.findAll());
    }

    /** Comprueba el error de una operación desconocida y que no modifica los datos. */
    @Test
    void operacionDesconocidaLanzaExcepcion() {
        Candidatura original = candidatura(1, "Dev Java", "Indra", Estado.PENDIENTE);
        repositorio.saveAll(List.of(original));

        TrackerException error = assertThrows(TrackerException.class,
                () -> ejecutor.ejecutar(new String[]{"-inventada"}));

        assertEquals("Operación desconocida: 'inventada'.", error.getMessage());
        assertEquals("", textoSalida());
        assertEquals(List.of(original), repositorio.findAll());
    }

    /** Devuelve la salida capturada usando la misma codificación que el ejecutor. */
    private String textoSalida() {
        return contenidoSalida.toString(StandardCharsets.UTF_8);
    }

    /** Crea datos de prueba con una fecha fija y campos opcionales vacíos. */
    private Candidatura candidatura(int id, String titulo, String empresa, Estado estado) {
        return new Candidatura(id, titulo, empresa, estado, LocalDate.of(2026, 10, 14), "", "");
    }
}
