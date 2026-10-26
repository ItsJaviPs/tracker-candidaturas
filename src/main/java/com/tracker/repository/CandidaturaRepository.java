package com.tracker.repository;

import com.tracker.model.Candidatura;

import java.util.List;

/**
 * Contrato de persistencia de candidaturas. Permite sustituir los ficheros por JDBC en la v2.0.
 */
public interface CandidaturaRepository {

    /**
     * @return todas las candidaturas almacenadas
     */
    List<Candidatura> findAll();

    /**
     * Sustituye el contenido almacenado por la lista indicada.
     *
     * @param candidaturas lista completa a guardar
     */
    void saveAll(List<Candidatura> candidaturas);
}
