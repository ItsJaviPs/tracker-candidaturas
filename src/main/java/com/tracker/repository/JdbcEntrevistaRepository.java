package com.tracker.repository;

import com.tracker.config.ConexionBd;
import com.tracker.exception.TrackerException;
import com.tracker.model.Entrevista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de entrevistas sobre JDBC. Las consultas unen ENTREVISTA, OFERTA y EMPRESA mediante JOIN.
 */
public class JdbcEntrevistaRepository implements EntrevistaRepository {

    private static final String SELECT = "SELECT " + MapeadorSql.COLUMNAS_ENTREVISTA + " "
            + MapeadorSql.DESDE_ENTREVISTA;
    private static final String ORDEN = " ORDER BY n.fecha DESC, n.id DESC";

    private final ConexionBd conexion;

    /**
     * @param conexion fábrica de conexiones a la base de datos
     */
    public JdbcEntrevistaRepository(ConexionBd conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Entrevista> findAll() {
        return consultar(SELECT + ORDEN, null);
    }

    @Override
    public List<Entrevista> findByOferta(int idOferta) {
        return consultar(SELECT + " WHERE n.id_oferta = ?" + ORDEN, idOferta);
    }

    @Override
    public Optional<Entrevista> findById(int id) {
        return consultar(SELECT + " WHERE n.id = ?", id).stream().findFirst();
    }

    @Override
    public Entrevista save(Entrevista entrevista) {
        try (Connection c = conexion.abrir()) {
            if (entrevista.id() == 0) {
                return insertar(c, entrevista);
            }
            actualizar(c, entrevista);
            return entrevista;
        } catch (SQLException e) {
            if (MapeadorSql.violaIntegridad(e)) {
                throw new TrackerException("La oferta seleccionada no existe o los datos no son válidos.", e);
            }
            throw new TrackerException("No se pudo guardar la entrevista: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM ENTREVISTA WHERE id = ?";
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new TrackerException("No existe ninguna entrevista con id " + id);
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo eliminar la entrevista: " + e.getMessage(), e);
        }
    }

    private List<Entrevista> consultar(String sql, Integer parametro) {
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Entrevista> entrevistas = new ArrayList<>();
                while (rs.next()) {
                    entrevistas.add(MapeadorSql.entrevista(rs));
                }
                return entrevistas;
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudieron consultar las entrevistas: " + e.getMessage(), e);
        }
    }

    private Entrevista insertar(Connection c, Entrevista entrevista) throws SQLException {
        String sql = "INSERT INTO ENTREVISTA (id_oferta, fecha, tipo, notas, resultado) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            rellenar(ps, entrevista);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return new Entrevista(claves.getInt(1), entrevista.oferta(), entrevista.fecha(), entrevista.tipo(),
                        entrevista.notas(), entrevista.resultado());
            }
        }
    }

    private void actualizar(Connection c, Entrevista entrevista) throws SQLException {
        String sql = "UPDATE ENTREVISTA SET id_oferta = ?, fecha = ?, tipo = ?, notas = ?, resultado = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            rellenar(ps, entrevista);
            ps.setInt(6, entrevista.id());
            if (ps.executeUpdate() == 0) {
                throw new TrackerException("No existe ninguna entrevista con id " + entrevista.id());
            }
        }
    }

    private void rellenar(PreparedStatement ps, Entrevista entrevista) throws SQLException {
        ps.setInt(1, entrevista.oferta().id());
        ps.setObject(2, entrevista.fecha());
        ps.setString(3, entrevista.tipo());
        ps.setString(4, entrevista.notas());
        ps.setString(5, entrevista.resultado());
    }
}
