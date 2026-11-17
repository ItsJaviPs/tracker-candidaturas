package com.tracker.controller;

import com.tracker.exception.TrackerException;
import com.tracker.model.Entrevista;
import com.tracker.model.Estado;
import com.tracker.model.Oferta;
import com.tracker.service.EntrevistaService;
import com.tracker.ui.Alertas;
import com.tracker.ui.Contexto;
import com.tracker.ui.ConversorEstado;
import com.tracker.ui.Formatos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.time.LocalDate;

/**
 * Detalle de una candidatura: datos de la oferta, empresa asociada y panel de entrevistas con su formulario.
 */
public class DetalleController {

    private final Contexto contexto;
    private final ObservableList<Entrevista> entrevistas = FXCollections.observableArrayList();
    private Oferta oferta;
    private Entrevista entrevistaSeleccionada;
    private boolean cargandoEstado;

    @FXML
    private Label etiquetaTitulo;
    @FXML
    private Label etiquetaEmpresa;
    @FXML
    private Label etiquetaSector;
    @FXML
    private Label etiquetaWeb;
    @FXML
    private Label etiquetaFecha;
    @FXML
    private Label etiquetaSalario;
    @FXML
    private Label etiquetaEnlace;
    @FXML
    private Label etiquetaDescripcion;
    @FXML
    private Label etiquetaNotas;
    @FXML
    private Label mensajeEntrevista;
    @FXML
    private ComboBox<Estado> comboEstado;
    @FXML
    private TableView<Entrevista> tablaEntrevistas;
    @FXML
    private TableColumn<Entrevista, LocalDate> colFechaEntrevista;
    @FXML
    private TableColumn<Entrevista, String> colTipo;
    @FXML
    private TableColumn<Entrevista, String> colResultado;
    @FXML
    private TableColumn<Entrevista, String> colNotasEntrevista;
    @FXML
    private DatePicker fechaEntrevista;
    @FXML
    private ComboBox<String> comboTipo;
    @FXML
    private ComboBox<String> comboResultado;
    @FXML
    private TextArea notasEntrevista;

    /**
     * @param contexto servicios de la aplicación
     */
    public DetalleController(Contexto contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        comboEstado.getItems().setAll(Estado.values());
        comboEstado.setConverter(new ConversorEstado());
        comboEstado.setOnAction(evento -> {
            if (!cargandoEstado && oferta != null && comboEstado.getValue() != null) {
                cambiarEstado(comboEstado.getValue());
            }
        });

        comboTipo.getItems().setAll(EntrevistaService.TIPOS);
        comboResultado.getItems().setAll(EntrevistaService.RESULTADOS);

        colFechaEntrevista.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().fecha()));
        colTipo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().tipo()));
        colResultado.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().resultado()));
        colNotasEntrevista.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().notas()));
        colFechaEntrevista.setCellFactory(columna -> new TableCell<Entrevista, LocalDate>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia ? "" : Formatos.fecha(fecha));
            }
        });

        tablaEntrevistas.setItems(entrevistas);
        tablaEntrevistas.getSelectionModel().selectedItemProperty().addListener((observable, anterior, nueva) -> {
            if (nueva != null) {
                rellenarFormulario(nueva);
            }
        });
    }

    /**
     * Carga en la pantalla la oferta indicada y sus entrevistas.
     *
     * @param idOferta identificador de la oferta
     * @throws TrackerException si la oferta no existe
     */
    public void mostrar(int idOferta) {
        oferta = contexto.ofertas().ver(idOferta);
        etiquetaTitulo.setText(oferta.titulo());
        etiquetaEmpresa.setText(oferta.empresa().nombre());
        etiquetaSector.setText(oferta.empresa().sector());
        etiquetaWeb.setText(oferta.empresa().web());
        etiquetaFecha.setText(Formatos.fecha(oferta.fechaPostulacion()));
        etiquetaSalario.setText(oferta.salario() == null ? "No indicado" : Formatos.salario(oferta.salario()));
        etiquetaEnlace.setText(oferta.enlace());
        etiquetaDescripcion.setText(oferta.descripcion());
        etiquetaNotas.setText(oferta.notas());
        cargandoEstado = true;
        comboEstado.setValue(oferta.estado());
        cargandoEstado = false;
        cargarEntrevistas();
        nuevaEntrevista();
    }

    @FXML
    private void volver() {
        contexto.navegador().mostrarListado();
    }

    @FXML
    private void editar() {
        contexto.navegador().mostrarFormularioOferta(oferta);
    }

    @FXML
    private void nuevaEntrevista() {
        entrevistaSeleccionada = null;
        tablaEntrevistas.getSelectionModel().clearSelection();
        fechaEntrevista.setValue(LocalDate.now());
        comboTipo.setValue(null);
        comboResultado.setValue("pendiente");
        notasEntrevista.clear();
        mensajeEntrevista.setText("");
    }

    @FXML
    private void guardarEntrevista() {
        try {
            int id = entrevistaSeleccionada == null ? 0 : entrevistaSeleccionada.id();
            contexto.entrevistas().guardar(new Entrevista(id, oferta, fechaEntrevista.getValue(),
                    comboTipo.getValue(), notasEntrevista.getText(), comboResultado.getValue()));
            cargarEntrevistas();
            nuevaEntrevista();
            mensajeEntrevista.setText("Entrevista guardada.");
        } catch (TrackerException e) {
            mensajeEntrevista.setText(e.getMessage());
        }
    }

    @FXML
    private void eliminarEntrevista() {
        if (entrevistaSeleccionada == null) {
            mensajeEntrevista.setText("Selecciona una entrevista de la tabla para eliminarla.");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar la entrevista del " + Formatos.fecha(entrevistaSeleccionada.fecha()) + "?")) {
            return;
        }
        try {
            contexto.entrevistas().eliminar(entrevistaSeleccionada.id());
            cargarEntrevistas();
            nuevaEntrevista();
            mensajeEntrevista.setText("Entrevista eliminada.");
        } catch (TrackerException e) {
            mensajeEntrevista.setText(e.getMessage());
        }
    }

    private void cambiarEstado(Estado nuevo) {
        try {
            contexto.ofertas().cambiarEstado(oferta.id(), nuevo);
            oferta = oferta.conEstado(nuevo);
        } catch (TrackerException e) {
            Alertas.error(e.getMessage());
            cargandoEstado = true;
            comboEstado.setValue(oferta.estado());
            cargandoEstado = false;
        }
    }

    private void cargarEntrevistas() {
        entrevistas.setAll(contexto.entrevistas().listarPorOferta(oferta.id()));
    }

    private void rellenarFormulario(Entrevista entrevista) {
        entrevistaSeleccionada = entrevista;
        fechaEntrevista.setValue(entrevista.fecha());
        comboTipo.setValue(entrevista.tipo());
        comboResultado.setValue(entrevista.resultado());
        notasEntrevista.setText(entrevista.notas());
        mensajeEntrevista.setText("");
    }
}
