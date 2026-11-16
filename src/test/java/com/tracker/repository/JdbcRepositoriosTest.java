package com.tracker.repository;

import com.tracker.config.ConexionBd;
import com.tracker.config.DbConfig;
import com.tracker.exception.TrackerException;
import com.tracker.model.Empresa;
import com.tracker.model.Entrevista;
import com.tracker.model.Estado;
import com.tracker.model.Oferta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integración de los repositorios JDBC sobre H2 en memoria (modo MySQL).
 */
class JdbcRepositoriosTest {

    private ConexionBd conexion;
    private JdbcEmpresaRepository empresas;
    private JdbcOfertaRepository ofertas;
    private JdbcEntrevistaRepository entrevistas;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException, IOException {
        conexion = new ConexionBd(new DbConfig("jdbc:h2:mem:canditrack;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        try (Connection c = conexion.abrir(); Statement st = c.createStatement()) {
            st.execute("DROP ALL OBJECTS");
            try (InputStream in = getClass().getResourceAsStream("/schema-h2.sql")) {
                String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                for (String sentencia : script.split(";")) {
                    if (!sentencia.isBlank()) {
                        st.execute(sentencia);
                    }
                }
            }
        }
        empresas = new JdbcEmpresaRepository(conexion);
        ofertas = new JdbcOfertaRepository(conexion);
        entrevistas = new JdbcEntrevistaRepository(conexion);
    }

    private Empresa crearEmpresa(String nombre) {
        return empresas.save(new Empresa(0, nombre, "Tecnología", "https://" + nombre + ".com",
                LocalDate.of(2026, 9, 1)));
    }

    private Oferta crearOferta(Empresa empresa, String titulo, Estado estado, LocalDate fecha, Double salario) {
        return ofertas.save(new Oferta(0, empresa, titulo, "", estado, fecha, "", salario, ""));
    }

    @Test
    void guardaYRecuperaUnaEmpresa() {
        Empresa guardada = crearEmpresa("Indra");

        assertTrue(guardada.id() > 0);
        assertEquals("Indra", empresas.findById(guardada.id()).orElseThrow().nombre());
    }

    @Test
    void actualizaUnaEmpresaExistente() {
        Empresa guardada = crearEmpresa("Indra");

        empresas.save(new Empresa(guardada.id(), "Indra Sistemas", "Consultoría", "", guardada.fechaRegistro()));

        assertEquals("Indra Sistemas", empresas.findById(guardada.id()).orElseThrow().nombre());
    }

    @Test
    void noPermiteDosEmpresasConElMismoNombre() {
        crearEmpresa("Indra");

        assertThrows(TrackerException.class, () -> crearEmpresa("Indra"));
    }

    @Test
    void noPermiteEliminarUnaEmpresaConOfertas() {
        Empresa indra = crearEmpresa("Indra");
        crearOferta(indra, "Dev Java", Estado.PENDIENTE, LocalDate.of(2026, 10, 1), null);

        assertThrows(TrackerException.class, () -> empresas.delete(indra.id()));
    }

    @Test
    void eliminaUnaEmpresaSinOfertas() {
        Empresa indra = crearEmpresa("Indra");

        empresas.delete(indra.id());

        assertTrue(empresas.findById(indra.id()).isEmpty());
    }

    @Test
    void cuentaLasOfertasDeCadaEmpresa() {
        Empresa indra = crearEmpresa("Indra");
        crearOferta(indra, "Dev Java", Estado.PENDIENTE, LocalDate.of(2026, 10, 1), null);
        crearOferta(indra, "QA", Estado.PENDIENTE, LocalDate.of(2026, 10, 2), null);

        Map<Integer, Integer> totales = empresas.contarOfertasPorEmpresa();

        assertEquals(2, totales.get(indra.id()).intValue());
    }

    @Test
    void recuperaUnaOfertaConSuEmpresaMedianteJoin() {
        Empresa indra = crearEmpresa("Indra");
        Oferta guardada = crearOferta(indra, "Dev Java", Estado.ENTREVISTA, LocalDate.of(2026, 10, 1), 28000.0);

        Oferta leida = ofertas.findById(guardada.id()).orElseThrow();

        assertEquals("Indra", leida.empresa().nombre());
        assertEquals(Estado.ENTREVISTA, leida.estado());
        assertEquals(28000.0, leida.salario().doubleValue());
    }

    @Test
    void guardaUnaOfertaSinSalario() {
        Oferta guardada = crearOferta(crearEmpresa("Indra"), "Dev Java", Estado.PENDIENTE,
                LocalDate.of(2026, 10, 1), null);

        assertNull(ofertas.findById(guardada.id()).orElseThrow().salario());
    }

    @Test
    void filtraPorVariosCriteriosCombinados() {
        Empresa indra = crearEmpresa("Indra");
        Empresa accenture = crearEmpresa("Accenture");
        crearOferta(indra, "Dev Java", Estado.ENTREVISTA, LocalDate.of(2026, 10, 1), 30000.0);
        crearOferta(indra, "QA", Estado.PENDIENTE, LocalDate.of(2026, 10, 5), 20000.0);
        crearOferta(accenture, "Backend", Estado.ENTREVISTA, LocalDate.of(2026, 9, 1), 35000.0);

        List<Oferta> porEstadoYEmpresa = ofertas.buscar(new FiltroOfertas(null, Estado.ENTREVISTA, "indra",
                null, null, null, null));
        List<Oferta> porSalario = ofertas.buscar(new FiltroOfertas(null, null, null, 25000.0, 40000.0, null, null));
        List<Oferta> porFechaYEmpresa = ofertas.buscar(new FiltroOfertas(null, null, "indra", null, null,
                LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 31)));
        List<Oferta> porTexto = ofertas.buscar(new FiltroOfertas("back", null, null, null, null, null, null));

        assertEquals(List.of("Dev Java"), porEstadoYEmpresa.stream().map(Oferta::titulo).toList());
        assertEquals(2, porSalario.size());
        assertEquals(List.of("QA"), porFechaYEmpresa.stream().map(Oferta::titulo).toList());
        assertEquals(List.of("Backend"), porTexto.stream().map(Oferta::titulo).toList());
    }

