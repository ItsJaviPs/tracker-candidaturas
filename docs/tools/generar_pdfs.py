#!/usr/bin/env python3
"""Genera los PDF de la v1.0: prototipo de pantallas y manual de estilo de CandiTrack.

Uso:  python3 generar_pdfs.py "Nombre Alumno 1" "Nombre Alumno 2" [directorio_salida]
Requiere: reportlab
"""
import sys
from pathlib import Path

from reportlab.lib.colors import HexColor, white
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas

NOMBRES = sys.argv[1:3] if len(sys.argv) >= 3 else ["[Nombre Alumno 1]", "[Nombre Alumno 2]"]
SALIDA = Path(sys.argv[3]) if len(sys.argv) >= 4 else Path(__file__).resolve().parent.parent
EQUIPO = " · ".join(NOMBRES)

import os


def _dir_fuentes():
    candidatos = [os.environ.get("FONT_DIR", ""), "/usr/share/fonts/truetype/dejavu/",
                  "/usr/share/fonts/dejavu/", "C:/Windows/Fonts/"]
    try:
        import matplotlib
        candidatos.append(os.path.join(os.path.dirname(matplotlib.__file__), "mpl-data", "fonts", "ttf"))
    except ImportError:
        pass
    for d in candidatos:
        if d and os.path.exists(os.path.join(d, "DejaVuSans.ttf")):
            return d.rstrip("/\\") + "/"
    sys.exit("No se encontró DejaVuSans.ttf. Instala la fuente o define FONT_DIR con su carpeta.")


FUENTES = _dir_fuentes()
pdfmetrics.registerFont(TTFont("Sans", FUENTES + "DejaVuSans.ttf"))
pdfmetrics.registerFont(TTFont("Sans-B", FUENTES + "DejaVuSans-Bold.ttf"))
pdfmetrics.registerFont(TTFont("Mono", FUENTES + "DejaVuSansMono.ttf"))
pdfmetrics.registerFont(TTFont("Mono-B", FUENTES + "DejaVuSansMono-Bold.ttf"))

# ---------------------------------------------------------------- paleta
PRIMARY, PRIMARY_DARK, PRIMARY_LIGHT = "#1F4E8C", "#163A6A", "#E3ECF7"
SECONDARY = "#0B7A6B"
ACCENT = "#E8590C"
BG, SURFACE, BORDER = "#F4F6FA", "#FFFFFF", "#D5DBE5"
TEXT, MUTED = "#1B2430", "#5B6778"
DANGER, SUCCESS = "#C62828", "#166534"
ESTADOS = {  # fondo, texto
    "pendiente": ("#FEF3C7", "#92400E"),
    "entrevista": ("#DBEAFE", "#1E40AF"),
    "rechazado": ("#FEE2E2", "#991B1B"),
    "aceptado": ("#DCFCE7", "#166534"),
}


def hc(valor):
    return HexColor(valor) if isinstance(valor, str) else valor


def luminancia(hexcolor):
    h = hexcolor.lstrip("#")
    canales = [int(h[i:i + 2], 16) / 255 for i in (0, 2, 4)]
    lin = [c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4 for c in canales]
    return 0.2126 * lin[0] + 0.7152 * lin[1] + 0.0722 * lin[2]


def contraste(a, b):
    la, lb = sorted((luminancia(a), luminancia(b)), reverse=True)
    return (la + 0.05) / (lb + 0.05)


# ---------------------------------------------------------------- helpers de dibujo (coordenadas desde arriba)
class Lienzo:
    def __init__(self, ruta, tam, titulo):
        self.c = canvas.Canvas(str(ruta), pagesize=tam)
        self.c.setTitle(titulo)
        self.c.setAuthor(EQUIPO)
        self.W, self.H = tam

    def y(self, v):
        return self.H - v

    def texto(self, x, y, s, size=10, fuente="Sans", color=TEXT, align="l"):
        c = self.c
        c.setFont(fuente, size)
        c.setFillColor(hc(color))
        if align == "c":
            c.drawCentredString(x, self.y(y), s)
        elif align == "r":
            c.drawRightString(x, self.y(y), s)
        else:
            c.drawString(x, self.y(y), s)

    def recortar(self, s, ancho, size, fuente="Sans"):
        while s and pdfmetrics.stringWidth(s, fuente, size) > ancho:
            s = s[:-1]
        return s

    def parrafo(self, x, y, s, ancho, size=10, fuente="Sans", color=TEXT, interlineado=1.45):
        palabras, linea, yy = s.split(), "", y
        for p in palabras:
            prueba = (linea + " " + p).strip()
            if pdfmetrics.stringWidth(prueba, fuente, size) > ancho and linea:
                self.texto(x, yy, linea, size, fuente, color)
                yy += size * interlineado
                linea = p
            else:
                linea = prueba
        if linea:
            self.texto(x, yy, linea, size, fuente, color)
            yy += size * interlineado
        return yy

    def rect(self, x, y, w, h, fill=None, stroke=None, r=0, lw=0.8):
        c = self.c
        if fill:
            c.setFillColor(hc(fill))
        if stroke:
            c.setStrokeColor(hc(stroke))
            c.setLineWidth(lw)
        if r:
            c.roundRect(x, self.y(y + h), w, h, r, stroke=1 if stroke else 0, fill=1 if fill else 0)
        else:
            c.rect(x, self.y(y + h), w, h, stroke=1 if stroke else 0, fill=1 if fill else 0)

    def linea(self, x1, y1, x2, y2, color=BORDER, lw=0.8):
        self.c.setStrokeColor(hc(color))
        self.c.setLineWidth(lw)
        self.c.line(x1, self.y(y1), x2, self.y(y2))

    def circulo(self, x, y, r, fill=None, stroke=None, lw=0.8):
        if fill:
            self.c.setFillColor(hc(fill))
        if stroke:
            self.c.setStrokeColor(hc(stroke))
            self.c.setLineWidth(lw)
        self.c.circle(x, self.y(y), r, stroke=1 if stroke else 0, fill=1 if fill else 0)

    def enlace(self, x, y, w, h, destino):
        self.c.linkRect("", destino, (x, self.y(y + h), x + w, self.y(y)), relative=0, thickness=0)

    def pagina(self):
        self.c.showPage()

    def guardar(self):
        self.c.save()


# ---------------------------------------------------------------- componentes
def logo(L, x, y, s, fondo=PRIMARY, linea=white, marca=SECONDARY):
    """Isotipo: tarjeta con lista de candidaturas y marca de verificación."""
    L.rect(x, y, s, s, fill=fondo, r=s * 0.22)
    for i, ancho in enumerate((0.52, 0.40, 0.46)):
        yy = y + s * (0.26 + i * 0.17)
        L.circulo(x + s * 0.2, yy, s * 0.03, fill=linea)
        L.rect(x + s * 0.28, yy - s * 0.03, s * ancho, s * 0.06, fill=linea, r=s * 0.03)
    cx, cy, r = x + s * 0.76, y + s * 0.76, s * 0.22
    L.circulo(cx, cy, r, fill=marca, stroke=fondo, lw=s * 0.05)
    c = L.c
    c.setStrokeColor(white)
    c.setLineWidth(s * 0.05)
    c.setLineCap(1)
    p = c.beginPath()
    p.moveTo(cx - r * 0.45, L.y(cy + r * 0.02))
    p.lineTo(cx - r * 0.1, L.y(cy + r * 0.38))
    p.lineTo(cx + r * 0.5, L.y(cy - r * 0.35))
    c.drawPath(p, stroke=1, fill=0)
    c.setLineCap(0)


def wordmark(L, x, y, size, color1=TEXT, color2=PRIMARY):
    L.texto(x, y, "Candi", size, "Sans-B", color1)
    L.texto(x + pdfmetrics.stringWidth("Candi", "Sans-B", size), y, "Track", size, "Sans-B", color2)


def boton(L, x, y, w, h, texto, estilo="primario", link=None, size=8):
    estilos = {
        "primario": (PRIMARY, None, "#FFFFFF"),
        "secundario": (SURFACE, PRIMARY, PRIMARY),
        "peligro": (DANGER, None, "#FFFFFF"),
        "ia": (SECONDARY, None, "#FFFFFF"),
        "fantasma": (None, None, PRIMARY),
        "deshabilitado": ("#E5E8EE", None, "#6B7280"),
    }
    fill, stroke, color = estilos[estilo]
    L.rect(x, y, w, h, fill=fill, stroke=stroke, r=5, lw=1.2)
    L.texto(x + w / 2, y + h / 2 + size * 0.35, texto, size, "Sans-B", color, "c")
    if link:
        L.enlace(x, y, w, h, link)


