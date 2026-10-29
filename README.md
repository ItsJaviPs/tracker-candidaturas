# CandiTrack — Tracker de Candidaturas Laborales

Aplicación Java para hacer seguimiento de candidaturas a ofertas de empleo o prácticas.
Proyecto integrador del 1.er trimestre (Acceso a Datos + Desarrollo de Interfaces).

**Equipo:** Javier Peña · Daniel García

## Versiones

| Tag  | Contenido                                                           | Estado     |
|------|---------------------------------------------------------------------|------------|
| v1.0 | Persistencia en ficheros + CLI + prototipo Figma + Manual de estilo | Entregada en la fila de v1.0  |
| v2.0 | JDBC + JavaFX (Scene Builder)                                       | Pendiente  |
| v3.0 | Hibernate + JavaFX avanzado + Ollama4j                              | Pendiente  |

## Tecnologías (v1.0)

- Java 24 · Maven
- Persistencia en fichero de texto plano (CSV con `;`)
- JUnit 5 + Mockito para las pruebas unitarias
- Figma (prototipo) · Manual de estilo en PDF

*(Se ampliará con JDBC, Hibernate, JavaFX, Scene Builder y Ollama en v2.0 y v3.0.)*

## Compilar y ejecutar (v1.0)

```bash
mvn clean package
java -jar target/tracker-candidaturas-1.0.jar -importar data/candidaturas.csv
java -jar target/tracker-candidaturas-1.0.jar -listar
```

El fichero de datos es `data/candidaturas.txt` (se crea al guardar). Se puede cambiar con la
variable de entorno `TRACKER_DATA`:

```bash
# Linux / macOS
TRACKER_DATA=/ruta/mis_datos.txt java -jar target/tracker-candidaturas-1.0.jar -listar
# Windows (PowerShell)
$env:TRACKER_DATA="C:\ruta\mis_datos.txt"; java -jar target\tracker-candidaturas-1.0.jar -listar
```

Ejecutar los tests: `mvn test`

### Pruebas

Con Java 24 y Maven, desde la raíz del proyecto, ejecuta `mvn test` para lanzar las
pruebas o `mvn clean package` para limpiar, compilar, probar y generar el JAR.
En IntelliJ: ventana **Maven** → **Execute Maven Goal** → `mvn clean package`.
Antes de cada commit se comprueba que aparece **BUILD SUCCESS**.

La suite contiene 42 pruebas con JUnit 5: 10 de `CommandLine` (argumentos y errores),
6 de `CommandRunner` (listar vacío y con filtros, añadir, ver, borrar y operación
desconocida), 16 del servicio con Mockito y 10 del repositorio (lectura, escritura,
fichero inexistente o vacío y registros con fecha, campos, estado o id incorrectos).
Las fechas se validan estrictamente: se rechazan días imposibles y el 29 de febrero
solo se admite en años bisiestos, tanto al añadir como al leer un fichero.
Las pruebas de la CLI usan `@TempDir` y capturan la salida con
`ByteArrayOutputStream`; no modifican `data/candidaturas.txt`.

En el equipo Windows de Daniel, Mockito 5.14.2 ha necesitado una propiedad local
`argLine` en **Settings → Build Tools → Maven → Runner → Properties**, con el valor
`-Dnet.bytebuddy.experimental=true -javaagent:<ruta-al-jar-mockito-core-5.14.2.jar>`.
El JAR está en el repositorio local de Maven, bajo
`org/mockito/mockito-core/5.14.2/`. La ruta debe apuntar al JAR de cada equipo.
También se han usado rutas cortas de Windows sin acentos para Maven y su repositorio
local, debido a un error de codificación del lanzador de IntelliJ. Son ajustes
locales del entorno; el `pom.xml` no se ha modificado.

### Operaciones

