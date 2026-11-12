package com.tracker.config;

import com.tracker.exception.TrackerException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Datos de conexión a la base de datos.
 *
 * <p>Se leen del fichero {@code config.properties} y pueden sobrescribirse con las variables de entorno
 * {@code DB_URL}, {@code DB_USER} y {@code DB_PASSWORD}.
 *
 * @param url      URL JDBC de la base de datos
 * @param usuario  usuario de la base de datos
 * @param password contraseña del usuario
 */
public record DbConfig(String url, String usuario, String password) {

    private static final Path FICHERO_POR_DEFECTO = Path.of("config.properties");

    /**
     * Carga la configuración desde {@code config.properties} (en el directorio de trabajo) y las variables
     * de entorno.
     *
     * @return configuración de conexión
     * @throws TrackerException si falta la URL de conexión o no se puede leer el fichero
     */
    public static DbConfig cargar() {
        return cargar(FICHERO_POR_DEFECTO, System.getenv());
    }

    /**
     * Carga la configuración desde un fichero y un conjunto de variables de entorno. Las variables de
     * entorno tienen prioridad sobre el fichero.
     *
     * @param fichero ruta del fichero de propiedades (puede no existir)
     * @param entorno variables de entorno
     * @return configuración de conexión
     * @throws TrackerException si falta la URL de conexión o no se puede leer el fichero
     */
    public static DbConfig cargar(Path fichero, Map<String, String> entorno) {
        Properties propiedades = new Properties();
        if (Files.isRegularFile(fichero)) {
            try (Reader lector = Files.newBufferedReader(fichero, StandardCharsets.UTF_8)) {
                propiedades.load(lector);
            } catch (IOException e) {
                throw new TrackerException("No se pudo leer '" + fichero + "': " + e.getMessage(), e);
            }
        }

        String url = valor(entorno.get("DB_URL"), propiedades.getProperty("db.url"));
        String usuario = valor(entorno.get("DB_USER"), propiedades.getProperty("db.user"));
        String password = valor(entorno.get("DB_PASSWORD"), propiedades.getProperty("db.password"));

        if (url == null) {
            throw new TrackerException("No hay configuración de base de datos. Copia config.properties.example "
                    + "como config.properties y rellénalo, o define la variable de entorno DB_URL.");
        }
        return new DbConfig(url, usuario == null ? "" : usuario, password == null ? "" : password);
    }

    /**
     * @return descripción de la configuración sin mostrar la contraseña
     */
    @Override
    public String toString() {
        return "DbConfig[url=" + url + ", usuario=" + usuario + ", password=****]";
    }

    private static String valor(String prioritario, String alternativo) {
        if (prioritario != null && !prioritario.isBlank()) {
            return prioritario.trim();
        }
        return alternativo == null || alternativo.isBlank() ? null : alternativo.trim();
    }
}
