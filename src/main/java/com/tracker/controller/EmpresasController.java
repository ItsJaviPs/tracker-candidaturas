package com.tracker.controller;

import com.tracker.exception.TrackerException;
import com.tracker.model.Empresa;
import com.tracker.ui.Alertas;
import com.tracker.ui.Contexto;
import com.tracker.ui.Formatos;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.Map;

/**
 * Gestión de empresas: tabla con el número de ofertas de cada una y formulario de alta, edición y baja.
 */
public class EmpresasController {

    private final Contexto contexto;
    private final ObservableList<Empresa> datos = FXCollections.observableArrayList();
    private Map<Integer, Integer> totalOfertas = Map.of();
    private Empresa seleccionada;

    @FXML
    private TableView<Empresa> tabla;
    @FXML
    private TableColumn<Empresa, String> colNombre;
    @FXML
    private TableColumn<Empresa, String> colSector;
    @FXML
    private TableColumn<Empresa, String> colWeb;
    @FXML
    private TableColumn<Empresa, Integer> colOfertas;
    @FXML
    private Label etiquetaFormulario;
    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoSector;
    @FXML
    private TextField campoWeb;
    @FXML
    private DatePicker fechaRegistro;
    @FXML
    private Label mensaje;

    /**
     * @param contexto servicios de la aplicación
     */
    public EmpresasController(Contexto contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().nombre()));
        colSector.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().sector()));
        colWeb.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().web()));
        colOfertas.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(totalOfertas.getOrDefault(c.getValue().id(), 0)));

        SortedList<Empresa> ordenadas = new SortedList<>(datos);
        ordenadas.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenadas);
        tabla.getSelectionModel().selectedItemProperty().addListener((observable, anterior, nueva) -> {
            if (nueva != null) {
                rellenar(nueva);
            }
        });

        nueva();
        Platform.runLater(this::cargar);
    }

    @FXML
    private void nueva() {
        seleccionada = null;
        tabla.getSelectionModel().clearSelection();
        etiquetaFormulario.setText("Nueva empresa");
        campoNombre.clear();
        campoSector.clear();
        campoWeb.clear();
        fechaRegistro.setValue(LocalDate.now());
        mensaje.setText("");
    }

    @FXML
    private void guardar() {
        try {
            int id = seleccionada == null ? 0 : seleccionada.id();
            contexto.empresas().guardar(new Empresa(id, campoNombre.getText(), campoSector.getText(),
                    campoWeb.getText(), fechaRegistro.getValue()));
            cargar();
            nueva();
            mensaje.setText("Empresa guardada.");
        } catch (TrackerException e) {
            mensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (seleccionada == null) {
            mensaje.setText("Selecciona una empresa de la tabla para eliminarla.");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar la empresa '" + seleccionada.nombre() + "'?")) {
            return;
        }
        try {
            contexto.empresas().eliminar(seleccionada.id());
            cargar();
            nueva();
            mensaje.setText("Empresa eliminada.");
        } catch (TrackerException e) {
            mensaje.setText(e.getMessage());
        }
    }

    private void cargar() {
        try {
            totalOfertas = contexto.empresas().contarOfertas();
            datos.setAll(contexto.empresas().listar());
            tabla.refresh();
        } catch (TrackerException e) {
            mensaje.setText(e.getMessage());
        }
    }

    private void rellenar(Empresa empresa) {
        seleccionada = empresa;
        etiquetaFormulario.setText("Editar empresa");
        campoNombre.setText(empresa.nombre());
        campoSector.setText(empresa.sector());
        campoWeb.setText(empresa.web());
        fechaRegistro.setValue(empresa.fechaRegistro());
        mensaje.setText("");
    }
}
