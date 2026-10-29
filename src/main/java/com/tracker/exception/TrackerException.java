package com.tracker.exception;

/**
 * Error de negocio o de uso de la aplicación con un mensaje apto para el usuario.
 */
public class TrackerException extends RuntimeException {

    /**
     * @param mensaje explicación del error para el usuario
     */
    public TrackerException(String mensaje) {
        super(mensaje);
    }

    /**
     * @param mensaje explicación del error para el usuario
     * @param causa error original de lectura o escritura
     */
    public TrackerException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
