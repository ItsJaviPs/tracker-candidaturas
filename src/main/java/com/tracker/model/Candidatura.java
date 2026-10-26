package com.tracker.model;

import java.time.LocalDate;

/**
 * Candidatura a una oferta de empleo (modelo v1.0: la empresa es un texto).
 *
 * @param id               identificador único
 * @param titulo           puesto al que se postula
 * @param empresa          nombre de la empresa
 * @param estado           estado actual del proceso
 * @param fechaPostulacion fecha de envío de la candidatura
 * @param enlace           URL de la oferta (puede estar vacío)
 * @param notas            notas personales (pueden estar vacías)
 */
public record Candidatura(
        int id,
        String titulo,
        String empresa,
        Estado estado,
        LocalDate fechaPostulacion,
        String enlace,
        String notas) {

    /**
     * @param nuevoId identificador a asignar
     * @return copia de la candidatura con otro identificador
     */
    public Candidatura conId(int nuevoId) {
        return new Candidatura(nuevoId, titulo, empresa, estado, fechaPostulacion, enlace, notas);
    }
}
