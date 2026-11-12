package com.tracker.model;

import java.time.LocalDate;

/**
 * Candidatura a una oferta de empleo (modelo v2.0: la empresa es una entidad).
 *
 * @param id               identificador (0 si aún no se ha guardado)
 * @param empresa          empresa de la oferta
 * @param titulo           puesto al que se postula
 * @param descripcion      descripción de la oferta (puede estar vacía)
 * @param estado           estado actual del proceso
 * @param fechaPostulacion fecha de envío de la candidatura
 * @param enlace           URL de la oferta (puede estar vacía)
 * @param salario          salario ofrecido, o {@code null} si no se conoce
 * @param notas            notas personales (pueden estar vacías)
 */
public record Oferta(
        int id,
        Empresa empresa,
        String titulo,
        String descripcion,
        Estado estado,
        LocalDate fechaPostulacion,
        String enlace,
        Double salario,
        String notas) {

    /**
     * @param nuevoEstado estado a asignar
     * @return copia de la oferta con otro estado
     */
    public Oferta conEstado(Estado nuevoEstado) {
        return new Oferta(id, empresa, titulo, descripcion, nuevoEstado, fechaPostulacion, enlace, salario, notas);
    }
}