def badge(L, x, y, estado, size=7, etiqueta=None):
    fondo, color = ESTADOS[estado]
    texto = etiqueta or estado
    w = pdfmetrics.stringWidth(texto, "Sans-B", size) + 14
    L.rect(x, y, w, 14, fill=fondo, r=7)
    L.texto(x + w / 2, y + 10, texto, size, "Sans-B", color, "c")
    return w


def campo(L, x, y, w, etiqueta, valor="", h=22, estado="normal", select=False, obligatorio=False, placeholder=False):
    etiqueta_txt = etiqueta + (" *" if obligatorio else "")
    L.texto(x, y, etiqueta_txt, 7.5, "Sans-B", TEXT)
    borde = {"normal": BORDER, "foco": PRIMARY, "error": DANGER, "deshabilitado": BORDER}[estado]
    fondo = "#EEF0F4" if estado == "deshabilitado" else SURFACE
    L.rect(x, y + 4, w, h, fill=fondo, stroke=borde, r=4, lw=1.6 if estado in ("foco", "error") else 0.9)
    L.texto(x + 7, y + 4 + h / 2 + 3, L.recortar(valor, w - 26, 8),
            8, "Sans", MUTED if (placeholder or estado == "deshabilitado") else TEXT)
    if select:
        L.texto(x + w - 12, y + 4 + h / 2 + 3, "▾", 8, "Sans", MUTED, "c")


def tabla(L, x, y, anchos, cabecera, filas, alto_fila=22, seleccion=None, link_fila=None):
    total = sum(anchos)
    L.rect(x, y, total, alto_fila, fill=PRIMARY_LIGHT, r=0)
    xx = x
    for a, cab in zip(anchos, cabecera):
        L.texto(xx + 7, y + alto_fila / 2 + 3, cab, 7.5, "Sans-B", PRIMARY_DARK)
        xx += a
    for i, fila in enumerate(filas):
        yy = y + alto_fila * (i + 1)
        fondo = PRIMARY_LIGHT if seleccion == i else (SURFACE if i % 2 == 0 else "#F8FAFC")
        L.rect(x, yy, total, alto_fila, fill=fondo)
        L.linea(x, yy + alto_fila, x + total, yy + alto_fila, BORDER, 0.5)
        xx = x
        for a, celda in zip(anchos, fila):
            if isinstance(celda, tuple) and celda[0] == "badge":
                badge(L, xx + 6, yy + 4, celda[1], 7, celda[2] if len(celda) > 2 else None)
            elif isinstance(celda, tuple) and celda[0] == "acciones":
                L.texto(xx + 7, yy + alto_fila / 2 + 3, "Ver   Editar   Borrar", 7, "Sans-B", PRIMARY)
            else:
                L.texto(xx + 7, yy + alto_fila / 2 + 3, L.recortar(str(celda), a - 12, 7.5), 7.5, "Sans", TEXT)
            xx += a
        if link_fila:
            L.enlace(x, yy, total, alto_fila, link_fila)
    L.rect(x, y, total, alto_fila * (len(filas) + 1), stroke=BORDER, r=0, lw=0.8)


def tarjeta(L, x, y, w, h, titulo=None):
    L.rect(x + 1.5, y + 2, w, h, fill="#E6E9EF", r=8)
    L.rect(x, y, w, h, fill=SURFACE, stroke=BORDER, r=8)
    if titulo:
        L.texto(x + 12, y + 18, titulo, 9, "Sans-B", PRIMARY_DARK)


def marcador(L, x, y, n):
    L.circulo(x, y, 7, fill=ACCENT)
    L.texto(x, y + 3, str(n), 8, "Sans-B", "#FFFFFF", "c")


# ---------------------------------------------------------------- datos de ejemplo (CSV de prueba)
FILAS = [
    ("1", "Desarrollador Java Junior", "Indra", ("badge", "pendiente"), "14/10/2026"),
    ("2", "Backend Developer Spring Boot", "Accenture", ("badge", "entrevista"), "08/10/2026"),
    ("3", "Analista Programador", "Capgemini", ("badge", "rechazado"), "02/10/2026"),
    ("4", "Desarrollador Full Stack", "NTT Data", ("badge", "aceptado"), "21/09/2026"),
    ("5", "Técnico de Desarrollo de Software", "Minsait", ("badge", "pendiente"), "16/10/2026"),
    ("6", "Desarrollador Java", "Indra", ("badge", "entrevista"), "30/09/2026"),
    ("7", "Programador Junior Java", "Capgemini", ("badge", "rechazado"), "18/09/2026"),
]
NAV = [("Candidaturas", "s1"), ("Empresas", "s4"), ("Entrevistas", "s5"), ("Estadísticas", "s6")]

# =============================================================== PDF 1: PROTOTIPO
FX, FY, FW, FH = 36, 84, 770, 396


def marco_pantalla(L, num, total, titulo, descripcion, activa, cabecera, boton_cab=None, enlace_cab=None, modo=None):
    L.c.bookmarkPage(f"s{num}")
    L.rect(0, 0, L.W, L.H, fill="#EDF0F5")
    logo(L, 36, 22, 30)
    L.texto(76, 38, f"Pantalla {num} de {total} · {titulo}", 13, "Sans-B", TEXT)
    L.texto(76, 54, descripcion, 8.5, "Sans", MUTED)
    L.texto(L.W - 36, 38, "CandiTrack · Prototipo v1.0", 8, "Sans", MUTED, "r")
    L.texto(L.W - 36, 52, EQUIPO, 8, "Sans", MUTED, "r")
    # ventana
    L.rect(FX + 2, FY + 3, FW, FH, fill="#D5DAE3", r=10)
    L.rect(FX, FY, FW, FH, fill=BG, stroke=BORDER, r=10)
    # barra lateral
    L.c.saveState()
    p = L.c.beginPath()
    p.roundRect(FX, L.y(FY + FH), FW, FH, 10)
    L.c.clipPath(p, stroke=0, fill=0)
    L.rect(FX, FY, 140, FH, fill=PRIMARY_DARK)
    L.c.restoreState()
    logo(L, FX + 14, FY + 14, 24, fondo="#FFFFFF", linea=PRIMARY, marca=SECONDARY)
    wordmark(L, FX + 44, FY + 32, 12, "#FFFFFF", "#9CC3F5")
    for i, (nombre, destino) in enumerate(NAV):
        yy = FY + 64 + i * 34
        if nombre == activa:
            L.rect(FX + 8, yy, 124, 26, fill=PRIMARY, r=5)
        L.rect(FX + 17, yy + 8, 10, 10, fill="#9CC3F5" if nombre != activa else "#FFFFFF", r=2)
        L.texto(FX + 34, yy + 17, nombre, 8.5, "Sans-B" if nombre == activa else "Sans", "#FFFFFF")
        L.enlace(FX + 8, yy, 124, 26, destino)
    L.texto(FX + 14, FY + FH - 16, "v1.0", 7, "Sans", "#9CC3F5")
    # cabecera de contenido
    L.rect(FX + 140, FY, FW - 140, 46, fill=SURFACE)
    L.linea(FX + 140, FY + 46, FX + FW, FY + 46, BORDER)
    L.texto(FX + 158, FY + 29, cabecera, 14, "Sans-B", TEXT)
    if boton_cab:
        boton(L, FX + FW - 18 - 140, FY + 11, 140, 24, boton_cab, "primario", enlace_cab)


def leyenda(L, items):
    y0 = FY + FH + 20
    L.texto(FX, y0, "Interacciones del prototipo (los botones y el menú lateral son enlaces navegables en este PDF)",
            8, "Sans-B", PRIMARY_DARK)
    for i, (n, texto) in enumerate(items):
        col, fila = i % 2, i // 2
        xx, yy = FX + col * 385, y0 + 16 + fila * 15
        marcador(L, xx + 7, yy - 3, n)
        L.texto(xx + 20, yy, L.recortar(texto, 360, 7.5), 7.5, "Sans", TEXT)


