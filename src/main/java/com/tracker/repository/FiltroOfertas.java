package com.tracker.repository;

import com.tracker.model.Estado;

import java.time.LocalDate;

/**
 * Criterios de búsqueda de ofertas. Los campos {@code null} no filtran; los informados se combinan con AND.
 *
 * @param texto      texto contenido en el título o en el nombre de la empresa
 * @param estado     estado exacto
 * @param empresa    texto contenido en el nombre de la empresa
 * @param salarioMin salario mínimo (incluido)
 * @param salarioMax salario máximo (incluido)
 * @param desde      fecha de postulación mínima (incluida)
 * @param hasta      fecha de postulación máxima (incluida)
 */
public record FiltroOfertas(String texto, Estado estado, String empresa, Double salarioMin, Double salarioMax,
                            LocalDate desde, LocalDate hasta) {

    /**
     * @return filtro que no excluye ninguna oferta
     */
    public static FiltroOfertas vacio() {
        return new FiltroOfertas(null, null, null, null, null, null, null);
    }
}
