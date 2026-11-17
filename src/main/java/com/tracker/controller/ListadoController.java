package com.tracker.controller;

import com.tracker.exception.TrackerException;
import com.tracker.model.Estado;
import com.tracker.model.Oferta;
import com.tracker.repository.FiltroOfertas;
import com.tracker.service.ResultadoImportacion;
import com.tracker.ui.Alertas;
import com.tracker.ui.Contexto;
import com.tracker.ui.ConversorEstado;
import com.tracker.ui.Formatos;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

/**
 * Pantalla del listado de candidaturas: filtros, tabla ordenable, cambio de estado en la propia tabla,
 * acceso al detalle y al formulario, importación de CSV y borrado.
 */
public class ListadoController {

    private final Contexto contexto;
    private final ObservableList<Oferta> datos = FXCollections.observableArrayList();

    @FXML
    private TextField campoBuscar;
    @FXML
    private TextField campoEmpresa;
    @FXML
    private TextField campoSalarioMin;
    @FXML
    private TextField campoSalarioMax;
    @FXML
    private ComboBox<Estado> comboEstado;
    @FXML
    private DatePicker fechaDesde;
    @FXML
    private DatePicker fechaHasta;
    @FXML
    private TableView<Oferta> tabla;
    @FXML
    private TableColumn<Oferta, Integer> colId;
    @FXML
    private TableColumn<Oferta, String> colTitulo;
    @FXML
    private TableColumn<Oferta, String> colEmpresa;
    @FXML
    private TableColumn<Oferta, Estado> colEstado;
    @FXML
    private TableColumn<Oferta, LocalDate> colFecha;
    @FXML
    private TableColumn<Oferta, Double> colSalario;
    @FXML
    private Label etiquetaTotal;

    /**
     * @param contexto servicios de la aplicación
     */
    public ListadoController(Contexto contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        comboEstado.getItems().setAll(Estado.values());
        comboEstado.setConverter(new ConversorEstado());

        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        colTitulo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().titulo()));
        colEmpresa.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().empresa().nombre()));
        colEstado.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().estado()));
        colFecha.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().fechaPostulacion()));
        colSalario.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().salario()));

        colEstado.setCellFactory(ComboBoxTableCell.forTableColumn(new ConversorEstado(), Estado.values()));
        colEstado.setOnEditCommit(evento -> cambiarEstado(evento.getRowValue(), evento.getNewValue()));
        colFecha.setCellFactory(columna -> new TableCell<Oferta, LocalDate>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia ? "" : Formatos.fecha(fecha));
            }
        });
        colSalario.setCellFactory(columna -> new TableCell<Oferta, Double>() {
            @Override
            protected void updateItem(Double salario, boolean vacio) {
                super.updateItem(salario, vacio);
                setText(vacio ? "" : Formatos.salario(salario));
            }
        });

        SortedList<Oferta> ordenadas = new SortedList<>(datos);
        ordenadas.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenadas);

        Platform.runLater(this::buscar);
    }

    @FXML
    private void buscar() {
        try {
            FiltroOfertas filtro = new FiltroOfertas(
                    Formatos.textoOnulo(campoBuscar.getText()),
                    comboEstado.getValue(),
                    Formatos.textoOnulo(campoEmpresa.getText()),
                    Formatos.numero(campoSalarioMin.getText(), "salario mínimo"),
                    Formatos.numero(campoSalarioMax.getText(), "salario máximo"),
                    fechaDesde.getValue(),
                    fechaHasta.getValue());
            datos.setAll(contexto.ofertas().buscar(filtro));
            etiquetaTotal.setText("Mostrando " + datos.size() + " candidaturas");
        } catch (TrackerException e) {
            Alertas.error(e.getMessage());
        }
    }

    @FXML
    private void limpiar() {
        campoBuscar.clear();
        campoEmpresa.clear();
        campoSalarioMin.clear();
        campoSalarioMax.clear();
        comboEstado.setValue(null);
        fechaDesde.setValue(null);
        fechaHasta.setValue(null);
        buscar();
    }

    @FXML
    private void nueva() {
        contexto.navegador().mostrarFormularioOferta(null);
    }

    @FXML
    private void ver() {
        Oferta oferta = seleccionada();
        if (oferta != null) {
            contexto.navegador().mostrarDetalle(oferta.id());
        }
    }

    @FXML
    private void editar() {
        Oferta oferta = seleccionada();
        if (oferta != null) {
            contexto.navegador().mostrarFormularioOferta(oferta);
        }
    }

    @FXML
    private void importarCsv() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Importar candidaturas desde CSV");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File fichero = selector.showOpenDialog(tabla.getScene().getWindow());
        if (fichero == null) {
            return;
        }
        try {
            ResultadoImportacion resultado = contexto.ofertas().importarCsv(fichero.toPath());
            Alertas.informacion("Importación terminada", resultado.leidas() + " leídas, "
                    + resultado.importadas() + " importadas y " + resultado.omitidas() + " omitidas (ya existían).");
            buscar();
        } catch (TrackerException e) {
            Alertas.error(e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Oferta oferta = seleccionada();
        if (oferta == null) {
            return;
        }
        if (!Alertas.confirmar("¿Eliminar '" + oferta.titulo() + "' de " + oferta.empresa().nombre()
                + "? También se borrarán sus entrevistas.")) {
            return;
        }
        try {
            contexto.ofertas().eliminar(oferta.id());
            buscar();
        } catch (TrackerException e) {
            Alertas.error(e.getMessage());
        }
    }

    private Oferta seleccionada() {
        Oferta oferta = tabla.getSelectionModel().getSelectedItem();
        if (oferta == null) {
            Alertas.informacion("Selecciona una candidatura", "Selecciona primero una candidatura de la tabla.");
        }
        return oferta;
    }

    private void cambiarEstado(Oferta oferta, Estado nuevo) {
        try {
            contexto.ofertas().cambiarEstado(oferta.id(), nuevo);
            buscar();
        } catch (TrackerException e) {
            Alertas.error(e.getMessage());
            tabla.refresh();
        }
    }
}
