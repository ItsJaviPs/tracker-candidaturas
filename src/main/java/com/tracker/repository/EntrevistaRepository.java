package com.tracker.repository;

import com.tracker.model.Entrevista;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de entrevistas.
 */
public interface EntrevistaRepository {

    /**
     * @return todas las entrevistas con su oferta y empresa, las más recientes primero
     */
    List<Entrevista> findAll();

    /**
     * @param idOferta identificador de la oferta
     * @return entrevistas de esa oferta, las más recientes primero
     */
    List<Entrevista> findByOferta(int idOferta);

    /**
     * @param id identificador buscado
     * @return la entrevista, si existe
     */
    Optional<Entrevista> findById(int id);

    /**
     * Inserta la entrevista si su id es 0 y la actualiza en caso contrario.
     *
     * @param entrevista entrevista a guardar
     * @return la entrevista guardada, con su id
     */
    Entrevista save(Entrevista entrevista);

    /**
     * @param id identificador de la entrevista a eliminar
     */
    void delete(int id);
}
