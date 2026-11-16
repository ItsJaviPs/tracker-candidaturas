package com.tracker.ui;

import com.tracker.config.ConexionBd;
import com.tracker.config.DbConfig;
import com.tracker.exception.TrackerException;
import com.tracker.repository.JdbcEmpresaRepository;
import com.tracker.repository.JdbcEntrevistaRepository;
import com.tracker.repository.JdbcOfertaRepository;
import com.tracker.service.EmpresaService;
import com.tracker.service.EntrevistaService;
import com.tracker.service.OfertaService;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Reúne los servicios de la aplicación y el navegador entre pantallas, para pasárselos a los controladores.
 */
public class Contexto {

    private final OfertaService ofertas;
    private final EmpresaService empresas;
    private final EntrevistaService entrevistas;
    private Navegador navegador;

    /**
     * @param ofertas     servicio de ofertas
     * @param empresas    servicio de empresas
     * @param entrevistas servicio de entrevistas
     */
    public Contexto(OfertaService ofertas, EmpresaService empresas, EntrevistaService entrevistas) {
        this.ofertas = ofertas;
        this.empresas = empresas;
        this.entrevistas = entrevistas;
    }

    /**
     * Crea el contexto real: lee la configuración, comprueba la conexión con MySQL y monta los servicios.
     *
     * @return contexto listo para usar
     * @throws TrackerException si falta la configuración o no se puede conectar con la base de datos
     */
    public static Contexto crear() {
        ConexionBd conexion = new ConexionBd(DbConfig.cargar());
        comprobar(conexion);
        return new Contexto(
                new OfertaService(new JdbcOfertaRepository(conexion)),
                new EmpresaService(new JdbcEmpresaRepository(conexion)),
                new EntrevistaService(new JdbcEntrevistaRepository(conexion)));
    }

    private static void comprobar(ConexionBd conexion) {
        try (Connection c = conexion.abrir()) {
            if (!c.isValid(3)) {
                throw new TrackerException("La conexión con la base de datos no es válida.");
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo comprobar la conexión con la base de datos: " + e.getMessage(), e);
        }
    }

    public OfertaService ofertas() {
        return ofertas;
    }

    public EmpresaService empresas() {
        return empresas;
    }

    public EntrevistaService entrevistas() {
        return entrevistas;
    }

    public Navegador navegador() {
        return navegador;
    }

    public void setNavegador(Navegador navegador) {
        this.navegador = navegador;
    }
}
