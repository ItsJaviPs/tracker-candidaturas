package com.tracker.repository;

import com.tracker.config.ConexionBd;
import com.tracker.exception.TrackerException;
import com.tracker.model.Empresa;
import com.tracker.model.Estado;
import com.tracker.model.Oferta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de ofertas sobre JDBC. Las consultas unen OFERTA con EMPRESA mediante JOIN.
 */
public class JdbcOfertaRepository implements OfertaRepository {

    private final ConexionBd conexion;

    /**
     * @param conexion fábrica de conexiones a la base de datos
     */
    public JdbcOfertaRepository(ConexionBd conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Oferta> buscar(FiltroOfertas filtro) {
        StringBuilder sql = new StringBuilder("SELECT ").append(MapeadorSql.COLUMNAS_OFERTA).append(' ')
                .append(MapeadorSql.DESDE_OFERTA).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        anadirCondiciones(sql, parametros, filtro);
        sql.append(" ORDER BY o.fechaPostulacion DESC, o.id DESC");

        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Oferta> ofertas = new ArrayList<>();
                while (rs.next()) {
                    ofertas.add(MapeadorSql.oferta(rs));
                }
                return ofertas;
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudieron consultar las ofertas: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Oferta> findById(int id) {
        String sql = "SELECT " + MapeadorSql.COLUMNAS_OFERTA + " " + MapeadorSql.DESDE_OFERTA + " WHERE o.id = ?";
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(MapeadorSql.oferta(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo consultar la oferta: " + e.getMessage(), e);
        }
    }

    @Override
    public Oferta save(Oferta oferta) {
        try (Connection c = conexion.abrir()) {
            if (oferta.id() == 0) {
                return MapeadorSql.conId(oferta, insertar(c, oferta, oferta.empresa().id()));
            }
            actualizar(c, oferta);
            return oferta;
        } catch (SQLException e) {
            if (MapeadorSql.violaIntegridad(e)) {
                throw new TrackerException("La empresa seleccionada no existe o los datos no son válidos.", e);
            }
            throw new TrackerException("No se pudo guardar la oferta: " + e.getMessage(), e);
        }
    }

    @Override
    public void cambiarEstado(int id, Estado estado) {
        String sql = "UPDATE OFERTA SET estado = ? WHERE id = ?";
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, estado.etiqueta());
            ps.setInt(2, id);
            if (ps.executeUpdate() == 0) {
                throw new TrackerException("No existe ninguna oferta con id " + id);
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo cambiar el estado: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM OFERTA WHERE id = ?";
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new TrackerException("No existe ninguna oferta con id " + id);
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo eliminar la oferta: " + e.getMessage(), e);
        }
    }

    @Override
    public int importar(List<Oferta> ofertas) {
        try (Connection c = conexion.abrir()) {
            c.setAutoCommit(false);
            try {
                Map<String, Integer> idsEmpresas = new HashMap<>();
                int nuevas = 0;
                for (Oferta oferta : ofertas) {
                    int idEmpresa = idDeEmpresa(c, idsEmpresas, oferta.empresa());
                    if (!existe(c, idEmpresa, oferta)) {
                        insertar(c, oferta, idEmpresa);
                        nuevas++;
                    }
                }
                c.commit();
                return nuevas;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new TrackerException("La importación se canceló y no se guardó nada: " + e.getMessage(), e);
        }
    }

    private void anadirCondiciones(StringBuilder sql, List<Object> parametros, FiltroOfertas filtro) {
        if (hayTexto(filtro.texto())) {
            String patron = patron(filtro.texto());
            sql.append(" AND (LOWER(o.titulo) LIKE ? OR LOWER(e.nombre) LIKE ?)");
            parametros.add(patron);
            parametros.add(patron);
        }
        if (filtro.estado() != null) {
            sql.append(" AND o.estado = ?");
            parametros.add(filtro.estado().etiqueta());
        }
        if (hayTexto(filtro.empresa())) {
            sql.append(" AND LOWER(e.nombre) LIKE ?");
            parametros.add(patron(filtro.empresa()));
        }
        if (filtro.salarioMin() != null) {
            sql.append(" AND o.salario >= ?");
            parametros.add(BigDecimal.valueOf(filtro.salarioMin()));
        }
        if (filtro.salarioMax() != null) {
            sql.append(" AND o.salario <= ?");
            parametros.add(BigDecimal.valueOf(filtro.salarioMax()));
        }
        if (filtro.desde() != null) {
            sql.append(" AND o.fechaPostulacion >= ?");
            parametros.add(filtro.desde());
        }
        if (filtro.hasta() != null) {
            sql.append(" AND o.fechaPostulacion <= ?");
            parametros.add(filtro.hasta());
        }
    }

    private boolean hayTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    private String patron(String texto) {
        return "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private int insertar(Connection c, Oferta oferta, int idEmpresa) throws SQLException {
        String sql = "INSERT INTO OFERTA (id_empresa, titulo, descripcion, estado, fechaPostulacion, enlace, "
                + "salario, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            rellenar(ps, oferta, idEmpresa);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    private void actualizar(Connection c, Oferta oferta) throws SQLException {
        String sql = "UPDATE OFERTA SET id_empresa = ?, titulo = ?, descripcion = ?, estado = ?, "
                + "fechaPostulacion = ?, enlace = ?, salario = ?, notas = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            rellenar(ps, oferta, oferta.empresa().id());
            ps.setInt(9, oferta.id());
            if (ps.executeUpdate() == 0) {
                throw new TrackerException("No existe ninguna oferta con id " + oferta.id());
            }
        }
    }

    private void rellenar(PreparedStatement ps, Oferta oferta, int idEmpresa) throws SQLException {
        ps.setInt(1, idEmpresa);
        ps.setString(2, oferta.titulo());
        ps.setString(3, oferta.descripcion());
        ps.setString(4, oferta.estado().etiqueta());
        ps.setObject(5, oferta.fechaPostulacion());
        ps.setString(6, oferta.enlace());
        MapeadorSql.asignarSalario(ps, 7, oferta.salario());
        ps.setString(8, oferta.notas());
    }

    private int idDeEmpresa(Connection c, Map<String, Integer> cache, Empresa empresa) throws SQLException {
        String clave = empresa.nombre().trim().toLowerCase(Locale.ROOT);
        Integer guardado = cache.get(clave);
        if (guardado != null) {
            return guardado;
        }
        int id = buscarIdEmpresa(c, clave);
        if (id == 0) {
            id = insertarEmpresa(c, empresa);
        }
        cache.put(clave, id);
        return id;
    }

    private int buscarIdEmpresa(Connection c, String nombreEnMinusculas) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM EMPRESA WHERE LOWER(nombre) = ?")) {
            ps.setString(1, nombreEnMinusculas);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int insertarEmpresa(Connection c, Empresa empresa) throws SQLException {
        String sql = "INSERT INTO EMPRESA (nombre, sector, web, fechaRegistro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empresa.nombre().trim());
            ps.setString(2, empresa.sector());
            ps.setString(3, empresa.web());
            ps.setObject(4, empresa.fechaRegistro());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    private boolean existe(Connection c, int idEmpresa, Oferta oferta) throws SQLException {
        String sql = "SELECT COUNT(*) FROM OFERTA WHERE id_empresa = ? AND titulo = ? AND fechaPostulacion = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            ps.setString(2, oferta.titulo());
            ps.setObject(3, oferta.fechaPostulacion());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
