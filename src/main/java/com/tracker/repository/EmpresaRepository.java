package com.tracker.repository;

import com.tracker.model.Empresa;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistencia de empresas.
 */
public interface EmpresaRepository {

    /**
     * @return todas las empresas ordenadas por nombre
     */
    List<Empresa> findAll();

    /**
     * @param id identificador buscado
     * @return la empresa, si existe
     */
    Optional<Empresa> findById(int id);

    /**
     * Inserta la empresa si su id es 0 y la actualiza en caso contrario.
     *
     * @param empresa empresa a guardar
     * @return la empresa guardada, con su id
     */
    Empresa save(Empresa empresa);

    /**
     * Elimina una empresa que no tenga ofertas asociadas.
     *
     * @param id identificador de la empresa
     */
    void delete(int id);

    /**
     * @return número de ofertas de cada empresa, por id de empresa (las empresas sin ofertas no aparecen)
     */
    Map<Integer, Integer> contarOfertasPorEmpresa();
}
