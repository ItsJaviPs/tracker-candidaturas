package com.tracker.controller;

import com.tracker.exception.TrackerException;
import com.tracker.model.Oferta;
import com.tracker.ui.Alertas;
import com.tracker.ui.Contexto;
import com.tracker.ui.Navegador;
import com.tracker.ui.Vistas;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * Ventana principal: menú lateral y zona central donde se cargan las pantallas.
 */
public class PrincipalController implements Navegador {

    private static final String ESTILO_ACTIVO =
            "-fx-background-color: #1F4E8C; -fx-text-fill: white; -fx-font-weight: bold;";
    private static final String ESTILO_NORMAL = "-fx-background-color: transparent; -fx-text-fill: white;";

    private final Contexto contexto;

    @FXML
    private StackPane contenido;
    @FXML
    private Button botonCandidaturas;
    @FXML
    private Button botonEmpresas;

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

    @FXML
    private void irAEmpresas() {
        mostrarEmpresas();
    }

    @Override
    public void mostrarListado() {
        Vistas.Cargada<ListadoController> cargada = Vistas.cargar("listado", contexto);
        mostrar(cargada.raiz(), botonCandidaturas);
    }

    @Override
    public void mostrarDetalle(int idOferta) {
        try {
            Vistas.Cargada<DetalleController> cargada = Vistas.cargar("detalle", contexto);
            cargada.controlador().mostrar(idOferta);
            mostrar(cargada.raiz(), botonCandidaturas);
        } catch (TrackerException e) {
            Alertas.error(e.getMessage());
            mostrarListado();
        }
    }

    @Override
    public void mostrarFormularioOferta(Oferta oferta) {
        Vistas.Cargada<FormularioOfertaController> cargada = Vistas.cargar("formulario-oferta", contexto);
        cargada.controlador().preparar(oferta);
        mostrar(cargada.raiz(), botonCandidaturas);
    }

    @Override
    public void mostrarEmpresas() {
        Vistas.Cargada<EmpresasController> cargada = Vistas.cargar("empresas", contexto);
        mostrar(cargada.raiz(), botonEmpresas);
    }

    private void mostrar(javafx.scene.Parent pantalla, Button activo) {
        contenido.getChildren().setAll(pantalla);
        botonCandidaturas.setStyle(botonCandidaturas == activo ? ESTILO_ACTIVO : ESTILO_NORMAL);
        botonEmpresas.setStyle(botonEmpresas == activo ? ESTILO_ACTIVO : ESTILO_NORMAL);
    }
}
