package com.tracker.repository;

import com.tracker.config.ConexionBd;
import com.tracker.exception.TrackerException;
import com.tracker.model.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de empresas sobre JDBC.
 */
public class JdbcEmpresaRepository implements EmpresaRepository {

    private static final String COLUMNAS = "id, nombre, sector, web, fechaRegistro";

    private final ConexionBd conexion;

    /**
     * @param conexion fábrica de conexiones a la base de datos
     */
    public JdbcEmpresaRepository(ConexionBd conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Empresa> findAll() {
        String sql = "SELECT " + COLUMNAS + " FROM EMPRESA ORDER BY nombre";
        try (Connection c = conexion.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Empresa> empresas = new ArrayList<>();
            while (rs.next()) {
                empresas.add(mapear(rs));
            }
            return empresas;
        } catch (SQLException e) {
            throw new TrackerException("No se pudieron consultar las empresas: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Empresa> findById(int id) {
        String sql = "SELECT " + COLUMNAS + " FROM EMPRESA WHERE id = ?";
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo consultar la empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public Empresa save(Empresa empresa) {
        try (Connection c = conexion.abrir()) {
            if (empresa.id() == 0) {
                return insertar(c, empresa);
            }
            actualizar(c, empresa);
            return empresa;
        } catch (SQLException e) {
            if (MapeadorSql.violaIntegridad(e)) {
                throw new TrackerException("Ya existe una empresa llamada '" + empresa.nombre() + "'.", e);
            }
            throw new TrackerException("No se pudo guardar la empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM EMPRESA WHERE id = ?";
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new TrackerException("No existe ninguna empresa con id " + id);
            }
        } catch (SQLException e) {
            if (MapeadorSql.violaIntegridad(e)) {
                throw new TrackerException("No se puede eliminar la empresa porque tiene ofertas asociadas.", e);
            }
            throw new TrackerException("No se pudo eliminar la empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<Integer, Integer> contarOfertasPorEmpresa() {
        String sql = "SELECT id_empresa, COUNT(*) AS total FROM OFERTA GROUP BY id_empresa";
        try (Connection c = conexion.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Map<Integer, Integer> totales = new HashMap<>();
            while (rs.next()) {
                totales.put(rs.getInt("id_empresa"), rs.getInt("total"));
            }
            return totales;
        } catch (SQLException e) {
            throw new TrackerException("No se pudo contar las ofertas por empresa: " + e.getMessage(), e);
        }
    }

    private Empresa insertar(Connection c, Empresa empresa) throws SQLException {
        String sql = "INSERT INTO EMPRESA (nombre, sector, web, fechaRegistro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empresa.nombre());
            ps.setString(2, empresa.sector());
            ps.setString(3, empresa.web());
            ps.setObject(4, empresa.fechaRegistro());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return new Empresa(claves.getInt(1), empresa.nombre(), empresa.sector(), empresa.web(),
                        empresa.fechaRegistro());
            }
        }
    }

    private void actualizar(Connection c, Empresa empresa) throws SQLException {
        String sql = "UPDATE EMPRESA SET nombre = ?, sector = ?, web = ?, fechaRegistro = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, empresa.nombre());
            ps.setString(2, empresa.sector());
            ps.setString(3, empresa.web());
            ps.setObject(4, empresa.fechaRegistro());
            ps.setInt(5, empresa.id());
            if (ps.executeUpdate() == 0) {
                throw new TrackerException("No existe ninguna empresa con id " + empresa.id());
            }
        }
    }

    private Empresa mapear(ResultSet rs) throws SQLException {
        return new Empresa(rs.getInt("id"), rs.getString("nombre"), rs.getString("sector"),
                MapeadorSql.texto(rs.getString("web")), rs.getObject("fechaRegistro", java.time.LocalDate.class));
    }
}
