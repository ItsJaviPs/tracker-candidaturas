package com.tracker.service;

import com.tracker.exception.TrackerException;
import com.tracker.model.Empresa;
import com.tracker.repository.EmpresaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Lógica de negocio de las empresas: consulta, alta, edición y baja con validación de los datos.
 */
public class EmpresaService {

    private static final int MAX_NOMBRE = 120;
    private static final int MAX_SECTOR = 80;
    private static final int MAX_WEB = 255;

    private final EmpresaRepository repositorio;

    /**
     * @param repositorio repositorio de empresas
     */
    public EmpresaService(EmpresaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * @return todas las empresas ordenadas por nombre
     */
    public List<Empresa> listar() {
        return repositorio.findAll();
    }

    /**
     * @param id identificador buscado
     * @return la empresa
     * @throws TrackerException si no existe
     */
    public Empresa ver(int id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new TrackerException("No existe ninguna empresa con id " + id));
    }

    /**
     * Valida y guarda una empresa. Si no tiene fecha de registro se usa la de hoy.
     *
     * @param empresa empresa a guardar (id 0 para un alta)
     * @return la empresa guardada
     * @throws TrackerException si algún dato no es válido o el nombre ya existe
     */
    public Empresa guardar(Empresa empresa) {
        String nombre = obligatorio(empresa.nombre(), "El nombre de la empresa es obligatorio.", MAX_NOMBRE);
        String sector = obligatorio(empresa.sector(), "El sector es obligatorio.", MAX_SECTOR);
        String web = empresa.web() == null ? "" : empresa.web().trim();
        if (web.length() > MAX_WEB) {
            throw new TrackerException("La web no puede superar " + MAX_WEB + " caracteres.");
        }
        if (!web.isEmpty() && !web.startsWith("http://") && !web.startsWith("https://")) {
            throw new TrackerException("La web debe empezar por http:// o https://");
        }
        LocalDate fecha = empresa.fechaRegistro() == null ? LocalDate.now() : empresa.fechaRegistro();
        return repositorio.save(new Empresa(empresa.id(), nombre, sector, web, fecha));
    }

    /**
     * Elimina una empresa que no tenga ofertas asociadas.
     *
     * @param id identificador de la empresa
     * @throws TrackerException si no existe o tiene ofertas
     */
    public void eliminar(int id) {
        repositorio.delete(id);
    }

    /**
     * @return número de ofertas de cada empresa, por id de empresa
     */
    public Map<Integer, Integer> contarOfertas() {
        return repositorio.contarOfertasPorEmpresa();
    }

    private String obligatorio(String valor, String mensaje, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new TrackerException(mensaje);
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new TrackerException("El texto no puede superar " + maximo + " caracteres: " + limpio);
        }
        return limpio;
    }
}
