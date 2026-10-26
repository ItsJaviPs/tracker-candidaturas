package com.tracker.cli;

import com.tracker.exception.TrackerException;
import com.tracker.model.Candidatura;
import com.tracker.repository.FileCandidaturaRepository;
import com.tracker.service.CandidaturaService;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Ejecuta la operación indicada en la línea de comandos y escribe el resultado en la salida.
 */
public class CommandRunner {

    private static final String FORMATO_FILA = "%-4s %-34s %-14s %-11s %-12s%n";

    private final CandidaturaService servicio;
    private final PrintStream salida;

    public CommandRunner(CandidaturaService servicio, PrintStream salida) {
        this.servicio = servicio;
        this.salida = salida;
    }

    /**
     * @param args argumentos de {@code main}
     * @throws TrackerException si la operación no existe o es incorrecta
     */
    public void ejecutar(String[] args) {
        CommandLine linea = CommandLine.parsear(args);
        switch (linea.operacion()) {
            case "listar" -> listar(linea);
            case "anadir", "añadir" -> anadir(linea);
            case "ver" -> ver(linea);
            case "borrar" -> borrar(linea);
            case "importar" -> importar(linea);
            case "ayuda", "help", "h" -> salida.print(ayuda());
            default -> throw new TrackerException("Operación desconocida: '" + linea.operacion() + "'.");
        }
    }

    /**
     * @return texto de ayuda con todas las operaciones
     */
    public static String ayuda() {
        return """
                Uso: java -jar tracker-candidaturas.jar <operación> [argumentos]

                Operaciones:
                  -listar [--estado X] [--empresa Y]
                  -añadir --titulo X --empresa Y --estado Z [--fecha dd/MM/yyyy] [--enlace URL] [--notas TXT]
                  -ver <id>
                  -borrar <id>
                  -importar <fichero.csv>

                Estados: pendiente, entrevista, rechazado, aceptado
                Fichero de datos: variable de entorno TRACKER_DATA (por defecto data/candidaturas.txt)
                """;
    }

    private void listar(CommandLine linea) {
        List<Candidatura> resultado = servicio.listar(linea.opcion("estado"), linea.opcion("empresa"));
        if (resultado.isEmpty()) {
            salida.println("No hay candidaturas que mostrar.");
            return;
        }
        salida.printf(FORMATO_FILA, "ID", "TÍTULO", "EMPRESA", "ESTADO", "FECHA");
        for (Candidatura c : resultado) {
            salida.printf(FORMATO_FILA, c.id(), recortar(c.titulo(), 34), recortar(c.empresa(), 14),
                    c.estado().etiqueta(), c.fechaPostulacion().format(FileCandidaturaRepository.FORMATO_FECHA));
        }
        salida.println(resultado.size() + " candidatura(s).");
    }

    private void anadir(CommandLine linea) {
        Candidatura creada = servicio.anadir(
                linea.opcion("titulo"), linea.opcion("empresa"), linea.opcion("estado"),
                linea.opcion("fecha"), linea.opcion("enlace"), linea.opcion("notas"));
        salida.println("Candidatura añadida con id " + creada.id() + ".");
    }

    private void ver(CommandLine linea) {
        Candidatura c = servicio.ver(parsearId(linea.posicionalObligatorio("id")));
        salida.println("ID:          " + c.id());
        salida.println("Título:      " + c.titulo());
        salida.println("Empresa:     " + c.empresa());
        salida.println("Estado:      " + c.estado().etiqueta());
        salida.println("Postulación: " + c.fechaPostulacion().format(FileCandidaturaRepository.FORMATO_FECHA));
        salida.println("Enlace:      " + vacioComoGuion(c.enlace()));
        salida.println("Notas:       " + vacioComoGuion(c.notas()));
    }

    private void borrar(CommandLine linea) {
        Candidatura borrada = servicio.borrar(parsearId(linea.posicionalObligatorio("id")));
        salida.println("Candidatura " + borrada.id() + " ('" + borrada.titulo() + "' en "
                + borrada.empresa() + ") eliminada.");
    }

    private void importar(CommandLine linea) {
        Path ruta = Path.of(linea.posicionalObligatorio("fichero.csv"));
        if (!Files.isRegularFile(ruta)) {
            throw new TrackerException("No se encontró el fichero '" + ruta + "'.");
        }
        int total = servicio.importar(new FileCandidaturaRepository(ruta));
        salida.println(total + " candidatura(s) importada(s) desde '" + ruta + "'.");
    }

    private int parsearId(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new TrackerException("El id '" + texto + "' no es un número válido.");
        }
    }

    private String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
    }

    private String vacioComoGuion(String texto) {
        return texto.isBlank() ? "-" : texto;
    }
}