    @Test
    void sinFiltrosDevuelveLasMasRecientesPrimero() {
        Empresa indra = crearEmpresa("Indra");
        crearOferta(indra, "Antigua", Estado.PENDIENTE, LocalDate.of(2026, 8, 1), null);
        crearOferta(indra, "Reciente", Estado.PENDIENTE, LocalDate.of(2026, 10, 1), null);

        List<Oferta> resultado = ofertas.buscar(FiltroOfertas.vacio());

        assertEquals("Reciente", resultado.get(0).titulo());
    }

    @Test
    void cambiaElEstadoDeUnaOferta() {
        Oferta guardada = crearOferta(crearEmpresa("Indra"), "Dev Java", Estado.PENDIENTE,
                LocalDate.of(2026, 10, 1), null);

        ofertas.cambiarEstado(guardada.id(), Estado.ACEPTADO);

        assertEquals(Estado.ACEPTADO, ofertas.findById(guardada.id()).orElseThrow().estado());
    }

    @Test
    void cambiarElEstadoDeUnaOfertaInexistenteLanzaExcepcion() {
        assertThrows(TrackerException.class, () -> ofertas.cambiarEstado(999, Estado.ACEPTADO));
    }

    @Test
    void alEliminarUnaOfertaSeEliminanSusEntrevistas() {
        Oferta oferta = crearOferta(crearEmpresa("Indra"), "Dev Java", Estado.ENTREVISTA,
                LocalDate.of(2026, 10, 1), null);
        entrevistas.save(new Entrevista(0, oferta, LocalDate.of(2026, 10, 10), "técnica", "", "pendiente"));

        ofertas.delete(oferta.id());

        assertTrue(entrevistas.findAll().isEmpty());
    }

    @Test
    void guardaYConsultaEntrevistasPorOferta() {
        Oferta oferta = crearOferta(crearEmpresa("Indra"), "Dev Java", Estado.ENTREVISTA,
                LocalDate.of(2026, 10, 1), null);
        Oferta otra = crearOferta(crearEmpresa("Accenture"), "QA", Estado.ENTREVISTA,
                LocalDate.of(2026, 10, 2), null);
        entrevistas.save(new Entrevista(0, oferta, LocalDate.of(2026, 10, 10), "técnica", "Spring", "pendiente"));
        entrevistas.save(new Entrevista(0, otra, LocalDate.of(2026, 10, 11), "online", "", "positivo"));

        List<Entrevista> deLaOferta = entrevistas.findByOferta(oferta.id());

        assertEquals(1, deLaOferta.size());
        assertEquals("técnica", deLaOferta.get(0).tipo());
        assertEquals("Indra", deLaOferta.get(0).oferta().empresa().nombre());
        assertEquals(2, entrevistas.findAll().size());
    }

    @Test
    void actualizaYEliminaUnaEntrevista() {
        Oferta oferta = crearOferta(crearEmpresa("Indra"), "Dev Java", Estado.ENTREVISTA,
                LocalDate.of(2026, 10, 1), null);
        Entrevista guardada = entrevistas.save(
                new Entrevista(0, oferta, LocalDate.of(2026, 10, 10), "técnica", "", "pendiente"));

        entrevistas.save(new Entrevista(guardada.id(), oferta, guardada.fecha(), "técnica", "", "positivo"));
        assertEquals("positivo", entrevistas.findById(guardada.id()).orElseThrow().resultado());

        entrevistas.delete(guardada.id());
        assertTrue(entrevistas.findById(guardada.id()).isEmpty());
    }

    @Test
    void importaCreandoLasEmpresasQueFaltan() {
        List<Oferta> lote = List.of(ofertaParaImportar("Indra", "Dev Java"),
                ofertaParaImportar("Indra", "QA"), ofertaParaImportar("Minsait", "APIs"));

        int importadas = ofertas.importar(lote);

        assertEquals(3, importadas);
        assertEquals(2, empresas.findAll().size());
    }

    @Test
    void importarDosVecesNoDuplicaLasOfertas() {
        List<Oferta> lote = List.of(ofertaParaImportar("Indra", "Dev Java"), ofertaParaImportar("Minsait", "APIs"));
        ofertas.importar(lote);

        int segundaVez = ofertas.importar(lote);

        assertEquals(0, segundaVez);
        assertEquals(2, ofertas.buscar(FiltroOfertas.vacio()).size());
    }

    @Test
    void siFallaLaImportacionNoSeGuardaNada() {
        List<Oferta> lote = List.of(ofertaParaImportar("Indra", "Dev Java"), ofertaParaImportar("Minsait", null));

        assertThrows(TrackerException.class, () -> ofertas.importar(lote));

        assertTrue(empresas.findAll().isEmpty());
        assertFalse(ofertas.buscar(FiltroOfertas.vacio()).iterator().hasNext());
    }

    private Oferta ofertaParaImportar(String empresa, String titulo) {
        Empresa e = new Empresa(0, empresa, "Sin clasificar", "", LocalDate.of(2026, 10, 20));
        return new Oferta(0, e, titulo, "", Estado.PENDIENTE, LocalDate.of(2026, 10, 1), "", null, "");
    }
}
