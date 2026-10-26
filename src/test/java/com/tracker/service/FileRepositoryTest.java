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

class FileRepositoryTest {

    @Test
    void ficheroInexistenteDevuelveListaVacia(@TempDir Path directorio) {
        assertTrue(new FileCandidaturaRepository(directorio.resolve("no-existe.txt")).findAll().isEmpty());
    }

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

    @Test
    void lineaConFechaInvalidaIndicaElNumeroDeLinea(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("malo.txt");
        Files.writeString(fichero, FileCandidaturaRepository.CABECERA + "\n1;Dev;Indra;pendiente;32/13/2026;;\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> new FileCandidaturaRepository(fichero).findAll());
        assertTrue(error.getMessage().contains("Línea 2"));
    }
}
