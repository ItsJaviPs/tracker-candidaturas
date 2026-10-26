package com.tracker.exception;

/**
 * Error de negocio o de uso de la aplicación con un mensaje apto para el usuario.
 */
public class TrackerException extends RuntimeException {

    public TrackerException(String mensaje) {
        super(mensaje);
    }

    public TrackerException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
