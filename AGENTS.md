# AGENTS.md — Uso de IA generativa en CandiTrack

Este documento recoge cómo se han usado herramientas de IA generativa en el proyecto, con qué instrucciones y qué parte del trabajo corresponde a cada miembro del equipo. Se incluye para que el trabajo asistido pueda evaluarse con transparencia.

## Herramientas utilizadas

| Herramienta | Uso |
|-------------|-----|
| Claude (Anthropic) | Asistente de código y documentación, y tutor paso a paso en Git, IntelliJ y Figma |
| ChatGPT | Tutoría de Java, JUnit y Git para Daniel; creación de tests, edición de documentación y ejecución de tareas en IntelliJ y GitHub Desktop a petición de Daniel |

## Instrucciones de proyecto dadas a la IA

- Java 24 y Maven. En v1.0, CLI por argumentos, sin `Scanner` ni menús interactivos.
- Arquitectura por capas: `cli` → `service` → `repository` (interfaz) → fichero de texto. La interfaz del repositorio permite sustituir los ficheros por JDBC en la v2.0.
- Código y Javadoc en español, sin código muerto ni comentarios de depuración.
- Pruebas unitarias con JUnit 5 y Mockito.
- Diseño siguiendo el manual de estilo del proyecto: paleta, tipografía DejaVu y componentes definidos en `docs/CandiTrack_Manual_de_Estilo_v1.0.pdf`.
## Prompts principales

1. **Datos de prueba:** plantilla del enunciado (15 registros, al menos 4 empresas, al menos 3 candidaturas por estado, fechas de los últimos 3 meses) para generar `data/candidaturas.csv`.
2. **Código base:** generación de la CLI, el repositorio de ficheros, el servicio y los tests iniciales a partir de los requisitos de la v1.0.
3. **Documentación y diseño:** borrador del README, del manual de estilo y de una maqueta inicial de las pantallas, en PDF.
4. **Mejora `--ordenar`:** opción de ordenación del listado (`id`, `fecha`, `empresa`, `titulo`), con sus tests.
5. **Pantallas del prototipo:** generador de las pantallas en SVG (`docs/tools/generar_svgs.py`) a partir del manual de estilo, para importarlas en Figma.
6. **Tutoría:** guía paso a paso para usar Git y GitHub Desktop, compilar con Maven en IntelliJ, y construir y enlazar el prototipo en Figma.
## Qué generó la IA

- La estructura inicial del código Java de la CLI (paquetes `cli`, `service`, `repository`, `model`, `exception`) y los tests iniciales.
- El CSV de datos de prueba.
- Los borradores del README, del manual de estilo y de la maqueta de pantallas.
- El generador de SVG de las pantallas y el código de la opción `--ordenar`.
## Aportaciones del equipo

**Javier Peña**
- Creación y gestión del repositorio público, colaboradores, commits y tag `v1.0` con GitHub Desktop.
- Configuración del entorno (IntelliJ, JDK, Maven), compilación con `mvn clean package` y ejecución de los tests.
- Pruebas manuales de todos los comandos de la CLI, corrección de la codificación de la consola en Windows y generación de las capturas de ejemplo.
- Integración y revisión de la opción `--ordenar` y de su documentación en el README.
- Prototipo en Figma: importación y revisión de las pantallas, portada, enlaces del prototipo interactivo y publicación con acceso de solo lectura.
- Exportación del PDF de pantallas, ordenado con ayuda del asistente, y actualización de la documentación del repositorio.

**Daniel García**

- Incorporación, con asistencia de ChatGPT, de 10 pruebas de `CommandLine` y 6 de `CommandRunner`, estas últimas con un repositorio real sobre `@TempDir` y captura de salida.
- Ampliación de `FileRepositoryTest` con 4 pruebas: pocos campos, estado inválido, id no numérico y fichero vacío.
- Comprobación asistida de la compilación y de los 36 tests resultantes mediante Maven en IntelliJ, y diagnóstico de los ajustes locales necesarios para Windows y Java 24.
- Actualización de sus contribuciones y de la sección de pruebas del README, y documentación del uso de ChatGPT en este archivo.

## Prompts de Daniel a ChatGPT (resumen)

1. Actuar como tutor paciente de Java y Git, leer los archivos existentes y explicar cada paso para poder defender el trabajo en un vídeo, sin cambiar paquetes, clases ni el `pom.xml`.
2. Preparar el entorno, actualizar primero el repositorio con GitHub Desktop y comprobar la compilación y los tests.
3. Crear tests de `CommandLine` para operaciones con y sin guion, opciones con y sin valor, argumentos inesperados y ausencia de operación.
4. Crear tests de `CommandRunner` con servicio y repositorio reales, directorio temporal y captura de salida, para listar, filtrar, añadir, ver, borrar y rechazar una operación desconocida.
5. Ampliar los tests del repositorio sin duplicarlos y documentar las aportaciones reales, las pruebas y el uso de IA en README y AGENTS.
6. Realizar las acciones en el ordenador y continuar sin detenerse en cada paso, manteniendo un commit por tarea y ejecutando `clean package` desde Maven en IntelliJ antes de cada commit.

ChatGPT no solo proporcionó explicaciones: también generó y escribió los tests y los
cambios de documentación, y manejó IntelliJ y GitHub Desktop. Durante la preparación
se ajustaron rutas locales de Maven en Windows y la ejecución de Mockito con Java 24;
el `pom.xml` y el código de producción no se modificaron.

## Revisión

El código y los documentos de la fase original asistida por Claude se han revisado,
ejecutado y adaptado por el equipo. Los cambios están en el historial de commits con
su autor. Las nuevas aportaciones asistidas por ChatGPT se han comprobado mediante
pruebas automáticas; su revisión humana por Daniel queda pendiente. Daniel debe
revisarlas y comprenderlas antes de presentarlas y explicarlas en su vídeo.