def pdf_prototipo(ruta):
    L = Lienzo(ruta, (842, 595), "CandiTrack — Prototipo de pantallas v1.0")
    TOTAL = 7
    # ---- portada
    L.rect(0, 0, 842, 595, fill=PRIMARY_DARK)
    logo(L, 70, 110, 96, fondo="#FFFFFF", linea=PRIMARY, marca=SECONDARY)
    wordmark(L, 190, 165, 44, "#FFFFFF", "#9CC3F5")
    L.texto(192, 195, "Prototipo de pantallas · v1.0", 16, "Sans", "#DCE8F7")
    L.texto(192, 222, "Tracker de Candidaturas Laborales", 11, "Sans", "#9CC3F5")
    L.texto(70, 330, "Equipo", 9, "Sans-B", "#9CC3F5")
    for i, n in enumerate(NOMBRES):
        L.texto(70, 352 + i * 20, n, 13, "Sans", "#FFFFFF")
    L.texto(70, 440, "Ejercicio Integrador 1.er Trimestre · Acceso a Datos + Desarrollo de Interfaces", 9, "Sans", "#DCE8F7")
    L.texto(70, 456, "Prototipo interactivo en Figma (enlace en el README del repositorio). Este PDF recoge las pantallas y el flujo.",
            9, "Sans", "#DCE8F7")
    L.pagina()

    # ---- mapa de navegación
    L.c.bookmarkPage("mapa")
    L.rect(0, 0, 842, 595, fill="#EDF0F5")
    logo(L, 36, 22, 30)
    L.texto(76, 38, "Mapa de navegación", 13, "Sans-B", TEXT)
    L.texto(76, 54, "Flujo entre las pantallas del prototipo. Haz clic en una pantalla para saltar a ella.", 8.5, "Sans", MUTED)
    L.texto(806, 38, "CandiTrack · Prototipo v1.0", 8, "Sans", MUTED, "r")
    L.texto(806, 52, EQUIPO, 8, "Sans", MUTED, "r")
    nodos = {
        "s1": (60, 130, "1 · Listado", "con filtros"),
        "s2": (330, 130, "2 · Detalle", "candidatura"),
        "s3": (330, 300, "3 · Formulario", "alta / edición"),
        "s7": (600, 130, "7 · Asistente IA", "diálogo (v3.0)"),
        "s4": (60, 300, "4 · Empresas", "listado + formulario"),
        "s5": (60, 440, "5 · Entrevistas", "listado + formulario"),
        "s6": (600, 300, "6 · Estadísticas", "panel (v3.0)"),
    }
    for clave, (x, y, t1, t2) in nodos.items():
        v3 = clave in ("s6", "s7")
        L.rect(x, y, 180, 62, fill=SURFACE, stroke=SECONDARY if v3 else PRIMARY, r=8, lw=1.6)
        L.texto(x + 90, y + 28, t1, 11, "Sans-B", SECONDARY if v3 else PRIMARY_DARK, "c")
        L.texto(x + 90, y + 46, t2, 8, "Sans", MUTED, "c")
        L.enlace(x, y, 180, 62, clave)

    def flecha(x1, y1, x2, y2, etiqueta="", ex=0, ey=-5):
        L.linea(x1, y1, x2, y2, ACCENT, 1.6)
        import math
        a = math.atan2(y2 - y1, x2 - x1)
        for d in (2.7, -2.7):
            L.linea(x2, y2, x2 - 9 * math.cos(a + d * 0.25 * 1), y2 - 9 * math.sin(a + d * 0.25 * 1), ACCENT, 1.6)
        if etiqueta:
            L.texto((x1 + x2) / 2 + ex, (y1 + y2) / 2 + ey, etiqueta, 7.5, "Sans-B", ACCENT, "c")

    flecha(240, 161, 330, 161, "Ver")
    flecha(330, 175, 240, 175, "Volver", 0, 12)
    flecha(420, 192, 420, 300, "Editar", 24, 0)
    flecha(240, 190, 340, 300, "Nueva", -22, 6)
    flecha(510, 161, 600, 161, "Asistente IA")
    flecha(150, 192, 150, 300, "Menú lateral", -42, 0)
    flecha(150, 362, 150, 440, "Menú lateral", -42, 0)
    L.texto(600, 380, "6 · Estadísticas se abre desde el menú lateral (v3.0).", 7.5, "Sans", MUTED)
    L.texto(60, 540, "Verde: pantallas que se implementan en v3.0. Azul: v1.0 / v2.0. El menú lateral permite saltar entre Candidaturas, Empresas, Entrevistas y Estadísticas desde cualquier pantalla.",
            7.5, "Sans", MUTED)
    L.texto(60, 554, "Otros saltos: Detalle → Empresas («Ver empresa»), Detalle → Entrevistas («+ Añadir entrevista»), Formulario → Listado («Guardar» / «Cancelar»).",
            7.5, "Sans", MUTED)
    L.pagina()

    # ---- 1 listado
    marco_pantalla(L, 1, TOTAL, "Listado de candidaturas", "Tabla con filtros por texto, estado y empresa.", "Candidaturas",
                   "Candidaturas", "+ Nueva candidatura", "s3")
    cx = FX + 158
    campo(L, cx, FY + 62, 190, "Buscar", "Título o empresa…", placeholder=True)
    campo(L, cx + 202, FY + 62, 110, "Estado", "Todos", select=True)
    campo(L, cx + 324, FY + 62, 110, "Empresa", "Todas", select=True)
    campo(L, cx + 446, FY + 62, 80, "Desde", "dd/MM/aaaa", placeholder=True)
    boton(L, cx + 540, FY + 70, 54, 22, "Limpiar", "secundario")
    tabla(L, cx, FY + 108, [32, 200, 92, 90, 76, 90], ["ID", "Título", "Empresa", "Estado", "Fecha", "Acciones"],
          [f + (("acciones",),) for f in FILAS], 24, seleccion=1, link_fila="s2")
    L.texto(cx, FY + 108 + 24 * 8 + 18, "Mostrando 7 de 15 candidaturas", 7.5, "Sans", MUTED)
    marcador(L, FX + FW - 30, FY + 6, 1)
    marcador(L, cx + 596, FY + 62, 2)
    marcador(L, cx - 10, FY + 168, 3)
    marcador(L, cx + 590, FY + 108, 4)
    leyenda(L, [(1, "«+ Nueva candidatura» → Formulario de alta (pantalla 3)"),
                (2, "Filtros: texto, estado, empresa y fecha; «Limpiar» restablece la lista"),
                (3, "Clic en una fila → Detalle de la candidatura (pantalla 2)"),
                (4, "Acciones por fila: Ver (detalle), Editar (formulario), Borrar (confirmación)")])
    L.pagina()

    # ---- 2 detalle
    marco_pantalla(L, 2, TOTAL, "Detalle de candidatura", "Datos de la oferta, empresa, entrevistas y notas.", "Candidaturas",
                   "Backend Developer Spring Boot")
    cx = FX + 158
    badge(L, cx, FY + 56, "entrevista", 8)
    boton(L, cx + 330, FY + 54, 70, 22, "← Volver", "fantasma", "s1")
    boton(L, cx + 404, FY + 54, 66, 22, "Editar", "secundario", "s3")
    boton(L, cx + 474, FY + 54, 88, 22, "✦ Asistente IA", "ia", "s7")
    tarjeta(L, cx, FY + 86, 292, 108, "Datos de la oferta")
    for i, (k, v) in enumerate([("Fecha de postulación", "08/10/2026"), ("Salario", "28.000 €"),
                                ("Enlace", "accenture.com/careers/backend"), ("Estado", "entrevista ▾")]):
        L.texto(cx + 12, FY + 38 + 86 + i * 16, k, 7.5, "Sans-B", MUTED)
        L.texto(cx + 118, FY + 38 + 86 + i * 16, L.recortar(v, 165, 7.5), 7.5, "Sans", TEXT)
    tarjeta(L, cx + 304, FY + 86, 258, 108, "Empresa")
    for i, (k, v) in enumerate([("Nombre", "Accenture"), ("Sector", "Consultoría"), ("Web", "www.accenture.com"),
                                ("Registrada", "01/09/2026")]):
        L.texto(cx + 316, FY + 38 + 86 + i * 16, k, 7.5, "Sans-B", MUTED)
        L.texto(cx + 380, FY + 38 + 86 + i * 16, v, 7.5, "Sans", TEXT)
    L.texto(cx + 540, FY + 66 + 86, "Ver empresa →", 7.5, "Sans-B", PRIMARY, "r")
    L.enlace(cx + 470, FY + 148, 80, 14, "s4")
    tarjeta(L, cx, FY + 206, 292, 134, "Entrevistas")
    tabla(L, cx + 10, FY + 228, [72, 80, 118], ["Fecha", "Tipo", "Resultado"],
          [("20/10/2026", "telefónica", "positivo"), ("28/10/2026", "técnica", "pendiente")], 20)
    boton(L, cx + 10, FY + 306, 120, 22, "+ Añadir entrevista", "secundario", "s5")
    tarjeta(L, cx + 304, FY + 206, 258, 134, "Notas")
    for i, (f, t) in enumerate([("08/10", "Enviado desde el portal de empleo."), ("20/10", "Primera llamada con RRHH. Buen feeling."),
                                ("21/10", "Preparar caso práctico de Spring.")]):
        L.texto(cx + 316, FY + 242 + i * 17, f, 7.5, "Sans-B", PRIMARY)
        L.texto(cx + 352, FY + 242 + i * 17, L.recortar(t, 195, 7.5), 7.5, "Sans", TEXT)
    campo(L, cx + 316, FY + 292, 150, "Nueva nota", "Escribe una nota…", h=20, placeholder=True)
    boton(L, cx + 474, FY + 296, 76, 20, "Añadir", "primario", None, 7.5)
    marcador(L, cx + 570, FY + 52, 1)
    marcador(L, cx + 437, FY + 54, 2)
    marcador(L, cx + 548, FY + 144, 3)
    marcador(L, cx + 140, FY + 300, 4)
    leyenda(L, [(1, "«Asistente IA» → Diálogo con la carta de presentación (pantalla 7)"),
                (2, "«Editar» → Formulario de edición (pantalla 3); «Volver» → Listado (pantalla 1)"),
                (3, "«Ver empresa» → Gestión de empresas (pantalla 4)"),
                (4, "«+ Añadir entrevista» → Gestión de entrevistas (pantalla 5)")])
    L.pagina()

    # ---- 3 formulario
    marco_pantalla(L, 3, TOTAL, "Formulario de alta / edición", "Campos obligatorios marcados con *. Validación en línea.", "Candidaturas",
                   "Nueva candidatura")
    cx = FX + 158
    tarjeta(L, cx, FY + 60, 580, 318)
    campo(L, cx + 16, FY + 84, 270, "Título del puesto", "", obligatorio=True, estado="error")
    L.texto(cx + 16, FY + 126, "El título es obligatorio.", 7, "Sans-B", DANGER)
    campo(L, cx + 300, FY + 84, 264, "Empresa", "Seleccionar empresa…", obligatorio=True, select=True, placeholder=True)
    campo(L, cx + 16, FY + 146, 130, "Estado", "pendiente", obligatorio=True, select=True)
    campo(L, cx + 160, FY + 146, 126, "Fecha de postulación", "26/10/2026", obligatorio=True, estado="foco")
    campo(L, cx + 300, FY + 146, 130, "Salario (€)", "", placeholder=True)
    campo(L, cx + 444, FY + 146, 120, "Enlace", "https://…", placeholder=True)
    campo(L, cx + 16, FY + 208, 548, "Descripción", "", h=44)
    campo(L, cx + 16, FY + 280, 548, "Notas", "", h=22)
    boton(L, cx + 16, FY + 334, 90, 26, "Guardar", "primario", "s1")
    boton(L, cx + 116, FY + 334, 90, 26, "Cancelar", "secundario", "s1")
    L.texto(cx + 564, FY + 352, "* Campo obligatorio", 7.5, "Sans", MUTED, "r")
    marcador(L, cx + 286, FY + 88, 1)
    marcador(L, cx + 286, FY + 150, 2)
    marcador(L, cx + 106, FY + 334, 3)
    marcador(L, FX + 158 + 172, FY + 22, 4)
    leyenda(L, [(1, "Campo con error: borde rojo + mensaje de texto (no solo color)"),
                (2, "Campo con foco: borde azul grueso; navegable con Tab"),
                (3, "«Guardar» valida y vuelve al Listado (pantalla 1); «Cancelar» descarta"),
                (4, "El mismo formulario sirve para alta y edición (el título cambia)")])
    L.pagina()

    # ---- 4 empresas
    marco_pantalla(L, 4, TOTAL, "Gestión de empresas", "Listado a la izquierda y formulario de alta/edición a la derecha.", "Empresas",
                   "Empresas", "+ Nueva empresa")
    cx = FX + 158
    tabla(L, cx, FY + 62, [92, 84, 64], ["Nombre", "Sector", "Ofertas"],
          [("Indra", "Tecnología", "3"), ("Accenture", "Consultoría", "3"), ("Capgemini", "Consultoría", "3"),
           ("NTT Data", "Tecnología", "3"), ("Minsait", "Tecnología", "3")], 26, seleccion=1)
    tarjeta(L, cx + 262, FY + 62, 318, 266, "Editar empresa")
    campo(L, cx + 278, FY + 100, 286, "Nombre", "Accenture", obligatorio=True)
    campo(L, cx + 278, FY + 148, 286, "Sector", "Consultoría", obligatorio=True, select=True)
    campo(L, cx + 278, FY + 196, 286, "Web", "https://www.accenture.com")
    campo(L, cx + 278, FY + 244, 130, "Fecha de registro", "01/09/2026", estado="deshabilitado")
    boton(L, cx + 278, FY + 290, 80, 22, "Guardar", "primario")
    boton(L, cx + 366, FY + 290, 72, 22, "Nueva", "secundario")
    boton(L, cx + 482, FY + 290, 82, 22, "Eliminar", "peligro")
    marcador(L, cx - 10, FY + 114, 1)
    marcador(L, cx + 574, FY + 290, 2)
    marcador(L, FX + 134, FY + 80, 3)
    marcador(L, FX + FW - 12, FY + 8, 4)
    leyenda(L, [(1, "Seleccionar una fila carga la empresa en el formulario"),
                (2, "«Eliminar» pide confirmación y avisa si la empresa tiene ofertas"),
                (3, "Menú lateral: Candidaturas / Entrevistas / Estadísticas"),
                (4, "«+ Nueva empresa» limpia el formulario para un alta")])
    L.pagina()

    # ---- 5 entrevistas
    marco_pantalla(L, 5, TOTAL, "Gestión de entrevistas", "Entrevistas de todas las ofertas, con formulario de alta/edición.", "Entrevistas",
                   "Entrevistas", "+ Nueva entrevista")
    cx = FX + 158
    tabla(L, cx, FY + 62, [118, 70, 80, 86], ["Oferta", "Fecha", "Tipo", "Resultado"],
          [("Backend Spring Boot", "20/10/2026", "telefónica", ("badge", "aceptado", "positivo")),
           ("Backend Spring Boot", "28/10/2026", "técnica", ("badge", "pendiente", "pendiente")),
           ("Desarrollador Java", "05/10/2026", "online", ("badge", "aceptado", "positivo")),
           ("Java Microservicios", "30/10/2026", "presencial", ("badge", "pendiente", "pendiente")),
           ("Analista Funcional", "03/09/2026", "técnica", ("badge", "rechazado", "negativo"))], 26, seleccion=1)
    tarjeta(L, cx + 370, FY + 62, 210, 318, "Entrevista")
    campo(L, cx + 384, FY + 100, 182, "Oferta", "Backend Spring Boot", obligatorio=True, select=True)
    campo(L, cx + 384, FY + 146, 84, "Fecha", "28/10/2026", obligatorio=True)
    campo(L, cx + 478, FY + 146, 88, "Tipo", "técnica", obligatorio=True, select=True)
    campo(L, cx + 384, FY + 192, 182, "Resultado", "pendiente", select=True)
    campo(L, cx + 384, FY + 238, 182, "Notas", "Prueba técnica", h=40)
    boton(L, cx + 384, FY + 336, 80, 22, "Guardar", "primario")
    boton(L, cx + 472, FY + 336, 94, 22, "Eliminar", "peligro")
    marcador(L, cx - 10, FY + 114, 1)
    marcador(L, cx + 574, FY + 336, 2)
    marcador(L, cx + 384 - 4, FY + 336, 4)
    leyenda(L, [(1, "Seleccionar una fila carga la entrevista en el formulario"),
                (2, "Resultado con badge de color + texto: pendiente / positivo / negativo"),
                (3, "Desde el Detalle (pantalla 2) se llega con la oferta ya seleccionada"),
                (4, "«Guardar» actualiza la tabla sin salir de la pantalla")])
    L.pagina()

    # ---- 6 estadísticas
    marco_pantalla(L, 6, TOTAL, "Panel de estadísticas (v3.0)", "Indicadores, gráficos y tabla resumen sobre las candidaturas.", "Estadísticas",
                   "Estadísticas")
    cx = FX + 158
    for i, (valor, etiqueta, color) in enumerate([("15", "Candidaturas totales", PRIMARY), ("20 %", "Porcentaje de éxito", SUCCESS),
                                                  ("4", "En fase de entrevista", "#1E40AF")]):
        xx = cx + i * 196
        tarjeta(L, xx, FY + 60, 184, 62)
        L.texto(xx + 14, FY + 98, valor, 22, "Sans-B", color)
        L.texto(xx + 14, FY + 114, etiqueta, 7.5, "Sans", MUTED)
    tarjeta(L, cx, FY + 134, 290, 200, "Ofertas por empresa")
    for i, (n, v) in enumerate([("Indra", 3), ("Accenture", 3), ("Capgemini", 3), ("NTT Data", 3), ("Minsait", 3)]):
        yy = FY + 154 + i * 34
        L.texto(cx + 14, yy + 14, n, 7.5, "Sans", TEXT)
        L.rect(cx + 74, yy + 3, v * 62, 16, fill=PRIMARY, r=3)
        L.texto(cx + 80 + v * 62, yy + 15, str(v), 7.5, "Sans-B", PRIMARY_DARK)
    tarjeta(L, cx + 302, FY + 134, 278, 98, "Ofertas por mes de postulación")
    for i, (m, v) in enumerate([("ago", 2), ("sep", 5), ("oct", 8)]):
        xx = cx + 330 + i * 76
        h = v * 6.4
        L.rect(xx, FY + 215 - h, 40, h, fill=SECONDARY, r=3)
        L.texto(xx + 20, FY + 227, m, 7.5, "Sans", TEXT, "c")
        L.texto(xx + 20, FY + 210 - h, str(v), 7.5, "Sans-B", SECONDARY, "c")
    tarjeta(L, cx + 302, FY + 244, 278, 90, "Porcentaje por estado")
    for i, (e, n) in enumerate([("pendiente", "4 · 26,7 %"), ("entrevista", "4 · 26,7 %"),
                                ("rechazado", "4 · 26,7 %"), ("aceptado", "3 · 20,0 %")]):
        col, fila = i % 2, i // 2
        badge(L, cx + 316 + col * 134, FY + 270 + fila * 28, e)
        L.texto(cx + 316 + col * 134 + 66, FY + 281 + fila * 28, n, 7.5, "Sans-B", TEXT)
    marcador(L, cx + 4, FY + 56, 1)
    marcador(L, cx + 296, FY + 138, 2)
    marcador(L, cx + 296, FY + 248, 3)
    leyenda(L, [(1, "Tarjetas resumen: total, % de éxito (aceptadas / totales) y entrevistas"),
                (2, "Gráficos de barras (BarChart de JavaFX) con valores visibles"),
                (3, "Tabla resumen por estado: color + texto + recuento + porcentaje"),
                (4, "Datos calculados a partir del CSV de prueba (15 candidaturas)")])
    L.pagina()

    # ---- 7 asistente IA
    marco_pantalla(L, 7, TOTAL, "Asistente IA (v3.0)", "Diálogo modal con la carta de presentación generada por el modelo local.", "Candidaturas",
                   "Backend Developer Spring Boot")
    tarjeta(L, FX + 158, FY + 60, 280, 150)
    tarjeta(L, FX + 450, FY + 60, 280, 150)
    tarjeta(L, FX + 158, FY + 222, 572, 150)
    L.c.saveState()
    L.c.setFillAlpha(0.55)
    L.rect(FX + 140, FY + 46, FW - 140, FH - 46, fill="#1B2430")
    L.c.restoreState()
    dx, dy, dw, dh = FX + 190, FY + 66, 480, 316
    L.rect(dx, dy, dw, dh, fill=SURFACE, r=10)
    L.texto(dx + 18, dy + 28, "✦ Asistente IA · Carta de presentación", 12, "Sans-B", SECONDARY)
    L.texto(dx + 18, dy + 44, "Backend Developer Spring Boot · Accenture", 8, "Sans", MUTED)
    L.texto(dx + dw - 22, dy + 28, "✕", 12, "Sans-B", MUTED, "c")
    L.enlace(dx + dw - 36, dy + 12, 28, 24, "s2")
    L.rect(dx + 18, dy + 56, dw - 36, 190, fill="#F8FAFC", stroke=BORDER, r=6)
    carta = ("Estimado equipo de selección de Accenture:  Me dirijo a vosotros para presentar mi candidatura al puesto de "
             "Backend Developer Spring Boot. Mi formación en desarrollo de aplicaciones multiplataforma y mis proyectos con "
             "Java, Maven y bases de datos relacionales me han permitido adquirir una base sólida para incorporarme a un "
             "equipo técnico. Me motiva especialmente trabajar en proyectos de consultoría donde pueda seguir aprendiendo. "
             "Quedo a vuestra disposición para ampliar información en una entrevista.  Un cordial saludo.")
    L.parrafo(dx + 30, dy + 78, carta, dw - 60, 8.5)
    L.texto(dx + 18, dy + 264, "Texto de ejemplo. El texto real lo genera el modelo local (Ollama) en la v3.0.", 7, "Sans", MUTED)
    boton(L, dx + 18, dy + 276, 88, 26, "Copiar texto", "primario")
    boton(L, dx + 114, dy + 276, 88, 26, "Regenerar", "secundario")
    boton(L, dx + dw - 100, dy + 276, 82, 26, "Cerrar", "secundario", "s2")
    marcador(L, dx + dw - 8, dy + 8, 1)
    marcador(L, dx + 14, dy + 52, 2)
    marcador(L, dx + 200, dy + 274, 3)
    leyenda(L, [(1, "«✕» o «Cerrar» → vuelve al Detalle (pantalla 2)"),
                (2, "Texto seleccionable y copiable para pegarlo en la plataforma de empleo"),
                (3, "«Regenerar» vuelve a llamar al modelo; mientras tanto se muestra «Generando…»"),
                (4, "Si Ollama no está disponible: mensaje claro de error sin bloquear la aplicación")])
    L.pagina()
    L.guardar()


