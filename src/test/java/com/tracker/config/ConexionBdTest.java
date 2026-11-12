package com.tracker.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Prueba de integración con la base de datos real. Se omite si no hay configuración local.
 */
class ConexionBdTest {

    @Test
    void abreConexionConLaConfiguracionLocal() throws SQLException {
        assumeTrue(Files.exists(Path.of("config.properties")) || System.getenv("DB_URL") != null,
                "Sin configuración de base de datos: se omite la prueba con MySQL real");

        try (Connection conexion = new ConexionBd(DbConfig.cargar()).abrir()) {
            assertTrue(conexion.isValid(2));
        }
    }
}
