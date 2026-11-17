package com.tracker.controller;

import com.tracker.exception.TrackerException;
import com.tracker.model.Empresa;
import com.tracker.model.Estado;
import com.tracker.model.Oferta;
import com.tracker.ui.Contexto;
import com.tracker.ui.ConversorEstado;
import com.tracker.ui.Formatos;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

/**
 * Formulario de alta y edición de una candidatura, con validación de los campos.
 */
public class FormularioOfertaController {

    private final Contexto contexto;
    private Oferta original;

    @FXML
    private Label etiquetaTitulo;
    @FXML
    private Label mensajeError;
    @FXML
    private TextField campoTitulo;
    @FXML
    private ComboBox<Empresa> comboEmpresa;
    @FXML
    private ComboBox<Estado> comboEstado;
    @FXML
    private DatePicker fechaPostulacion;
    @FXML
    private TextField campoSalario;
    @FXML
    private TextField campoEnlace;
    @FXML
    private TextArea campoDescripcion;
    @FXML
    private TextArea campoNotas;

    /**
     * @param contexto servicios de la aplicación
     */
    public FormularioOfertaController(Contexto contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        comboEstado.getItems().setAll(Estado.values());
        comboEstado.setConverter(new ConversorEstado());
        try {
            comboEmpresa.getItems().setAll(contexto.empresas().listar());
            if (comboEmpresa.getItems().isEmpty()) {
                mensajeError.setText("No hay empresas registradas. Importa un CSV o da de alta una empresa primero.");
            }
        } catch (TrackerException e) {
            mensajeError.setText(e.getMessage());
        }
    }

    /**
     * Prepara el formulario para crear o editar una candidatura.
     *
     * @param oferta oferta a editar, o {@code null} para crear una nueva
     */
    public void preparar(Oferta oferta) {
        original = oferta;
        if (oferta == null) {
            etiquetaTitulo.setText("Nueva candidatura");
            comboEstado.setValue(Estado.PENDIENTE);
            fechaPostulacion.setValue(LocalDate.now());
            return;
        }
        etiquetaTitulo.setText("Editar candidatura");
        campoTitulo.setText(oferta.titulo());
        comboEmpresa.getItems().stream()
                .filter(empresa -> empresa.id() == oferta.empresa().id())
                .findFirst()
                .ifPresent(comboEmpresa::setValue);
        comboEstado.setValue(oferta.estado());
        fechaPostulacion.setValue(oferta.fechaPostulacion());
        campoSalario.setText(oferta.salario() == null ? "" : String.valueOf(oferta.salario()));
        campoEnlace.setText(oferta.enlace());
        campoDescripcion.setText(oferta.descripcion());
        campoNotas.setText(oferta.notas());
    }

    @FXML
    private void guardar() {
        try {
            Oferta nueva = new Oferta(original == null ? 0 : original.id(), comboEmpresa.getValue(),
                    campoTitulo.getText(), campoDescripcion.getText(), comboEstado.getValue(),
                    fechaPostulacion.getValue(), campoEnlace.getText(),
                    Formatos.numero(campoSalario.getText(), "salario"), campoNotas.getText());
            Oferta guardada = contexto.ofertas().guardar(nueva);
            contexto.navegador().mostrarDetalle(guardada.id());
        } catch (TrackerException e) {
            mensajeError.setText(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        if (original == null) {
            contexto.navegador().mostrarListado();
        } else {
            contexto.navegador().mostrarDetalle(original.id());
        }
    }
}
