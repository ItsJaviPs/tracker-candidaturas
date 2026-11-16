package com.tracker.controller;

import com.tracker.ui.Contexto;
import com.tracker.ui.Navegador;
import com.tracker.ui.Vistas;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

/**
 * Ventana principal: menú lateral y zona central donde se cargan las pantallas.
 */
public class PrincipalController implements Navegador {

    private final Contexto contexto;

    @FXML
    private StackPane contenido;

    /**
     * @param contexto servicios de la aplicación
     */
    public PrincipalController(Contexto contexto) {
        this.contexto = contexto;
        contexto.setNavegador(this);
    }

    @FXML
    private void initialize() {
        mostrarListado();
    }

    @FXML
    private void irAListado() {
        mostrarListado();
    }

    @Override
    public void mostrarListado() {
        mostrar("listado");
    }

    private void mostrar(String pantalla) {
        Vistas.Cargada<?> cargada = Vistas.cargar(pantalla, contexto);
        contenido.getChildren().setAll(cargada.raiz());
    }
}
