package com.tracker.ui;

/**
 * Permite a los controladores cambiar de pantalla sin conocer la ventana principal.
 */
public interface Navegador {

    /**
     * Muestra el listado de candidaturas.
     */
    void mostrarListado();
}
