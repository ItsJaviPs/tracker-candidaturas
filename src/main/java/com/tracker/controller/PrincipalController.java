package com.tracker.controller;

import com.tracker.exception.TrackerException;
import com.tracker.model.Oferta;
import com.tracker.ui.Alertas;
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
        Vistas.Cargada<ListadoController> cargada = Vistas.cargar("listado", contexto);
        contenido.getChildren().setAll(cargada.raiz());
    }

    @Override
    public void mostrarDetalle(int idOferta) {
        try {
            Vistas.Cargada<DetalleController> cargada = Vistas.cargar("detalle", contexto);
            cargada.controlador().mostrar(idOferta);
            contenido.getChildren().setAll(cargada.raiz());
        } catch (TrackerException e) {
            Alertas.error(e.getMessage());
            mostrarListado();
        }
    }

    @Override
    public void mostrarFormularioOferta(Oferta oferta) {
        Vistas.Cargada<FormularioOfertaController> cargada = Vistas.cargar("formulario-oferta", contexto);
        cargada.controlador().preparar(oferta);
        contenido.getChildren().setAll(cargada.raiz());
    }
}
