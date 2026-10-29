package com.tracker.cli;

import com.tracker.exception.TrackerException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Argumentos de línea de comandos interpretados: operación, valor posicional y opciones {@code --clave valor}.
 */
public final class CommandLine {

    private final String operacion;
    private final String valorPosicional;
    private final Map<String, String> opciones;

    /** Conserva los argumentos interpretados por {@link #parsear(String[])}. */
    private CommandLine(String operacion, String valorPosicional, Map<String, String> opciones) {
        this.operacion = operacion;
        this.valorPosicional = valorPosicional;
        this.opciones = opciones;
    }

    /**
     * Interpreta los argumentos de {@code main}. La operación puede escribirse con o sin guion inicial
     * ({@code -listar} o {@code listar}).
     *
     * @param args argumentos recibidos
     * @return argumentos interpretados
     * @throws TrackerException si no hay operación, una opción no está permitida o no tiene valor
     */
    public static CommandLine parsear(String[] args) {
        if (args == null || args.length == 0) {
            throw new TrackerException("No se indicó ninguna operación.");
        }
        String operacion = args[0].replaceFirst("^-+", "").toLowerCase(Locale.ROOT);
        String posicional = null;
        Map<String, String> opciones = new HashMap<>();

        for (int i = 1; i < args.length; i++) {
            String argumento = args[i];
            if (argumento.startsWith("--")) {
                String clave = argumento.substring(2).toLowerCase(Locale.ROOT);
                validarOpcion(operacion, clave);
                if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                    throw new TrackerException("La opción --" + clave + " necesita un valor.");
                }
                opciones.put(clave, args[++i]);
            } else if (posicional == null) {
                posicional = argumento;
            } else {
                throw new TrackerException("Argumento inesperado: '" + argumento + "'");
            }
        }
        return new CommandLine(operacion, posicional, opciones);
    }

    /** Comprueba que la opción pertenece al comando antes de leer su valor. */
    private static void validarOpcion(String operacion, String clave) {
        Set<String> opcionesPermitidas = switch (operacion) {
            case "listar" -> Set.of("estado", "empresa", "ordenar");
            case "anadir", "añadir" -> Set.of("titulo", "empresa", "estado", "fecha", "enlace", "notas");
            default -> Set.of();
        };
        if (!opcionesPermitidas.contains(clave)) {
            throw new TrackerException("La opción --" + clave + " no está permitida para '" + operacion + "'.");
        }
    }

    /**
     * @return operación normalizada, sin guion inicial y en minúsculas
     */
    public String operacion() {
        return operacion;
    }

    /**
     * @param nombre nombre del argumento, para el mensaje de error
     * @return el valor posicional (id o ruta)
     * @throws TrackerException si no se indicó
     */
    public String posicionalObligatorio(String nombre) {
        if (valorPosicional == null) {
            throw new TrackerException("Falta el argumento: " + nombre);
        }
        return valorPosicional;
    }

    /**
     * @param clave nombre de la opción sin guiones
     * @return valor de la opción o {@code null} si no se indicó
     */
    public String opcion(String clave) {
        return opciones.get(clave);
    }
}
