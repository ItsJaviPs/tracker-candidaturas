package com.tracker.service;

import com.tracker.exception.TrackerException;
import com.tracker.model.Candidatura;
import com.tracker.model.Estado;
import com.tracker.repository.CandidaturaRepository;
import com.tracker.repository.FileCandidaturaRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lógica de negocio de las candidaturas: listado, filtrado, alta, consulta, borrado e importación.
 */
public class CandidaturaService {

    private final CandidaturaRepository repositorio;

    /**
     * @param repositorio almacenamiento utilizado para leer y guardar las candidaturas
     */
    public CandidaturaService(CandidaturaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Lista las candidaturas ordenadas por id, aplicando los filtros indicados.
     *
     * @param estado  estado exacto (texto) o {@code null} para no filtrar
     * @param empresa texto contenido en el nombre de la empresa (sin distinguir mayúsculas) o {@code null}
     * @return candidaturas que cumplen todos los filtros informados
     * @throws TrackerException si el estado no es válido
     */
    public List<Candidatura> listar(String estado, String empresa) {
        return listar(estado, empresa, null);
    }

    /**
     * Lista las candidaturas aplicando los filtros indicados y el orden pedido.
     *
     * @param estado    estado exacto (texto) o {@code null} para no filtrar
     * @param empresa   texto contenido en el nombre de la empresa o {@code null}
     * @param ordenarPor {@code "id"} (por defecto), {@code "fecha"} (más recientes primero),
     *                   {@code "empresa"} o {@code "titulo"}
     * @return candidaturas que cumplen los filtros, en el orden pedido
     * @throws TrackerException si el estado o el criterio de orden no son válidos
     */
    public List<Candidatura> listar(String estado, String empresa, String ordenarPor) {
        Estado estadoFiltro = estado == null ? null : Estado.desdeTexto(estado);
        String empresaFiltro = empresa == null ? null : empresa.trim().toLowerCase(Locale.ROOT);
        Comparator<Candidatura> orden = comparadorPara(ordenarPor);

        return repositorio.findAll().stream()
                .filter(c -> estadoFiltro == null || c.estado() == estadoFiltro)
                .filter(c -> empresaFiltro == null
                        || c.empresa().toLowerCase(Locale.ROOT).contains(empresaFiltro))
                .sorted(orden)
                .toList();
    }

    /** Selecciona el orden solicitado y usa el id para resolver los empates. */
    private Comparator<Candidatura> comparadorPara(String ordenarPor) {
        Comparator<Candidatura> porId = Comparator.comparingInt(Candidatura::id);
        if (ordenarPor == null) {
            return porId;
        }
        return switch (ordenarPor.trim().toLowerCase(Locale.ROOT)) {
            case "id" -> porId;
            case "fecha" -> Comparator.comparing(Candidatura::fechaPostulacion).reversed().thenComparing(porId);
            case "empresa" -> Comparator.comparing((Candidatura c) -> c.empresa().toLowerCase(Locale.ROOT))
                    .thenComparing(porId);
            case "titulo" -> Comparator.comparing((Candidatura c) -> c.titulo().toLowerCase(Locale.ROOT))
                    .thenComparing(porId);
            default -> throw new TrackerException("Criterio de orden no válido: '" + ordenarPor
                    + "'. Valores permitidos: id, fecha, empresa, titulo");
        };
    }

    /**
     * Añade una candidatura con el siguiente id disponible.
     *
     * @param titulo  puesto (obligatorio)
     * @param empresa empresa (obligatoria)
     * @param estado  estado en texto (obligatorio)
     * @param fecha   fecha dd/MM/yyyy; si es {@code null} se usa la fecha de hoy
     * @param enlace  URL opcional
     * @param notas   notas opcionales
     * @return la candidatura creada
     */
    public Candidatura anadir(String titulo, String empresa, String estado, String fecha,
                              String enlace, String notas) {
        exigirTexto(titulo, "titulo");
        exigirTexto(empresa, "empresa");
        exigirTexto(estado, "estado");

        List<Candidatura> todas = repositorio.findAll();
        Candidatura nueva = new Candidatura(
                siguienteId(todas),
                titulo.trim(),
                empresa.trim(),
                Estado.desdeTexto(estado),
                parsearFecha(fecha),
                enlace == null ? "" : enlace.trim(),
                notas == null ? "" : notas.trim());

        todas.add(nueva);
        repositorio.saveAll(todas);
        return nueva;
    }

    /**
     * @param id identificador buscado
     * @return la candidatura con ese id
     * @throws TrackerException si no existe
     */
    public Candidatura ver(int id) {
        return repositorio.findAll().stream()
                .filter(c -> c.id() == id)
                .findFirst()
                .orElseThrow(() -> new TrackerException("No existe ninguna candidatura con id " + id));
    }

    /**
     * Elimina la candidatura con el id indicado.
     *
     * @param id identificador a borrar
     * @return la candidatura eliminada
     * @throws TrackerException si no existe
     */
    public Candidatura borrar(int id) {
        List<Candidatura> todas = repositorio.findAll();
        Candidatura objetivo = todas.stream()
                .filter(c -> c.id() == id)
                .findFirst()
                .orElseThrow(() -> new TrackerException("No existe ninguna candidatura con id " + id));
        todas.remove(objetivo);
        repositorio.saveAll(todas);
        return objetivo;
    }

    /**
     * Importa las candidaturas de otro repositorio (el CSV de origen). Si un id ya existe
     * en el destino, la candidatura importada recibe el siguiente id libre.
     *
     * @param origen repositorio del que se leen las candidaturas
     * @return número de candidaturas importadas
     */
    public int importar(CandidaturaRepository origen) {
        List<Candidatura> importadas = origen.findAll();
        List<Candidatura> destino = repositorio.findAll();
        Set<Integer> idsUsados = new HashSet<>();
        destino.forEach(c -> idsUsados.add(c.id()));

        int siguiente = siguienteId(destino);
        for (Candidatura candidatura : importadas) {
            Candidatura nueva = candidatura;
            if (idsUsados.contains(candidatura.id())) {
                while (idsUsados.contains(siguiente)) {
                    siguiente++;
                }
                nueva = candidatura.conId(siguiente);
            }
            idsUsados.add(nueva.id());
            destino.add(nueva);
        }
        repositorio.saveAll(destino);
        return importadas.size();
    }

    /** Devuelve uno más que el mayor id existente, o uno si la lista está vacía. */
    private int siguienteId(List<Candidatura> candidaturas) {
        return candidaturas.stream().mapToInt(Candidatura::id).max().orElse(0) + 1;
    }

    /** Valida la fecha introducida o utiliza la fecha actual si no se indica. */
    private LocalDate parsearFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(fecha.trim(), FileCandidaturaRepository.FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new TrackerException("Fecha no válida: '" + fecha + "'. Usa el formato dd/MM/yyyy");
        }
    }

    /** Rechaza un dato obligatorio nulo, vacío o compuesto solo por espacios. */
    private void exigirTexto(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new TrackerException("Falta el argumento obligatorio --" + nombre);
        }
    }
}