# =============================================================== PDF 2: MANUAL DE ESTILO
PW, PH = 595, 842
M = 48


def cabecera_pag(L, n, titulo):
    L.rect(0, 0, PW, PH, fill="#FFFFFF")
    L.rect(0, 0, PW, 6, fill=PRIMARY)
    logo(L, M, 26, 22)
    wordmark(L, M + 30, 42, 12)
    L.texto(PW - M, 38, "Manual de estilo · v1.0", 8, "Sans", MUTED, "r")
    L.texto(M, 86, titulo, 20, "Sans-B", PRIMARY_DARK)
    L.linea(M, 96, PW - M, 96, PRIMARY, 1.6)
    L.linea(M, PH - 40, PW - M, PH - 40, BORDER, 0.6)
    L.texto(M, PH - 26, EQUIPO, 7.5, "Sans", MUTED)
    L.texto(PW - M, PH - 26, f"Página {n}", 7.5, "Sans", MUTED, "r")


def subtitulo(L, y, texto):
    L.texto(M, y, texto, 11, "Sans-B", PRIMARY)
    return y + 8


def pdf_manual(ruta):
    L = Lienzo(ruta, (PW, PH), "CandiTrack — Manual de estilo v1.0")
    # ---- portada
    L.rect(0, 0, PW, PH, fill=PRIMARY_DARK)
    logo(L, M, 180, 110, fondo="#FFFFFF", linea=PRIMARY, marca=SECONDARY)
    wordmark(L, M, 360, 48, "#FFFFFF", "#9CC3F5")
    L.texto(M, 396, "Manual de estilo", 22, "Sans", "#DCE8F7")
    L.texto(M, 424, "Versión 1.0 · Tracker de Candidaturas Laborales", 11, "Sans", "#9CC3F5")
    L.texto(M, 640, "Equipo", 9, "Sans-B", "#9CC3F5")
    for i, n in enumerate(NOMBRES):
        L.texto(M, 662 + i * 20, n, 13, "Sans", "#FFFFFF")
    L.texto(M, 780, "Ejercicio Integrador 1.er Trimestre · Acceso a Datos + Desarrollo de Interfaces", 8.5, "Sans", "#DCE8F7")
    L.pagina()

    # ---- 1 introducción y logo
    cabecera_pag(L, 2, "1. Identidad y logo")
    y = L.parrafo(M, 124, "CandiTrack ayuda a llevar el seguimiento de candidaturas laborales. Su imagen debe transmitir "
                  "orden, confianza y claridad: una interfaz sobria, con un azul corporativo como color protagonista y un "
                  "verde azulado para las acciones de apoyo. Este manual define los elementos visuales que se aplicarán "
                  "en las pantallas JavaFX (v2.0 y v3.0) mediante una hoja de estilos CSS.", PW - 2 * M, 9.5)
    y = subtitulo(L, y + 14, "Principios de diseño") + 14
    for t in ("Claridad: una acción principal por pantalla y jerarquía visual evidente.",
              "Consistencia: los mismos componentes con el mismo aspecto en toda la aplicación.",
              "Accesibilidad: contraste mínimo 4,5:1 en texto, foco visible y nunca usar solo el color para informar.",
              "Eficiencia: tablas, filtros y atajos de teclado pensados para uso repetido."):
        L.circulo(M + 4, y - 3, 2.2, fill=PRIMARY)
        y = L.parrafo(M + 14, y, t, PW - 2 * M - 14, 9.5)
    y = subtitulo(L, y + 14, "Logo e isotipo") + 6
    tarjeta(L, M, y, 230, 150)
    logo(L, M + 20, y + 32, 80)
    wordmark(L, M + 108, y + 86, 16)
    L.texto(M + 110, y + 102, "Tracker de candidaturas", 7.5, "Sans", MUTED)
    L.texto(M + 115, y + 140, "Versión principal", 7.5, "Sans-B", MUTED)
    L.rect(M + 250, y, 134, 150, fill=PRIMARY_DARK, r=8)
    logo(L, M + 282, y + 30, 70, fondo="#FFFFFF", linea=PRIMARY, marca=SECONDARY)
    wordmark(L, M + 270, y + 124, 15, "#FFFFFF", "#9CC3F5")
    L.texto(M + 317, y + 144, "Sobre fondo oscuro", 7, "Sans-B", "#9CC3F5", "c")
    L.rect(M + 396, y, 55, 150, fill=SURFACE, stroke=BORDER, r=8)
    logo(L, M + 406, y + 50, 35, fondo="#374151", linea="#FFFFFF", marca="#6B7280")
    L.texto(M + 423, y + 110, "Mono", 7, "Sans-B", MUTED, "c")
    L.texto(M + 423, y + 120, "(1 tinta)", 7, "Sans", MUTED, "c")
    y += 170
    L.parrafo(M, y, "El isotipo representa una lista de candidaturas con una marca de verificación que indica el "
              "seguimiento de su estado. Zona de seguridad: un margen mínimo equivalente a un tercio del lado del isotipo "
              "en todos los lados. Tamaño mínimo: 24 px en pantalla. No deformar, rotar ni cambiar los colores fuera de las "
              "versiones mostradas.", PW - 2 * M, 9)
    L.pagina()

    # ---- 2 paleta
    cabecera_pag(L, 3, "2. Paleta de colores")
    y = 124
    grupos = [
        ("Colores de marca", [("Principal", PRIMARY, "Botones primarios, enlaces, cabeceras"),
                              ("Principal oscuro", PRIMARY_DARK, "Barra lateral, títulos"),
                              ("Secundario", SECONDARY, "Acciones de apoyo, asistente IA, gráficos"),
                              ("Acento", ACCENT, "Solo marcadores en prototipo y avisos puntuales")]),
        ("Fondos y texto", [("Fondo", BG, "Fondo de la aplicación"),
                            ("Superficie", SURFACE, "Tarjetas, tablas, diálogos"),
                            ("Texto", TEXT, "Texto principal"),
                            ("Texto atenuado", MUTED, "Etiquetas, ayudas"),
                            ("Borde", BORDER, "Líneas y contornos"),
                            ("Principal claro", PRIMARY_LIGHT, "Fila seleccionada, cabecera de tabla")]),
        ("Feedback", [("Error / peligro", DANGER, "Errores, botón Eliminar"),
                      ("Éxito", SUCCESS, "Confirmaciones, % de éxito")]),
    ]
    for nombre, items in grupos:
        y = subtitulo(L, y, nombre) + 6
        for i, (n, hexa, uso) in enumerate(items):
            col, fila = i % 2, i // 2
            x = M + col * 250
            yy = y + fila * 50
            L.rect(x, yy, 44, 40, fill=hexa, stroke=BORDER, r=6)
            L.texto(x + 54, yy + 14, n, 9, "Sans-B", TEXT)
            r, g, b = (int(hexa[i:i + 2], 16) for i in (1, 3, 5))
            L.texto(x + 54, yy + 26, f"{hexa.upper()}  ·  RGB {r}, {g}, {b}", 7.5, "Mono", MUTED)
            L.texto(x + 54, yy + 37, L.recortar(uso, 188, 7), 7, "Sans", MUTED)
        y += ((len(items) + 1) // 2) * 50 + 8
    y = subtitulo(L, y, "Colores de estado (badge: fondo + texto, siempre con la palabra escrita)") + 8
    for i, (e, (fondo, color)) in enumerate(ESTADOS.items()):
        x = M + i * 124
        L.rect(x, y, 112, 30, fill=fondo, r=15)
        L.texto(x + 56, y + 19, e, 9, "Sans-B", color, "c")
        L.texto(x, y + 44, f"Fondo {fondo.upper()}", 7, "Mono", MUTED)
        L.texto(x, y + 54, f"Texto {color.upper()}", 7, "Mono", MUTED)
        L.texto(x, y + 64, f"Contraste {contraste(fondo, color):.1f}:1", 7, "Sans-B", SUCCESS)
    y += 86
    y = subtitulo(L, y, "Contrastes verificados (WCAG 2.1, mínimo AA 4,5:1 para texto)") + 8
    pares = [("Texto sobre fondo", TEXT, BG), ("Texto atenuado sobre superficie", MUTED, SURFACE),
             ("Blanco sobre principal", "#FFFFFF", PRIMARY), ("Blanco sobre secundario", "#FFFFFF", SECONDARY),
             ("Blanco sobre peligro", "#FFFFFF", DANGER), ("Principal sobre superficie (enlaces)", PRIMARY, SURFACE)]
    for i, (n, fg, bg) in enumerate(pares):
        col, fila = i % 2, i // 2
        x, yy = M + col * 250, y + fila * 22
        L.rect(x, yy, 26, 16, fill=bg, stroke=BORDER, r=3)
        L.texto(x + 13, yy + 12, "Aa", 8, "Sans-B", fg, "c")
        L.texto(x + 34, yy + 12, f"{n}: {contraste(fg, bg):.1f}:1  ✓", 8, "Sans", TEXT)
    L.pagina()

    # ---- 3 tipografía
    cabecera_pag(L, 4, "3. Tipografía")
    y = L.parrafo(M, 124, "Se utiliza la familia DejaVu (licencia libre, incluida en la mayoría de sistemas y con soporte completo "
                  "de caracteres en español). En JavaFX se declara con -fx-font-family y se mantiene una alternativa genérica "
                  "(sans-serif / monospace) por si no estuviera instalada.", PW - 2 * M, 9.5)
    y += 8
    muestras = [("Títulos", "DejaVu Sans Bold", "Sans-B", "Seguimiento de candidaturas", 22,
                 "Pantallas, cabeceras de tarjeta y diálogos"),
                ("Cuerpo", "DejaVu Sans Regular", "Sans", "Ordena tus candidaturas y no pierdas el hilo de cada proceso.", 12,
                 "Texto general, etiquetas y celdas de tabla"),
                ("Monoespaciado", "DejaVu Sans Mono", "Mono", "-listar --estado entrevista --empresa Indra", 11,
                 "Comandos, IDs, fechas técnicas y salida de consola")]
    for n, fam, f, ejemplo, size, uso in muestras:
        tarjeta(L, M, y, PW - 2 * M, 84)
        L.texto(M + 14, y + 20, f"{n} · {fam}", 9, "Sans-B", PRIMARY)
        L.texto(M + 14, y + 50, L.recortar(ejemplo, PW - 2 * M - 28, size, f), size, f, TEXT)
        L.texto(M + 14, y + 72, uso, 8, "Sans", MUTED)
        y += 96
    y = subtitulo(L, y + 6, "Escala tipográfica") + 8
    escala = [("Título de pantalla (H1)", 18, "Sans-B", "Candidaturas", "-fx-font-size: 18px; -fx-font-weight: bold"),
              ("Título de tarjeta (H2)", 14, "Sans-B", "Datos de la oferta", "-fx-font-size: 14px; -fx-font-weight: bold"),
              ("Subtítulo (H3)", 12, "Sans-B", "Entrevistas", "-fx-font-size: 12px; -fx-font-weight: bold"),
              ("Cuerpo", 12, "Sans", "Backend Developer", "-fx-font-size: 12px"),
              ("Texto pequeño / ayuda", 10, "Sans", "Campo obligatorio", "-fx-font-size: 10px"),
              ("Monoespaciado", 11, "Mono", "13;Dev Java;Indra", "-fx-font-family: monospace")]
    for i, (n, s, f, ej, css) in enumerate(escala):
        yy = y + i * 26
        L.linea(M, yy + 22, PW - M, yy + 22, BORDER, 0.5)
        L.texto(M, yy + 15, n, 8, "Sans", MUTED)
        L.texto(M + 150, yy + 16, L.recortar(ej, 170, s, f), s, f, TEXT)
        L.texto(M + 335, yy + 15, css, 6.8, "Mono", MUTED)
    L.pagina()

    # ---- 4 espaciado, iconos
    cabecera_pag(L, 5, "4. Espaciado, formas e iconografía")
    y = subtitulo(L, 124, "Escala de espaciado (múltiplos de 4 px)") + 10
    for i, v in enumerate((4, 8, 12, 16, 24, 32)):
        x = M + i * 84
        L.rect(x, y, v, v, fill=PRIMARY, r=1)
        L.texto(x, y + 48, f"{v} px", 8, "Sans-B", TEXT)
    y += 76
    y = subtitulo(L, y, "Radios, bordes y sombras") + 10
    for i, (n, r, extra) in enumerate([("Controles · radio 4 px", 4, ""), ("Botones · radio 5 px", 5, ""), ("Tarjetas · radio 8 px", 8, "sombra")]):
        x = M + i * 165
        if extra:
            L.rect(x + 2, y + 3, 120, 50, fill="#E6E9EF", r=r)
        L.rect(x, y, 120, 50, fill=SURFACE, stroke=BORDER, r=r)
        L.texto(x, y + 70, n, 8, "Sans-B", TEXT)
    y += 94
    y = subtitulo(L, y, "Iconografía") + 8
    y = L.parrafo(M, y + 6, "Iconos lineales simples de trazo 1,5 px, tamaño 16 px en tablas y 20 px en botones y menú. "
                  "Se recomienda Material Symbols (Outlined) o Ikonli (paquete Material Design) para JavaFX. Siempre "
                  "acompañados de texto o de una descripción accesible (tooltip).", PW - 2 * M, 9.5)
    iconos = ["Añadir", "Editar", "Eliminar", "Ver", "Buscar", "Filtrar", "Estadísticas", "Asistente IA"]
    y += 6
    for i, n in enumerate(iconos):
        x = M + i * 64
        L.rect(x, y, 48, 48, fill=SURFACE, stroke=BORDER, r=8)
        cx, cy = x + 24, y + 24
        c = L.c
        c.setStrokeColor(hc(PRIMARY))
        c.setLineWidth(1.8)
        c.setLineCap(1)
        yy = lambda v: L.y(v)
        if n == "Añadir":
            c.line(cx - 9, yy(cy), cx + 9, yy(cy)); c.line(cx, yy(cy - 9), cx, yy(cy + 9))
        elif n == "Editar":
            c.line(cx - 9, yy(cy + 9), cx + 6, yy(cy - 6)); c.line(cx - 9, yy(cy + 9), cx - 9, yy(cy + 4))
            c.line(cx - 9, yy(cy + 9), cx - 4, yy(cy + 9))
        elif n == "Eliminar":
            c.rect(cx - 7, yy(cy + 9), 14, 14, stroke=1, fill=0); c.line(cx - 10, yy(cy - 6), cx + 10, yy(cy - 6))
            c.line(cx - 3, yy(cy - 6), cx - 3, yy(cy - 9)); c.line(cx + 3, yy(cy - 6), cx + 3, yy(cy - 9))
        elif n == "Ver":
            c.ellipse(cx - 11, yy(cy + 6), cx + 11, yy(cy - 6), stroke=1, fill=0); c.circle(cx, yy(cy), 3, stroke=1, fill=0)
        elif n == "Buscar":
            c.circle(cx - 2, yy(cy - 2), 7, stroke=1, fill=0); c.line(cx + 3, yy(cy + 3), cx + 10, yy(cy + 10))
        elif n == "Filtrar":
            c.line(cx - 10, yy(cy - 8), cx + 10, yy(cy - 8)); c.line(cx - 10, yy(cy - 8), cx - 2, yy(cy + 1))
            c.line(cx + 10, yy(cy - 8), cx + 2, yy(cy + 1)); c.line(cx - 2, yy(cy + 1), cx - 2, yy(cy + 9))
            c.line(cx + 2, yy(cy + 1), cx + 2, yy(cy + 6))
        elif n == "Estadísticas":
            c.rect(cx - 10, yy(cy + 9), 5, 8, stroke=1, fill=0); c.rect(cx - 2, yy(cy + 9), 5, 14, stroke=1, fill=0)
            c.rect(cx + 6, yy(cy + 9), 5, 19, stroke=1, fill=0)
        else:
            c.setStrokeColor(hc(SECONDARY))
            c.line(cx, yy(cy - 10), cx, yy(cy + 10)); c.line(cx - 10, yy(cy), cx + 10, yy(cy))
            c.line(cx - 6, yy(cy - 6), cx + 6, yy(cy + 6)); c.line(cx - 6, yy(cy + 6), cx + 6, yy(cy - 6))
        c.setLineCap(0)
        L.texto(x + 24, y + 62, n, 7, "Sans", MUTED, "c")
    L.pagina()

    # ---- 5 botones y formularios
    cabecera_pag(L, 6, "5. Botones y formularios")
    y = subtitulo(L, 124, "Botones") + 10
    L.texto(M, y + 6, "Variante", 8, "Sans-B", MUTED)
    L.texto(M + 150, y + 6, "Normal", 8, "Sans-B", MUTED)
    L.texto(M + 270, y + 6, "Foco (teclado)", 8, "Sans-B", MUTED)
    L.texto(M + 390, y + 6, "Uso", 8, "Sans-B", MUTED)
    variantes = [("Primario", "primario", "Acción principal (una por pantalla)"),
                 ("Secundario", "secundario", "Acciones alternativas, Cancelar"),
                 ("Peligro", "peligro", "Eliminar (con confirmación)"),
                 ("Asistente IA", "ia", "Acciones de IA"),
                 ("Texto", "fantasma", "Navegación: Volver"),
                 ("Deshabilitado", "deshabilitado", "Acción no disponible")]
    y += 14
    for i, (n, est, uso) in enumerate(variantes):
        yy = y + i * 38
        L.texto(M, yy + 18, n, 9, "Sans-B", TEXT)
        boton(L, M + 150, yy + 2, 100, 28, n if n != "Texto" else "← Volver", est, size=9)
        L.rect(M + 266, yy - 1, 108, 34, stroke=ACCENT, r=7, lw=2)
        boton(L, M + 270, yy + 2, 100, 28, n if n != "Texto" else "← Volver", est, size=9)
        L.texto(M + 390, yy + 18, L.recortar(uso, 150, 7.5), 7.5, "Sans", MUTED)
    y += 6 * 38 + 6
    L.parrafo(M, y, "Altura mínima 32 px, padding horizontal 16 px, texto en negrita 12 px. El foco se muestra con un contorno visible "
              "de 2 px además del color.", PW - 2 * M, 8.5, color=MUTED)
    y = subtitulo(L, y + 34, "Campos de formulario") + 8
    campo(L, M, y + 4, 150, "Normal", "Valor", obligatorio=False)
    campo(L, M + 170, y + 4, 150, "Foco", "Valor", estado="foco")
    campo(L, M + 340, y + 4, 150, "Error", "", estado="error", obligatorio=True)
    L.texto(M + 340, y + 48, "El título es obligatorio.", 7.5, "Sans-B", DANGER)
    campo(L, M, y + 66, 150, "Deshabilitado", "No editable", estado="deshabilitado")
    campo(L, M + 170, y + 66, 150, "Desplegable", "pendiente", select=True)
    L.rect(M + 340, y + 78, 14, 14, fill=PRIMARY, r=3)
    L.texto(M + 347, y + 89, "✓", 9, "Sans-B", "#FFFFFF", "c")
    L.texto(M + 362, y + 89, "Casilla marcada", 8.5, "Sans", TEXT)
    L.parrafo(M, y + 124, "Cada campo lleva una etiqueta visible sobre él (nunca solo placeholder). Los obligatorios se marcan con *. "
              "Los errores se indican con borde rojo y un mensaje de texto junto al campo.", PW - 2 * M, 8.5, color=MUTED)
    L.pagina()

    # ---- 6 tablas, tarjetas, navegación
    cabecera_pag(L, 7, "6. Tablas, tarjetas y navegación")
    y = subtitulo(L, 124, "Tablas") + 8
    tabla(L, M, y, [34, 170, 80, 84, 76, 50], ["ID", "Título", "Empresa", "Estado", "Fecha", ""],
          [FILAS[0] + ("",), FILAS[1] + ("",), FILAS[2] + ("",)], 22, seleccion=1)
    y += 22 * 4 + 14
    L.parrafo(M, y, "Cabecera en principal claro con texto principal oscuro; filas alternas blanco/gris muy claro; fila seleccionada en "
              "principal claro. Estados siempre como badge con texto. Fecha en formato dd/MM/yyyy. Columnas ordenables y alto de fila 24 px.",
              PW - 2 * M, 8.5, color=MUTED)
    y = subtitulo(L, y + 54, "Tarjetas") + 8
    tarjeta(L, M, y, 230, 96, "Datos de la oferta")
    L.texto(M + 12, y + 40, "Fecha de postulación", 7.5, "Sans-B", MUTED)
    L.texto(M + 120, y + 40, "08/10/2026", 7.5, "Sans", TEXT)
    L.texto(M + 12, y + 58, "Salario", 7.5, "Sans-B", MUTED)
    L.texto(M + 120, y + 58, "28.000 €", 7.5, "Sans", TEXT)
    tarjeta(L, M + 250, y, 100, 96)
    L.texto(M + 266, y + 50, "20 %", 20, "Sans-B", SUCCESS)
    L.texto(M + 266, y + 72, "Éxito", 8, "Sans", MUTED)
    L.parrafo(M + 372, y + 12, "Fondo blanco, borde 1 px, radio 8 px y sombra suave. Título en principal oscuro 14 px.", PW - 2 * M - 372, 8, color=MUTED)
    y += 120
    y = subtitulo(L, y, "Navegación (menú lateral)") + 8
    L.rect(M, y, 150, 150, fill=PRIMARY_DARK, r=8)
    logo(L, M + 12, y + 10, 22, fondo="#FFFFFF", linea=PRIMARY, marca=SECONDARY)
    wordmark(L, M + 40, y + 27, 11, "#FFFFFF", "#9CC3F5")
    for i, n in enumerate(["Candidaturas", "Empresas", "Entrevistas", "Estadísticas"]):
        yy = y + 46 + i * 24
        if i == 0:
            L.rect(M + 8, yy, 134, 22, fill=PRIMARY, r=5)
        L.rect(M + 16, yy + 6, 9, 9, fill="#9CC3F5" if i else "#FFFFFF", r=2)
        L.texto(M + 32, yy + 15, n, 8, "Sans-B" if i == 0 else "Sans", "#FFFFFF")
    L.parrafo(M + 175, y + 8, "Menú lateral fijo de 220 px en la aplicación final (140 px en el prototipo), fondo principal oscuro. "
              "El elemento activo se resalta con fondo principal y texto en negrita. Cada entrada lleva icono y texto. "
              "Todo el menú es accesible con teclado (Tab y Enter). Los diálogos son modales con fondo atenuado y un botón "
              "de cierre visible.", PW - 2 * M - 175, 8.5, color=MUTED)
    L.pagina()

    # ---- 7 layout y sistema de diseño
    cabecera_pag(L, 8, "7. Layout y sistema de diseño")
    y = subtitulo(L, 124, "Estructura de pantalla (layout base)") + 8
    L.rect(M, y, 300, 190, fill=BG, stroke=BORDER, r=8)
    L.rect(M, y, 70, 190, fill=PRIMARY_DARK, r=8)
    L.texto(M + 35, y + 100, "Menú", 8, "Sans-B", "#FFFFFF", "c")
    L.texto(M + 35, y + 112, "lateral", 8, "Sans-B", "#FFFFFF", "c")
    L.rect(M + 70, y, 230, 30, fill=SURFACE)
    L.texto(M + 82, y + 19, "Cabecera: título + acción", 8, "Sans-B", TEXT)
    L.rect(M + 82, y + 42, 206, 24, stroke=PRIMARY, r=4, lw=1)
    L.texto(M + 185, y + 58, "Filtros / formulario", 8, "Sans", PRIMARY, "c")
    L.rect(M + 82, y + 76, 206, 100, stroke=PRIMARY, r=4, lw=1)
    L.texto(M + 185, y + 130, "Contenido: tabla o tarjetas", 8, "Sans", PRIMARY, "c")
    L.parrafo(M + 322, y + 6, "BorderPane: menú lateral a la izquierda (left), cabecera arriba (top) y contenido en el centro (center).",
              PW - 2 * M - 322, 8.5)
    L.parrafo(M + 322, y + 56, "Márgenes de contenido: 24 px. Separación entre tarjetas: 16 px. Rejilla de 12 columnas con "
              "separación de 16 px. Ventana mínima: 1000 × 650 px.", PW - 2 * M - 322, 8.5)
    L.parrafo(M + 322, y + 112, "Orden de lectura: título → filtros → datos → acciones.", PW - 2 * M - 322, 8.5)
    y += 210
    y = subtitulo(L, y, "Componentes reutilizables") + 10
    filas = [("Botón primario", "Button", ".btn-primary"), ("Botón secundario", "Button", ".btn-secondary"),
             ("Botón peligro", "Button", ".btn-danger"), ("Campo de texto / error", "TextField", ".field, .field-error"),
             ("Desplegable", "ComboBox", ".combo"), ("Tabla", "TableView", ".table"),
             ("Badge de estado", "Label", ".badge-pendiente … .badge-aceptado"), ("Tarjeta", "VBox", ".card"),
             ("Menú lateral", "VBox + ToggleButton", ".sidebar, .nav-item"), ("Diálogo", "Dialog / Stage modal", ".dialog")]
    tabla(L, M, y, [150, 130, 215], ["Componente", "Nodo JavaFX", "Clase CSS"], filas, 20)
    y += 20 * 11 + 18
    y = subtitulo(L, y, "Variables CSS (styles.css, para v2.0 y v3.0)") + 6
    css = [".root {", f"    -color-primary: {PRIMARY};   -color-primary-dark: {PRIMARY_DARK};",
           f"    -color-secondary: {SECONDARY};   -color-bg: {BG};",
           f"    -color-text: {TEXT};   -color-danger: {DANGER};", "    -fx-font-family: \"DejaVu Sans\", sans-serif;", "}"]
    L.rect(M, y, PW - 2 * M, 14 * len(css) + 12, fill="#F1F4F9", stroke=BORDER, r=6)
    for i, l in enumerate(css):
        L.texto(M + 10, y + 16 + i * 14, l, 7.8, "Mono", TEXT)
    L.pagina()

    # ---- 8 accesibilidad
    cabecera_pag(L, 9, "8. Accesibilidad y usabilidad")
    y = 124
    reglas = [("Contraste", "Todo el texto cumple WCAG AA (≥ 4,5:1). Los pares verificados están en la página 3."),
              ("No depender solo del color", "Los estados llevan siempre texto; los errores, mensaje además del borde rojo."),
              ("Teclado", "Orden de tabulación lógico, foco visible de 2 px, Enter para aceptar y Esc para cerrar diálogos."),
              ("Tamaño de objetivos", "Botones y filas con al menos 32 × 24 px para facilitar la selección con ratón."),
              ("Etiquetas descriptivas", "Todos los campos tienen etiqueta visible; los iconos, tooltip y texto alternativo (accessibleText)."),
              ("Prevención de errores", "Confirmación al borrar, validación en línea y mensajes claros sobre cómo corregir."),
              ("Feedback", "Tras cada acción se informa del resultado: guardado, eliminado o error, sin bloquear la aplicación."),
              ("Consistencia", "Mismos componentes y ubicación de acciones en todas las pantallas (acción principal arriba a la derecha).")]
    for n, t in reglas:
        tarjeta(L, M, y, PW - 2 * M, 54)
        L.texto(M + 14, y + 22, n, 9.5, "Sans-B", PRIMARY_DARK)
        L.parrafo(M + 14, y + 38, t, PW - 2 * M - 28, 8.5, color=MUTED)
        y += 64
    L.pagina()
    L.guardar()


if __name__ == "__main__":
    SALIDA.mkdir(parents=True, exist_ok=True)
    pdf_prototipo(SALIDA / "CandiTrack_Prototipo_v1.0.pdf")
    pdf_manual(SALIDA / "CandiTrack_Manual_de_Estilo_v1.0.pdf")
    print("PDF generados en", SALIDA)
