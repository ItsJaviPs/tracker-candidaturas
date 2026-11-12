package com.tracker.model;

import java.time.LocalDate;

/**
 * Empresa a la que se han enviado candidaturas.
 *
 * @param id            identificador (0 si aún no se ha guardado)
 * @param nombre        nombre de la empresa
 * @param sector        sector de actividad
 * @param web           sitio web corporativo (puede estar vacío)
 * @param fechaRegistro fecha en que se registró la empresa
 */
public record Empresa(int id, String nombre, String sector, String web, LocalDate fechaRegistro) {

    /**
     * @return el nombre, para mostrar la empresa en listas desplegables
     */
    @Override
    public String toString() {
        return nombre;
    }
}
