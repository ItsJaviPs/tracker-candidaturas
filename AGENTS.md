# AGENTS.md — Uso de IA generativa

Este proyecto se ha desarrollado con ayuda de un asistente de IA (Claude). Se documenta aquí su uso.

## Instrucciones de proyecto dadas al asistente
- Java 24, Maven, sin Scanner ni menús interactivos en v1.0.
- Arquitectura por capas: `cli` → `service` → `repository` (interfaz) → fichero de texto.
- Código y Javadoc en español, sin código muerto ni comentarios de depuración.
- Pruebas con JUnit 5 + Mockito sobre el servicio y el repositorio.

## Prompts principales
1. Generación del CSV de datos: plantilla del enunciado (15 registros, ≥4 empresas, ≥3 por estado, últimos 3 meses).
2. Generación del esqueleto de la CLI, el repositorio de ficheros y el servicio.
3. Generación del prototipo de pantallas y del manual de estilo (PDF).

## Revisión humana
Todo el código y los documentos generados han sido revisados, probados y adaptados por el equipo.
