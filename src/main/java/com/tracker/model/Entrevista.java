package com.tracker.model;

import java.time.LocalDate;

/**
 * Entrevista asociada a una oferta.
 *
 * @param id        identificador (0 si aún no se ha guardado)
 * @param oferta    oferta a la que pertenece
 * @param fecha     fecha de la entrevista
 * @param tipo      tipo de entrevista (telefónica, técnica, presencial, online)
 * @param notas     notas sobre la entrevista (pueden estar vacías)
 * @param resultado resultado (pendiente, positivo o negativo)
 */
public record Entrevista(int id, Oferta oferta, LocalDate fecha, String tipo, String notas, String resultado) {
}
