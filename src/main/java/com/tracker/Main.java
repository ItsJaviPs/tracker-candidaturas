package com.tracker;

import com.tracker.cli.CommandRunner;
import com.tracker.exception.TrackerException;
import com.tracker.repository.FileCandidaturaRepository;
import com.tracker.service.CandidaturaService;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Punto de entrada de la aplicación de línea de comandos (v1.0).
 */
public final class Main {

    private static final String VARIABLE_FICHERO = "TRACKER_DATA";
    private static final String FICHERO_POR_DEFECTO = "data/candidaturas.txt";

    private Main() {
    }

    public static void main(String[] args) {
        PrintStream salida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        PrintStream errores = new PrintStream(System.err, true, StandardCharsets.UTF_8);

        String ruta = System.getenv(VARIABLE_FICHERO);
        Path fichero = Path.of(ruta == null || ruta.isBlank() ? FICHERO_POR_DEFECTO : ruta);
        CommandRunner runner = new CommandRunner(
                new CandidaturaService(new FileCandidaturaRepository(fichero)), salida);

        try {
            runner.ejecutar(args);
        } catch (TrackerException e) {
            errores.println("Error: " + e.getMessage());
            errores.println("Usa 'ayuda' para ver las operaciones disponibles.");
            System.exit(1);
        }
    }
}
