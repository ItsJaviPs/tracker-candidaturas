package com.tracker.service;

import com.tracker.exception.TrackerException;
import com.tracker.model.Empresa;
import com.tracker.model.Estado;
import com.tracker.model.Oferta;
import com.tracker.repository.FiltroOfertas;
import com.tracker.repository.OfertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    private static final Empresa INDRA = new Empresa(1, "Indra", "Tecnología", "", LocalDate.of(2026, 9, 1));

    @Mock
    private OfertaRepository repositorio;

    private OfertaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new OfertaService(repositorio);
    }

    private Oferta oferta(String titulo, Empresa empresa, Double salario, String enlace) {
        return new Oferta(0, empresa, titulo, "", Estado.PENDIENTE, LocalDate.of(2026, 10, 1), enlace, salario, "");
    }

    @Test
    void buscarConSalarioMinimoMayorQueElMaximoLanzaExcepcion() {
        FiltroOfertas incoherente = new FiltroOfertas(null, null, null, 40000.0, 20000.0, null, null);

        assertThrows(TrackerException.class, () -> servicio.buscar(incoherente));
        verify(repositorio, never()).buscar(any(FiltroOfertas.class));
    }

    @Test
    void buscarConFechaInicialPosteriorALaFinalLanzaExcepcion() {
        FiltroOfertas incoherente = new FiltroOfertas(null, null, null, null, null,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 1));

        assertThrows(TrackerException.class, () -> servicio.buscar(incoherente));
    }

    @Test
    void buscarDelegaEnElRepositorioConElMismoFiltro() {
        FiltroOfertas filtro = new FiltroOfertas(null, Estado.PENDIENTE, "indra", null, null, null, null);
        when(repositorio.buscar(filtro)).thenReturn(List.of());

        assertEquals(List.of(), servicio.buscar(filtro));
    }

    @Test
    void guardarSinTituloLanzaExcepcionYNoGuarda() {
        assertThrows(TrackerException.class, () -> servicio.guardar(oferta(" ", INDRA, null, "")));
        verify(repositorio, never()).save(any(Oferta.class));
    }

    @Test
    void guardarSinEmpresaSeleccionadaLanzaExcepcion() {
        assertThrows(TrackerException.class, () -> servicio.guardar(oferta("Dev Java", null, null, "")));
    }

    @Test
    void guardarConSalarioNegativoLanzaExcepcion() {
        assertThrows(TrackerException.class, () -> servicio.guardar(oferta("Dev Java", INDRA, -1.0, "")));
    }

    @Test
    void guardarConEnlaceSinProtocoloLanzaExcepcion() {
        assertThrows(TrackerException.class, () -> servicio.guardar(oferta("Dev Java", INDRA, null, "indra.es")));
    }

    @Test
    void guardarUnaOfertaValidaLaEnviaAlRepositorio() {
        Oferta valida = oferta(" Dev Java ", INDRA, 28000.0, "https://indra.es/oferta");
        when(repositorio.save(any(Oferta.class))).thenReturn(valida);

        servicio.guardar(valida);

        ArgumentCaptor<Oferta> captor = ArgumentCaptor.forClass(Oferta.class);
        verify(repositorio).save(captor.capture());
        assertEquals("Dev Java", captor.getValue().titulo());
    }

    @Test
    void cambiarEstadoDelegaEnElRepositorio() {
        servicio.cambiarEstado(3, Estado.ACEPTADO);

        verify(repositorio).cambiarEstado(3, Estado.ACEPTADO);
    }

    @Test
    void verUnaOfertaInexistenteLanzaExcepcion() {
        when(repositorio.findById(99)).thenReturn(Optional.empty());

        assertThrows(TrackerException.class, () -> servicio.ver(99));
    }

    @Test
    void importarUnFicheroInexistenteLanzaExcepcion(@TempDir Path directorio) {
        assertThrows(TrackerException.class, () -> servicio.importarCsv(directorio.resolve("nada.csv")));
        verify(repositorio, never()).importar(anyList());
    }

    @Test
    void importarCsvConvierteLasCandidaturasEnOfertasYCuentaLasOmitidas(@TempDir Path directorio)
            throws IOException {
        Path csv = directorio.resolve("datos.csv");
        Files.writeString(csv, "id;titulo;empresa;estado;fechaPostulacion;enlace;notas\n"
                + "1;Dev Java;Indra;pendiente;14/10/2026;https://indra.es;Nota\n"
                + "2;QA;Minsait;entrevista;10/10/2026;;\n");
        when(repositorio.importar(anyList())).thenReturn(1);

        ResultadoImportacion resultado = servicio.importarCsv(csv);

        ArgumentCaptor<List<Oferta>> captor = ArgumentCaptor.forClass(List.class);
        verify(repositorio).importar(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals("Indra", captor.getValue().get(0).empresa().nombre());
        assertEquals(Estado.ENTREVISTA, captor.getValue().get(1).estado());
        assertEquals(2, resultado.leidas());
        assertEquals(1, resultado.importadas());
        assertEquals(1, resultado.omitidas());
    }
}
