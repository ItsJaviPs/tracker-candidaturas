package com.tracker.ui;

import com.tracker.model.Oferta;

/**
 * Permite a los controladores cambiar de pantalla sin conocer la ventana principal.
 */
public interface Navegador {

    /**
     * Muestra el listado de candidaturas.
     */
    void mostrarListado();

    /**
     * Muestra el detalle de una candidatura con su empresa y sus entrevistas.
     *
     * @param idOferta identificador de la oferta
     */
    void mostrarDetalle(int idOferta);

    /**
     * Muestra el formulario de alta o edición de una candidatura.
     *
     * @param oferta oferta a editar, o {@code null} para crear una nueva
     */
    void mostrarFormularioOferta(Oferta oferta);

    /**
     * Muestra la gestión de empresas (listado y formulario).
     */
    void mostrarEmpresas();
}
