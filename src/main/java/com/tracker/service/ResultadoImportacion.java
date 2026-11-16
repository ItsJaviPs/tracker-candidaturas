package com.tracker.service;

/**
 * Resultado de importar un fichero CSV a la base de datos.
 *
 * @param leidas     candidaturas leídas del fichero
 * @param importadas candidaturas nuevas guardadas en la base de datos
 */
public record ResultadoImportacion(int leidas, int importadas) {

    /**
     * @return candidaturas que ya estaban registradas y se han omitido
     */
    public int omitidas() {
        return leidas - importadas;
    }
}
