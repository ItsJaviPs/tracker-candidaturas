package com.tracker.ui;

import com.tracker.exception.TrackerException;
import com.tracker.repository.FileCandidaturaRepository;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Conversiones de texto compartidas por las pantallas.
 */
public final class Formatos {

    private static final NumberFormat SALARIO = NumberFormat.getNumberInstance(Locale.of("es", "ES"));

    private Formatos() {
    }

    /**
     * @param fecha fecha a mostrar
     * @return la fecha como dd/MM/yyyy, o cadena vacía si es nula
     */
    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FileCandidaturaRepository.FORMATO_FECHA);
    }

    /**
     * @param salario salario a mostrar
     * @return el salario con separador de miles y símbolo de euro, o cadena vacía si es nulo
     */
    public static String salario(Double salario) {
        return salario == null ? "" : SALARIO.format(salario) + " €";
    }

    /**
     * @param texto texto escrito por el usuario
     * @return el texto sin espacios sobrantes, o {@code null} si está vacío
     */
    public static String textoOnulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    /**
     * Convierte un texto en número aceptando la coma como separador decimal.
     *
     * @param texto texto escrito por el usuario
     * @param campo nombre del campo, para el mensaje de error
     * @return el número, o {@code null} si el texto está vacío
     * @throws TrackerException si el texto no es un número
     */
    public static Double numero(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new TrackerException("El " + campo + " debe ser un número.");
        }
    }
}
