package com.tracker.service;

import com.tracker.exception.TrackerException;
import com.tracker.model.Empresa;
import com.tracker.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository repositorio;

    private EmpresaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new EmpresaService(repositorio);
    }

    @Test
    void guardarSinNombreLanzaExcepcionYNoGuarda() {
        Empresa sinNombre = new Empresa(0, "  ", "Tecnología", "", null);

        assertThrows(TrackerException.class, () -> servicio.guardar(sinNombre));
        verify(repositorio, never()).save(any(Empresa.class));
    }

    @Test
    void guardarSinSectorLanzaExcepcion() {
        Empresa sinSector = new Empresa(0, "Indra", "", "", null);

        assertThrows(TrackerException.class, () -> servicio.guardar(sinSector));
    }

    @Test
    void guardarConWebSinProtocoloLanzaExcepcion() {
        Empresa webInvalida = new Empresa(0, "Indra", "Tecnología", "www.indra.es", null);

        assertThrows(TrackerException.class, () -> servicio.guardar(webInvalida));
    }

    @Test
    void guardarLimpiaEspaciosYAsignaLaFechaDeHoy() {
        Empresa guardada = new Empresa(1, "Indra", "Tecnología", "", LocalDate.now());
        when(repositorio.save(any(Empresa.class))).thenReturn(guardada);

        servicio.guardar(new Empresa(0, "  Indra ", " Tecnología ", null, null));

        ArgumentCaptor<Empresa> captor = ArgumentCaptor.forClass(Empresa.class);
        verify(repositorio).save(captor.capture());
        assertEquals("Indra", captor.getValue().nombre());
        assertEquals("Tecnología", captor.getValue().sector());
        assertEquals(LocalDate.now(), captor.getValue().fechaRegistro());
    }

    @Test
    void verUnaEmpresaInexistenteLanzaExcepcion() {
        when(repositorio.findById(99)).thenReturn(Optional.empty());

        assertThrows(TrackerException.class, () -> servicio.ver(99));
    }

    @Test
    void eliminarDelegaEnElRepositorio() {
        servicio.eliminar(5);

        verify(repositorio).delete(5);
    }
}
