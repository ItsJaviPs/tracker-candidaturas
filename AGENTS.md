# AGENTS.md — Uso de IA generativa en CandiTrack

Este documento recoge cómo se han usado herramientas de IA generativa en el proyecto, con qué instrucciones y qué
parte del trabajo corresponde a cada miembro del equipo. Se incluye para que el trabajo asistido pueda evaluarse con
transparencia.

## Herramientas utilizadas

| Herramienta | Uso |
|-------------|-----|
| Claude (Anthropic) | Asistente de código y documentación, y tutor paso a paso en Git, IntelliJ, MySQL, Scene Builder y Figma |
| ChatGPT | Asistencia en las pruebas y la documentación de la v1.0 (Daniel García) |

---

# Versión 1.0 — Archivos + CLI + Diseño

## Instrucciones de proyecto dadas a la IA

- Java 24 y Maven. En v1.0, CLI por argumentos, sin `Scanner` ni menús interactivos.
- Arquitectura por capas: `cli` → `service` → `repository` (interfaz) → fichero de texto. La interfaz del
  repositorio permite sustituir los ficheros por JDBC en la v2.0.
- Código y Javadoc en español, sin código muerto ni comentarios de depuración.
- Pruebas unitarias con JUnit 5 y Mockito.
- Diseño siguiendo el manual de estilo del proyecto: paleta, tipografía DejaVu y componentes definidos en
  `docs/CandiTrack_Manual_de_Estilo_v1.0.pdf`.

## Prompts principales

1. **Datos de prueba:** plantilla del enunciado (15 registros, al menos 4 empresas, al menos 3 candidaturas por
   estado, fechas de los últimos 3 meses) para generar `data/candidaturas.csv`.
2. **Código base:** generación de la CLI, el repositorio de ficheros, el servicio y los tests iniciales a partir de
   los requisitos de la v1.0.
3. **Documentación y diseño:** borrador del README, del manual de estilo y de una maqueta inicial de las pantallas,
   en PDF.
4. **Mejora `--ordenar`:** opción de ordenación del listado (`id`, `fecha`, `empresa`, `titulo`), con sus tests.
5. **Pantallas del prototipo:** generador de las pantallas en SVG (`docs/tools/generar_svgs.py`) a partir del
   manual de estilo, para importarlas en Figma.
6. **Tutoría:** guía paso a paso para usar Git y GitHub Desktop, compilar con Maven en IntelliJ, y construir y
   enlazar el prototipo en Figma.

## Qué generó la IA

- La estructura inicial del código Java de la CLI (paquetes `cli`, `service`, `repository`, `model`, `exception`)
  y los tests iniciales.
- El CSV de datos de prueba.
- Los borradores del README, del manual de estilo y de la maqueta de pantallas.
- El generador de SVG de las pantallas y el código de la opción `--ordenar`.

## Aportaciones del equipo en la v1.0

**Javier Peña**
- Creación y gestión del repositorio público, colaboradores, commits y etiquetas con GitHub Desktop.
- Configuración del entorno (IntelliJ, JDK, Maven), compilación con `mvn clean package` y ejecución de los tests.
- Pruebas manuales de todos los comandos de la CLI, corrección de la codificación de la consola en Windows y
  generación de las capturas de ejemplo.
- Integración y revisión de la opción `--ordenar` y de su documentación en el README.
- Prototipo en Figma: importación y revisión de las pantallas, portada, enlaces del prototipo interactivo y
  publicación con acceso de solo lectura.
- Exportación del PDF de pantallas, ordenado con ayuda del asistente, y actualización de la documentación.

**Daniel García**
- Incorporación de 10 tests de `CommandLine` y 6 de `CommandRunner` con ficheros temporales y salida capturada.
- Ampliación de las pruebas de servicio y repositorio para fechas imposibles, años bisiestos y registros de siete
  campos, y validación estricta de fechas y campos.
- Comprobación con Maven y documentación de las pruebas y de la entrega, con asistencia de ChatGPT.

---

# Versión 2.0 — JDBC + JavaFX

## Instrucciones de proyecto dadas a la IA

- Java 24, Maven, MySQL 8 con JDBC; JavaFX con FXML y Scene Builder.
- Arquitectura por capas: `controller` → `service` → `repository` (interfaces con implementaciones JDBC) → MySQL.
- Consultas siempre con `PreparedStatement`, recursos cerrados con try-with-resources y transacciones en las
  operaciones que afectan a varias tablas.
- Configuración de la conexión externalizada; la contraseña nunca se sube al repositorio.
- Código y Javadoc en español, sin código muerto ni comentarios de depuración.
- Estilos según el manual de estilo de la v1.0.

## Prompts principales

1. **Esquema:** base de datos MySQL (`db/schema.sql`) a partir del modelo de datos del enunciado.
2. **Conexión y modelo:** configuración de la conexión (`DbConfig`, `ConexionBd`) y entidades `Empresa`, `Oferta`
   y `Entrevista`.
3. **Repositorios JDBC** con `JOIN`, filtros combinados, cambio de estado e importación transaccional del CSV.
4. **Servicios** con validación de campos, y pruebas unitarias (Mockito) y de integración (H2 en memoria).
5. **Pantallas JavaFX:** ventana principal, listado, detalle con entrevistas, formulario de candidatura y empresas.
6. **Estilos:** hoja de estilos CSS con las clases del manual de estilo.
7. **Tutoría paso a paso** para instalar y configurar MySQL, ejecutar el proyecto en IntelliJ y trabajar en
   Scene Builder.

## Qué generó la IA

- La capa de acceso a datos (configuración, repositorios JDBC, servicios y validaciones) y sus tests.
- Los controladores JavaFX, la primera versión de los archivos FXML y la hoja de estilos `styles.css`.

## Aportaciones del equipo en la v2.0

**Javier Peña**
- Instalación y configuración de MySQL y Scene Builder; creación de la base de datos con `schema.sql` y de la
  configuración local sin subir la contraseña al repositorio.
- Integración del código en el proyecto, ejecución de las pruebas (101 en verde en la última ejecución) y prueba
  manual de las pantallas contra MySQL, incluida la importación del CSV.
- Trabajo en Scene Builder: asignación de clases CSS del manual de estilo a los componentes del listado,
  corrección de errores de diseño (el botón *Buscar* y el campo de búsqueda) y ajustes en la pantalla de empresas
  (textos del formulario y ancho de la tarjeta).
- Gestión del repositorio y de la documentación.

**Daniel García**
- *(Pendiente de completar por Daniel con las tareas realizadas en la v2.0.)*

## Revisión

Todo el código y los documentos generados con IA se han revisado, ejecutado y adaptado por el equipo antes de
incorporarse al repositorio. Los cambios y su autoría están en el historial de commits.