| Comando | Descripción |
|---------|-------------|
| `-listar` | Lista todas las candidaturas |
| `-listar --estado entrevista --empresa Indra` | Filtra por estado (exacto) y/o empresa (contiene el texto) |
| `-listar --ordenar fecha` | Ordena el listado por `id` (por defecto), `fecha` (más recientes primero), `empresa` o `titulo`. Se puede combinar con los filtros |
| `-añadir --titulo "Dev Java" --empresa "Indra" --estado pendiente --fecha 26/10/2026` | Añade una candidatura. `--fecha` (por defecto hoy), `--enlace` y `--notas` son opcionales |
| `-ver 13` | Muestra el detalle de la candidatura 13 |
| `-borrar 13` | Elimina la candidatura 13 |
| `-importar data/candidaturas.csv` | Importa candidaturas desde un CSV (si un id ya existe, se asigna el siguiente libre) |
| `ayuda` | Muestra la ayuda |

Estados válidos: `pendiente`, `entrevista`, `rechazado`, `aceptado`.
El guion inicial de la operación es opcional (`-listar` = `listar`).
Si la consola no muestra bien la `ñ`, usa `-anadir` (alias equivalente).

Los errores (argumentos faltantes, fichero no encontrado, id inexistente, fecha, estado u orden no válidos)
se muestran por consola con un mensaje claro y el programa termina con código de salida 1.

### Ejemplos de uso

Listado completo:

![Listado](docs/capturas/listar.png)

Filtro por estado y empresa:

![Filtro](docs/capturas/filtrar.png)

Error por id inexistente:

![Error](docs/capturas/error.png)

## Estructura

```
src/main/java/com/tracker
├── Main.java                  Punto de entrada
├── cli/                       Interpretación de argumentos y ejecución de comandos
├── model/                     Candidatura (record) y Estado (enum)
├── repository/                Interfaz + implementación sobre fichero de texto
├── service/                   Lógica de negocio (filtros, orden, altas, bajas, importación)
└── exception/                 TrackerException
data/candidaturas.csv          Datos de prueba (15 registros)
docs/                          Prototipo (PDF), Manual de estilo (PDF) y capturas de la CLI
```

## Datos de prueba

[`data/candidaturas.csv`](data/candidaturas.csv): 15 candidaturas, 5 empresas, entre 3 y 4 por estado,
con fechas de agosto a octubre de 2026. Generado con IA a partir de la plantilla del enunciado
(ver `AGENTS.md`).

## Diseño

- Prototipo Figma (solo lectura): **https://www.figma.com/proto/1mqY4IMHFg4MyFbTKW7PJ2/CandiTrack---Prototipo-v1.0?node-id=7-582&p=f&viewport=605%2C531%2C0.22&t=2DE6i4E39VPtJebN-1&scaling=min-zoom&content-scaling=fixed&starting-point-node-id=7%3A582&page-id=0%3A1**
- Pantallas del prototipo: [`docs/CandiTrack_Prototipo_v1.0.pdf`](docs/CandiTrack_Prototipo_v1.0.pdf)
- Manual de estilo: [`docs/CandiTrack_Manual_de_Estilo_v1.0.pdf`](docs/CandiTrack_Manual_de_Estilo_v1.0.pdf)

## Contribuciones

| Miembro | v1.0 | v2.0 | v3.0 |
|---------|------|------|------|
| Javier Peña | Compilación y pruebas del código Java (CLI, servicio y repositorio), ejecución de los tests con Maven, repositorio de GitHub, datos de prueba, opción `--ordenar` con sus tests y capturas de la CLI, prototipo interactivo en Figma (importación de pantallas y enlaces entre ellas) | | |
| Daniel García | Incorporación de 10 tests de CommandLine y 6 de CommandRunner con ficheros temporales y salida capturada; ampliación de FileRepositoryTest con 4 casos de ficheros vacíos y registros inválidos; comprobación con Maven y documentación de las pruebas, con asistencia de ChatGPT | | |
