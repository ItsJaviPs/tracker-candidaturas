package com.tracker.config;

import com.tracker.exception.TrackerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbConfigTest {

    private static final String CONTENIDO = "db.url=jdbc:mysql://localhost:3306/canditrack\n"
            + "db.user=root\ndb.password=secreta\n";

    @Test
    void leeLaConfiguracionDelFichero(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("config.properties");
        Files.writeString(fichero, CONTENIDO);

        DbConfig config = DbConfig.cargar(fichero, Map.of());

        assertEquals("jdbc:mysql://localhost:3306/canditrack", config.url());
        assertEquals("root", config.usuario());
        assertEquals("secreta", config.password());
    }

    @Test
    void lasVariablesDeEntornoTienenPrioridadSobreElFichero(@TempDir Path directorio) throws IOException {
        Path fichero = directorio.resolve("config.properties");
        Files.writeString(fichero, CONTENIDO);

        DbConfig config = DbConfig.cargar(fichero, Map.of("DB_PASSWORD", "otra"));

        assertEquals("otra", config.password());
        assertEquals("root", config.usuario());
    }

    @Test
    void funcionaSoloConVariablesDeEntorno(@TempDir Path directorio) {
        DbConfig config = DbConfig.cargar(directorio.resolve("no-existe.properties"),
                Map.of("DB_URL", "jdbc:h2:mem:prueba", "DB_USER", "sa"));

        assertEquals("jdbc:h2:mem:prueba", config.url());
        assertEquals("", config.password());
    }

    @Test
    void sinUrlLanzaUnErrorClaro(@TempDir Path directorio) {
        TrackerException error = assertThrows(TrackerException.class,
                () -> DbConfig.cargar(directorio.resolve("no-existe.properties"), Map.of()));

        assertTrue(error.getMessage().contains("config.properties"));
    }
}
