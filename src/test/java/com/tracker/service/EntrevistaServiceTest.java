package com.tracker.service;

import com.tracker.exception.TrackerException;
import com.tracker.model.Empresa;
import com.tracker.model.Entrevista;
import com.tracker.model.Estado;
import com.tracker.model.Oferta;
import com.tracker.repository.EntrevistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntrevistaServiceTest {

    private static final Empresa INDRA = new Empresa(1, "Indra", "Tecnología", "", LocalDate.of(2026, 9, 1));
    private static final Oferta OFERTA = new Oferta(7, INDRA, "Dev Java", "", Estado.ENTREVISTA,
            LocalDate.of(2026, 10, 1), "", null, "");

    @Mock
    private EntrevistaRepository repositorio;

    private EntrevistaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new EntrevistaService(repositorio);
    }

    private Entrevista entrevista(Oferta oferta, LocalDate fecha, String tipo, String resultado) {
        return new Entrevista(0, oferta, fecha, tipo, "", resultado);
    }

    @Test
    void guardarSinOfertaLanzaExcepcion() {
        assertThrows(TrackerException.class,
                () -> servicio.guardar(entrevista(null, LocalDate.of(2026, 10, 10), "técnica", "pendiente")));
        verify(repositorio, never()).save(any(Entrevista.class));
    }

    @Test
    void guardarSinFechaLanzaExcepcion() {
        assertThrows(TrackerException.class, () -> servicio.guardar(entrevista(OFERTA, null, "técnica", "pendiente")));
    }

    @Test
    void guardarConTipoNoPermitidoLanzaExcepcion() {
        assertThrows(TrackerException.class,
                () -> servicio.guardar(entrevista(OFERTA, LocalDate.of(2026, 10, 10), "telepática", "pendiente")));
    }

    @Test
    void guardarConResultadoNoPermitidoLanzaExcepcion() {
        assertThrows(TrackerException.class,
                () -> servicio.guardar(entrevista(OFERTA, LocalDate.of(2026, 10, 10), "técnica", "quizás")));
    }

    @Test
    void guardarUnaEntrevistaValidaLaEnviaAlRepositorio() {
        Entrevista valida = entrevista(OFERTA, LocalDate.of(2026, 10, 10), "técnica", "pendiente");
        when(repositorio.save(valida)).thenReturn(valida);

        servicio.guardar(valida);

        verify(repositorio).save(valida);
    }

    @Test
    void eliminarDelegaEnElRepositorio() {
        servicio.eliminar(4);

        verify(repositorio).delete(4);
    }
}
