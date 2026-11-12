package com.tracker.config;

import com.tracker.exception.TrackerException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexiones JDBC. Cada llamada a {@link #abrir()} devuelve una conexión nueva que quien la
 * recibe debe cerrar (idealmente con try-with-resources).
 */
public class ConexionBd {

    private final DbConfig config;

    /**
     * @param config datos de conexión
     */
    public ConexionBd(DbConfig config) {
        this.config = config;
    }

    /**
     * Abre una conexión nueva con la base de datos.
     *
     * @return conexión abierta, con auto-commit activado
     * @throws TrackerException si no se puede conectar
     */
    public Connection abrir() {
        try {
            return DriverManager.getConnection(config.url(), config.usuario(), config.password());
        } catch (SQLException e) {
            throw new TrackerException("No se pudo conectar con la base de datos: " + e.getMessage(), e);
        }
    }
}
