package com.tracker.service;

import com.tracker.exception.TrackerException;
import com.tracker.model.Candidatura;
import com.tracker.model.Estado;
import com.tracker.repository.CandidaturaRepository;
import com.tracker.repository.FileCandidaturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidaturaServiceTest {

    @Mock
    private CandidaturaRepository repositorio;

    private CandidaturaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new CandidaturaService(repositorio);
    }

    private Candidatura candidatura(int id, String empresa, Estado estado) {
        return new Candidatura(id, "Dev " + id, empresa, estado, LocalDate.of(2026, 10, 1), "", "");
    }

    private List<Candidatura> datosBase() {
        return new ArrayList<>(List.of(
                candidatura(1, "Indra", Estado.PENDIENTE),
                candidatura(2, "Accenture", Estado.ENTREVISTA),
                candidatura(3, "Indra", Estado.ENTREVISTA)));
    }

    @Test
    void listarSinFiltrosDevuelveTodasOrdenadasPorId() {
        when(repositorio.findAll()).thenReturn(new ArrayList<>(List.of(
                candidatura(3, "Indra", Estado.PENDIENTE), candidatura(1, "Indra", Estado.PENDIENTE))));

        List<Candidatura> resultado = servicio.listar(null, null);

        assertEquals(List.of(1, 3), resultado.stream().map(Candidatura::id).toList());
    }

    @Test
    void listarFiltraPorEstadoYEmpresaCombinados() {
        when(repositorio.findAll()).thenReturn(datosBase());

        List<Candidatura> resultado = servicio.listar("ENTREVISTA", "indra");

        assertEquals(1, resultado.size());
        assertEquals(3, resultado.get(0).id());
    }

    @Test
    void listarConEstadoInvalidoLanzaExcepcion() {
        assertThrows(TrackerException.class, () -> servicio.listar("raro", null));
    }

    @Test
    void anadirAsignaSiguienteIdYGuarda() {
        when(repositorio.findAll()).thenReturn(datosBase());

        Candidatura creada = servicio.anadir("QA", "Minsait", "pendiente", "05/10/2026", null, null);

        assertEquals(4, creada.id());
        assertEquals(LocalDate.of(2026, 10, 5), creada.fechaPostulacion());
        ArgumentCaptor<List<Candidatura>> captor = ArgumentCaptor.forClass(List.class);
        verify(repositorio).saveAll(captor.capture());
        assertEquals(4, captor.getValue().size());
    }

    @Test
    void anadirSinTituloLanzaExcepcionYNoGuarda() {
        assertThrows(TrackerException.class, () -> servicio.anadir(" ", "Indra", "pendiente", null, null, null));
        verify(repositorio, never()).saveAll(anyList());
    }

    @Test
    void anadirConFechaInvalidaLanzaExcepcion() {
        when(repositorio.findAll()).thenReturn(datosBase());
        assertThrows(TrackerException.class,
                () -> servicio.anadir("QA", "Minsait", "pendiente", "2026-10-05", null, null));
    }

    @Test
    void verDevuelveLaCandidaturaIndicada() {
        when(repositorio.findAll()).thenReturn(datosBase());
        assertEquals("Accenture", servicio.ver(2).empresa());
    }

    @Test
    void verConIdInexistenteLanzaExcepcion() {
        when(repositorio.findAll()).thenReturn(datosBase());
        assertThrows(TrackerException.class, () -> servicio.ver(99));
    }

    @Test
    void borrarEliminaLaCandidaturaYGuarda() {
        when(repositorio.findAll()).thenReturn(datosBase());

        servicio.borrar(2);

        ArgumentCaptor<List<Candidatura>> captor = ArgumentCaptor.forClass(List.class);
        verify(repositorio).saveAll(captor.capture());
        assertTrue(captor.getValue().stream().noneMatch(c -> c.id() == 2));
    }

    @Test
    void borrarConIdInexistenteNoGuarda() {
        when(repositorio.findAll()).thenReturn(datosBase());
        assertThrows(TrackerException.class, () -> servicio.borrar(99));
        verify(repositorio, never()).saveAll(anyList());
    }

    @Test
    void importarReasignaIdsDuplicados(@TempDir Path directorio) {
        FileCandidaturaRepository origen = new FileCandidaturaRepository(directorio.resolve("origen.csv"));
        origen.saveAll(List.of(candidatura(1, "NTT Data", Estado.ACEPTADO), candidatura(10, "NTT Data", Estado.PENDIENTE)));
        when(repositorio.findAll()).thenReturn(datosBase());

        int importadas = servicio.importar(origen);

        assertEquals(2, importadas);
        ArgumentCaptor<List<Candidatura>> captor = ArgumentCaptor.forClass(List.class);
        verify(repositorio).saveAll(captor.capture());
        List<Integer> ids = captor.getValue().stream().map(Candidatura::id).toList();
        assertEquals(5, ids.size());
        assertEquals(5, ids.stream().distinct().count());
        assertTrue(ids.contains(10));
    }

    @Test
    void listarOrdenaPorFechaMasRecientePrimero() {
        when(repositorio.findAll()).thenReturn(new ArrayList<>(List.of(
                new Candidatura(1, "A", "Indra", Estado.PENDIENTE, LocalDate.of(2026, 9, 1), "", ""),
                new Candidatura(2, "B", "Indra", Estado.PENDIENTE, LocalDate.of(2026, 10, 1), "", ""))));

        List<Candidatura> resultado = servicio.listar(null, null, "fecha");

        assertEquals(List.of(2, 1), resultado.stream().map(Candidatura::id).toList());
    }

    @Test
    void listarConCriterioDeOrdenInvalidoLanzaExcepcion() {
        assertThrows(TrackerException.class, () -> servicio.listar(null, null, "color"));
    }
}