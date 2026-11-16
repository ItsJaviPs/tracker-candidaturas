package com.tracker.service;

import com.tracker.exception.TrackerException;
import com.tracker.model.Candidatura;
import com.tracker.model.Empresa;
import com.tracker.model.Estado;
import com.tracker.model.Oferta;
import com.tracker.repository.FileCandidaturaRepository;
import com.tracker.repository.FiltroOfertas;
import com.tracker.repository.OfertaRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de las ofertas: búsqueda con filtros, alta, edición, cambio de estado, baja e
 * importación desde CSV.
 */
public class OfertaService {

    private static final int MAX_TITULO = 150;
    private static final int MAX_ENLACE = 500;
    private static final String SECTOR_POR_DEFECTO = "Sin clasificar";

    private final OfertaRepository repositorio;

    /**
     * @param repositorio repositorio de ofertas
     */
    public OfertaService(OfertaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Busca ofertas combinando los criterios del filtro.
     *
     * @param filtro criterios de búsqueda
     * @return ofertas que los cumplen todos
     * @throws TrackerException si el rango de salario o de fechas es incoherente
     */
    public List<Oferta> buscar(FiltroOfertas filtro) {
        if (filtro.salarioMin() != null && filtro.salarioMax() != null && filtro.salarioMin() > filtro.salarioMax()) {
            throw new TrackerException("El salario mínimo no puede ser mayor que el máximo.");
        }
        if (filtro.desde() != null && filtro.hasta() != null && filtro.desde().isAfter(filtro.hasta())) {
            throw new TrackerException("La fecha inicial no puede ser posterior a la final.");
        }
        return repositorio.buscar(filtro);
    }

    /**
     * @param id identificador buscado
     * @return la oferta con su empresa
     * @throws TrackerException si no existe
     */
    public Oferta ver(int id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new TrackerException("No existe ninguna oferta con id " + id));
    }

    /**
     * Valida y guarda una oferta.
     *
     * @param oferta oferta a guardar (id 0 para un alta)
     * @return la oferta guardada
     * @throws TrackerException si algún dato no es válido
     */
    public Oferta guardar(Oferta oferta) {
        if (oferta.titulo() == null || oferta.titulo().isBlank()) {
            throw new TrackerException("El título es obligatorio.");
        }
        if (oferta.titulo().trim().length() > MAX_TITULO) {
            throw new TrackerException("El título no puede superar " + MAX_TITULO + " caracteres.");
        }
        if (oferta.empresa() == null || oferta.empresa().id() <= 0) {
            throw new TrackerException("Selecciona una empresa.");
        }
        if (oferta.estado() == null) {
            throw new TrackerException("Selecciona un estado.");
        }
        if (oferta.fechaPostulacion() == null) {
            throw new TrackerException("La fecha de postulación es obligatoria.");
        }
        if (oferta.salario() != null && oferta.salario() < 0) {
            throw new TrackerException("El salario no puede ser negativo.");
        }
        String enlace = oferta.enlace() == null ? "" : oferta.enlace().trim();
        if (enlace.length() > MAX_ENLACE) {
            throw new TrackerException("El enlace no puede superar " + MAX_ENLACE + " caracteres.");
        }
        if (!enlace.isEmpty() && !enlace.startsWith("http://") && !enlace.startsWith("https://")) {
            throw new TrackerException("El enlace debe empezar por http:// o https://");
        }
        return repositorio.save(new Oferta(oferta.id(), oferta.empresa(), oferta.titulo().trim(),
                oferta.descripcion(), oferta.estado(), oferta.fechaPostulacion(), enlace, oferta.salario(),
                oferta.notas()));
    }

    /**
     * Cambia el estado de una oferta.
     *
     * @param id     identificador de la oferta
     * @param estado nuevo estado
     * @throws TrackerException si la oferta no existe
     */
    public void cambiarEstado(int id, Estado estado) {
        if (estado == null) {
            throw new TrackerException("Selecciona un estado.");
        }
        repositorio.cambiarEstado(id, estado);
    }

    /**
     * Elimina una oferta junto con sus entrevistas.
     *
     * @param id identificador de la oferta
     * @throws TrackerException si no existe
     */
    public void eliminar(int id) {
        repositorio.delete(id);
    }

    /**
     * Importa un CSV de candidaturas (formato de la v1.0) a la base de datos. Las empresas que no existan se
     * crean con el sector "Sin clasificar" y las candidaturas ya registradas se omiten.
     *
     * @param fichero ruta del CSV
     * @return número de candidaturas leídas e importadas
     * @throws TrackerException si el fichero no existe o es inválido, o si falla la importación
     */
    public ResultadoImportacion importarCsv(Path fichero) {
        if (!Files.isRegularFile(fichero)) {
            throw new TrackerException("No se encontró el fichero '" + fichero + "'.");
        }
        List<Candidatura> leidas = new FileCandidaturaRepository(fichero).findAll();
        List<Oferta> ofertas = leidas.stream().map(this::aOferta).toList();
        int importadas = repositorio.importar(ofertas);
        return new ResultadoImportacion(leidas.size(), importadas);
    }

    private Oferta aOferta(Candidatura candidatura) {
        Empresa empresa = new Empresa(0, candidatura.empresa(), SECTOR_POR_DEFECTO, "", LocalDate.now());
        return new Oferta(0, empresa, candidatura.titulo(), "", candidatura.estado(),
                candidatura.fechaPostulacion(), candidatura.enlace(), null, candidatura.notas());
    }
}
