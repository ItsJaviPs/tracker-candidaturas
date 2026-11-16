CREATE TABLE EMPRESA (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    sector VARCHAR(80) NOT NULL,
    web VARCHAR(255),
    fechaRegistro DATE NOT NULL,
    CONSTRAINT uq_empresa_nombre UNIQUE (nombre)
);

CREATE TABLE OFERTA (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_empresa INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'pendiente',
    fechaPostulacion DATE NOT NULL,
    enlace VARCHAR(500),
    salario DECIMAL(10, 2),
    notas TEXT,
    CONSTRAINT fk_oferta_empresa FOREIGN KEY (id_empresa) REFERENCES EMPRESA (id) ON DELETE RESTRICT,
    CONSTRAINT ck_oferta_salario CHECK (salario IS NULL OR salario >= 0)
);

CREATE TABLE ENTREVISTA (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_oferta INT NOT NULL,
    fecha DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    notas TEXT,
    resultado VARCHAR(20) NOT NULL DEFAULT 'pendiente',
    CONSTRAINT fk_entrevista_oferta FOREIGN KEY (id_oferta) REFERENCES OFERTA (id) ON DELETE CASCADE,
    CONSTRAINT ck_entrevista_resultado CHECK (resultado IN ('pendiente', 'positivo', 'negativo'))
);
