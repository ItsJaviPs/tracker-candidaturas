package com.tracker.repository;

import com.tracker.exception.TrackerException;
import com.tracker.model.Candidatura;
import com.tracker.model.Estado;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio basado en un fichero de texto con un registro por línea y campos separados por punto y coma.
 */
public class FileCandidaturaRepository implements CandidaturaRepository {

    public static final String SEPARADOR = ";";
    public static final String CABECERA = "id;titulo;empresa;estado;fechaPostulacion;enlace;notas";
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final int NUM_CAMPOS = 7;

    private final Path fichero;

    /**
     * @param fichero ruta del fichero de datos; se crea al guardar si no existe
     */
    public FileCandidaturaRepository(Path fichero) {
        this.fichero = fichero;
    }

    /**
     * Lee todas las candidaturas. Si el fichero no existe devuelve una lista vacía.
     * Las líneas vacías y la cabecera se ignoran.
     *
     * @throws TrackerException si el fichero no se puede leer o una línea es inválida
     */
    @Override
    public List<Candidatura> findAll() {
        List<Candidatura> resultado = new ArrayList<>();
        if (!Files.exists(fichero)) {
            return resultado;
        }
        try (BufferedReader lector = Files.newBufferedReader(fichero, StandardCharsets.UTF_8)) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank() || linea.startsWith("id" + SEPARADOR)) {
                    continue;
                }
                resultado.add(parsearLinea(linea, numeroLinea));
            }
        } catch (IOException e) {
            throw new TrackerException("No se pudo leer el fichero '" + fichero + "': " + e.getMessage(), e);
        }
        return resultado;
    }

    /**
     * Reescribe el fichero completo con la cabecera y las candidaturas indicadas.
     *
     * @throws TrackerException si no se puede escribir en el fichero
     */
    @Override
    public void saveAll(List<Candidatura> candidaturas) {
        try {
            Path directorio = fichero.toAbsolutePath().getParent();
            if (directorio != null) {
                Files.createDirectories(directorio);
            }
            try (BufferedWriter escritor = Files.newBufferedWriter(fichero, StandardCharsets.UTF_8)) {
                escritor.write(CABECERA);
                escritor.newLine();
                for (Candidatura candidatura : candidaturas) {
                    escritor.write(formatearLinea(candidatura));
                    escritor.newLine();
                }
            }
        } catch (IOException e) {
            throw new TrackerException("No se pudo escribir en el fichero '" + fichero + "': " + e.getMessage(), e);
        }
    }

    /** Interpreta un registro de siete campos e identifica la línea si contiene errores. */
    private Candidatura parsearLinea(String linea, int numeroLinea) {
        String[] campos = linea.split(SEPARADOR, -1);
        if (campos.length != NUM_CAMPOS) {
            throw new TrackerException("Línea " + numeroLinea + " incorrecta: se esperaban " + NUM_CAMPOS
                    + " campos y hay " + campos.length);
        }
        try {
            if (campos[1].isBlank()) {
                throw new TrackerException("el título es obligatorio");
            }
            if (campos[2].isBlank()) {
                throw new TrackerException("la empresa es obligatoria");
            }
            return new Candidatura(
                    Integer.parseInt(campos[0].trim()),
                    campos[1].trim(),
                    campos[2].trim(),
                    Estado.desdeTexto(campos[3]),
                    LocalDate.parse(campos[4].trim(), FORMATO_FECHA),
                    campos[5].trim(),
                    campos[6].trim());
        } catch (NumberFormatException e) {
            throw new TrackerException("Línea " + numeroLinea + ": el id '" + campos[0] + "' no es un número");
        } catch (DateTimeParseException e) {
            throw new TrackerException("Línea " + numeroLinea + ": fecha '" + campos[4]
                    + "' no válida (formato dd/MM/yyyy)");
        } catch (TrackerException e) {
            throw new TrackerException("Línea " + numeroLinea + ": " + e.getMessage());
        }
    }

    /** Convierte una candidatura en un registro, limpiando los campos de texto. */
    private String formatearLinea(Candidatura c) {
        return String.join(SEPARADOR,
                String.valueOf(c.id()),
                limpiar(c.titulo()),
                limpiar(c.empresa()),
                c.estado().etiqueta(),
                c.fechaPostulacion().format(FORMATO_FECHA),
                limpiar(c.enlace()),
                limpiar(c.notas()));
    }

    /** Evita que el contenido rompa el formato del fichero. */
    private String limpiar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace(SEPARADOR, ",").replace("\r", " ").replace("\n", " ").trim();
    }
}
