package com.tracker.repository;

import com.tracker.model.Estado;
import com.tracker.model.Oferta;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de ofertas.
 */
public interface OfertaRepository {

    /**
     * Busca ofertas combinando los criterios del filtro. Las más recientes salen primero.
     *
     * @param filtro criterios de búsqueda
     * @return ofertas que cumplen todos los criterios, con su empresa
     */
    List<Oferta> buscar(FiltroOfertas filtro);

    /**
     * @param id identificador buscado
     * @return la oferta con su empresa, si existe
     */
    Optional<Oferta> findById(int id);

    /**
     * Inserta la oferta si su id es 0 y la actualiza en caso contrario.
     *
     * @param oferta oferta a guardar
     * @return la oferta guardada, con su id
     */
    Oferta save(Oferta oferta);

    /**
     * Cambia el estado de una oferta.
     *
     * @param id     identificador de la oferta
     * @param estado nuevo estado
     */
    void cambiarEstado(int id, Estado estado);

    /**
     * Elimina una oferta y sus entrevistas.
     *
     * @param id identificador de la oferta
     */
    void delete(int id);

    /**
     * Importa varias ofertas en una única transacción: crea las empresas que no existan y omite las ofertas
     * ya registradas (mismo título, empresa y fecha). Si algo falla no se guarda nada.
     *
     * @param ofertas ofertas a importar; su empresa se identifica por el nombre
     * @return número de ofertas nuevas guardadas
     */
    int importar(List<Oferta> ofertas);
}
