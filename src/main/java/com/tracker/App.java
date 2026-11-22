package com.tracker;

import com.tracker.exception.TrackerException;
import com.tracker.ui.Alertas;
import com.tracker.ui.Contexto;
import com.tracker.ui.Vistas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Punto de entrada de la aplicación JavaFX (v2.0).
 */
public class App extends Application {

    private static final double ANCHO = 1280;
    private static final double ALTO = 800;
    private static final String HOJA_DE_ESTILOS = "/com/tracker/css/styles.css";

    @Override
    public void start(Stage ventana) {
        Vistas.Cargada<?> principal;
        try {
            principal = Vistas.cargar("principal", Contexto.crear());
        } catch (TrackerException e) {
            Alertas.error(e.getMessage());
            Platform.exit();
            return;
        }

        Scene escena = new Scene(principal.raiz(), ANCHO, ALTO);
        URL estilos = App.class.getResource(HOJA_DE_ESTILOS);
        if (estilos != null) {
            escena.getStylesheets().add(estilos.toExternalForm());
        }
        ventana.setTitle("CandiTrack - Tracker de Candidaturas");
        ventana.setMinWidth(1000);
        ventana.setMinHeight(650);
        ventana.setScene(escena);
        ventana.show();
    }

    /**
     * @param args argumentos de la línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
