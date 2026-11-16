package com.tracker.service;

import com.tracker.exception.TrackerException;
import com.tracker.model.Entrevista;
import com.tracker.repository.EntrevistaRepository;

import java.util.List;

/**
 * Lógica de negocio de las entrevistas: consulta, alta, edición y baja con validación de los datos.
 */
public class EntrevistaService {

    /** Tipos de entrevista permitidos. */
    public static final List<String> TIPOS = List.of("telefónica", "técnica", "presencial", "online");

    /** Resultados permitidos. */
    public static final List<String> RESULTADOS = List.of("pendiente", "positivo", "negativo");

    private final EntrevistaRepository repositorio;

    /**
     * @param repositorio repositorio de entrevistas
     */
    public EntrevistaService(EntrevistaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * @return todas las entrevistas, las más recientes primero
     */
    public List<Entrevista> listar() {
        return repositorio.findAll();
    }

    /**
     * @param idOferta identificador de la oferta
     * @return entrevistas de esa oferta, las más recientes primero
     */
    public List<Entrevista> listarPorOferta(int idOferta) {
        return repositorio.findByOferta(idOferta);
    }

    /**
     * Valida y guarda una entrevista.
     *
     * @param entrevista entrevista a guardar (id 0 para un alta)
     * @return la entrevista guardada
     * @throws TrackerException si algún dato no es válido
     */
    public Entrevista guardar(Entrevista entrevista) {
        if (entrevista.oferta() == null || entrevista.oferta().id() <= 0) {
            throw new TrackerException("Selecciona la oferta de la entrevista.");
        }
        if (entrevista.fecha() == null) {
            throw new TrackerException("La fecha de la entrevista es obligatoria.");
        }
        if (!TIPOS.contains(entrevista.tipo())) {
            throw new TrackerException("Tipo de entrevista no válido. Valores permitidos: " + TIPOS);
        }
        if (!RESULTADOS.contains(entrevista.resultado())) {
            throw new TrackerException("Resultado no válido. Valores permitidos: " + RESULTADOS);
        }
        return repositorio.save(entrevista);
    }

    /**
     * @param id identificador de la entrevista a eliminar
     * @throws TrackerException si no existe
     */
    public void eliminar(int id) {
        repositorio.delete(id);
    }
}
