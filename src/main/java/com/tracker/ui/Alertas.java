package com.tracker.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Diálogos de aviso, información y confirmación.
 */
public final class Alertas {

    private Alertas() {
    }

    /**
     * Muestra un mensaje de error.
     *
     * @param mensaje texto para el usuario
     */
    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }

    /**
     * Muestra un mensaje informativo.
     *
     * @param titulo  título de la ventana
     * @param mensaje texto para el usuario
     */
    public static void informacion(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    /**
     * Pide confirmación al usuario.
     *
     * @param mensaje pregunta a mostrar
     * @return {@code true} si el usuario acepta
     */
    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        alerta.setTitle("Confirmar");
        alerta.setHeaderText(null);
        return alerta.showAndWait().filter(boton -> boton == ButtonType.OK).isPresent();
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
