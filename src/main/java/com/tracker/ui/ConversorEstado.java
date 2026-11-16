package com.tracker.ui;

import com.tracker.model.Estado;
import javafx.util.StringConverter;

/**
 * Muestra los estados en minúsculas (pendiente, entrevista...) en tablas y desplegables.
 */
public class ConversorEstado extends StringConverter<Estado> {

    @Override
    public String toString(Estado estado) {
        return estado == null ? "" : estado.etiqueta();
    }

    @Override
    public Estado fromString(String texto) {
        return texto == null || texto.isBlank() ? null : Estado.desdeTexto(texto);
    }
}
