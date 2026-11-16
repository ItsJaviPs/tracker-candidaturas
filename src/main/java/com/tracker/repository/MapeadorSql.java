package com.tracker.repository;

import com.tracker.model.Empresa;
import com.tracker.model.Entrevista;
import com.tracker.model.Estado;
import com.tracker.model.Oferta;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

/**
 * Utilidades compartidas por los repositorios JDBC: columnas de las consultas con JOIN y conversión de filas.
 */
final class MapeadorSql {

    static final String COLUMNAS_OFERTA = "o.id AS oferta_id, o.id_empresa, o.titulo, o.descripcion, o.estado, "
            + "o.fechaPostulacion, o.enlace, o.salario, o.notas AS oferta_notas, "
            + "e.nombre AS empresa_nombre, e.sector AS empresa_sector, e.web AS empresa_web, "
            + "e.fechaRegistro AS empresa_fecha";

    static final String DESDE_OFERTA = "FROM OFERTA o JOIN EMPRESA e ON o.id_empresa = e.id";

    static final String COLUMNAS_ENTREVISTA = "n.id AS entrevista_id, n.fecha AS entrevista_fecha, n.tipo, "
            + "n.notas AS entrevista_notas, n.resultado, " + COLUMNAS_OFERTA;

    static final String DESDE_ENTREVISTA = "FROM ENTREVISTA n JOIN OFERTA o ON n.id_oferta = o.id "
            + "JOIN EMPRESA e ON o.id_empresa = e.id";

    private MapeadorSql() {
    }

    static Empresa empresaDeOferta(ResultSet rs) throws SQLException {
        return new Empresa(rs.getInt("id_empresa"), rs.getString("empresa_nombre"), rs.getString("empresa_sector"),
                texto(rs.getString("empresa_web")), rs.getObject("empresa_fecha", LocalDate.class));
    }

    static Oferta oferta(ResultSet rs) throws SQLException {
        return new Oferta(rs.getInt("oferta_id"), empresaDeOferta(rs), rs.getString("titulo"),
                texto(rs.getString("descripcion")), Estado.desdeTexto(rs.getString("estado")),
                rs.getObject("fechaPostulacion", LocalDate.class), texto(rs.getString("enlace")),
                salario(rs), texto(rs.getString("oferta_notas")));
    }

    static Entrevista entrevista(ResultSet rs) throws SQLException {
        return new Entrevista(rs.getInt("entrevista_id"), oferta(rs), rs.getObject("entrevista_fecha", LocalDate.class),
                rs.getString("tipo"), texto(rs.getString("entrevista_notas")), rs.getString("resultado"));
    }

    static Double salario(ResultSet rs) throws SQLException {
        BigDecimal valor = rs.getBigDecimal("salario");
        return valor == null ? null : valor.doubleValue();
    }

    static void asignarSalario(PreparedStatement ps, int posicion, Double salario) throws SQLException {
        if (salario == null) {
            ps.setNull(posicion, Types.DECIMAL);
        } else {
            ps.setBigDecimal(posicion, BigDecimal.valueOf(salario));
        }
    }

    static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    /** Un SQLState que empieza por 23 indica una restricción de integridad (clave foránea, unicidad, etc.). */
    static boolean violaIntegridad(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }

    static Oferta conId(Oferta oferta, int id) {
        return new Oferta(id, oferta.empresa(), oferta.titulo(), oferta.descripcion(), oferta.estado(),
                oferta.fechaPostulacion(), oferta.enlace(), oferta.salario(), oferta.notas());
    }
}
