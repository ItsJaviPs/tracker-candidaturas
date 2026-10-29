package com.tracker.model;

import com.tracker.exception.TrackerException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Estados posibles de una candidatura.
 */
public enum Estado {
    PENDIENTE,
    ENTREVISTA,
    RECHAZADO,
    ACEPTADO;

    /**
     * Convierte un texto en un estado, ignorando mayúsculas y espacios.
     *
     * @param texto texto introducido por el usuario o leído del fichero
     * @return el estado correspondiente
     * @throws TrackerException si el texto no corresponde a ningún estado
     */
    public static Estado desdeTexto(String texto) {
        if (texto != null) {
            String normalizado = texto.trim().toUpperCase(Locale.ROOT);
            for (Estado estado : values()) {
                if (estado.name().equals(normalizado)) {
                    return estado;
                }
            }
        }
        throw new TrackerException("Estado no válido: '" + texto + "'. Valores permitidos: " + valoresPermitidos());
    }

    /**
     * @return texto en minúsculas tal y como se guarda en el fichero
     */
    public String etiqueta() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Construye la lista de estados que se muestra en los mensajes de error. */
    private static String valoresPermitidos() {
        return Arrays.stream(values()).map(Estado::etiqueta).collect(Collectors.joining(", "));
    }
}
