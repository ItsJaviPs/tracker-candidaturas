# AGENTS.md — Uso de IA generativa en CandiTrack

Este documento recoge cómo se han usado herramientas de IA generativa en el proyecto, con qué instrucciones y qué parte del trabajo corresponde a cada miembro del equipo. Se incluye para que el trabajo asistido pueda evaluarse con transparencia.

## Herramientas utilizadas

| Herramienta | Uso |
|-------------|-----|
| Claude (Anthropic) | Asistente de código y documentación, y tutor paso a paso en Git, IntelliJ y Figma |
| ChatGPT | [Daniel: completar si lo has usado, indicando para qué] |

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
- [Completar con las tareas realizadas.]
## Revisión

Todo el código y los documentos generados con IA se han revisado, ejecutado y adaptado por el equipo antes de incorporarse al repositorio. Los cambios están en el historial de commits con su autor.
