package com.tracker.ui;

import com.tracker.exception.TrackerException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Carga pantallas FXML y les inyecta el {@link Contexto} a través del constructor de su controlador.
 */
public final class Vistas {

    private static final String RUTA = "/com/tracker/view/";

    private Vistas() {
    }

    /**
     * Pantalla cargada y su controlador.
     *
     * @param raiz        nodo raíz de la pantalla
     * @param controlador controlador asociado
     * @param <T>         tipo del controlador
     */
    public record Cargada<T>(Parent raiz, T controlador) {
    }

    /**
     * Carga el archivo FXML indicado.
     *
     * @param nombre   nombre del archivo sin extensión (por ejemplo {@code "listado"})
     * @param contexto servicios que recibirá el controlador
     * @param <T>      tipo del controlador
     * @return pantalla y controlador
     * @throws TrackerException si el archivo no existe o no se puede cargar
     */
    public static <T> Cargada<T> cargar(String nombre, Contexto contexto) {
        FXMLLoader cargador = new FXMLLoader(Vistas.class.getResource(RUTA + nombre + ".fxml"));
        cargador.setControllerFactory(clase -> crearControlador(clase, contexto));
        try {
            Parent raiz = cargador.load();
            T controlador = cargador.getController();
            return new Cargada<>(raiz, controlador);
        } catch (IOException | RuntimeException e) {
            throw new TrackerException("No se pudo cargar la pantalla '" + nombre + "': " + e.getMessage(), e);
        }
    }

    private static Object crearControlador(Class<?> clase, Contexto contexto) {
        try {
            return clase.getConstructor(Contexto.class).newInstance(contexto);
        } catch (ReflectiveOperationException e) {
            throw new TrackerException("No se pudo crear el controlador " + clase.getSimpleName(), e);
        }
    }
}
