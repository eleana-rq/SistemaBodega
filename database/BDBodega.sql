DROP DATABASE IF EXISTS BDBodega;
CREATE DATABASE BDBodega CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE BDBodega;

-- SEGURIDAD

-- Perfiles de acceso: Administrador y Cajero.
CREATE TABLE tb_rol (
    id_rol      INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE,
    descripcion VARCHAR(150),
    estado      INT          NOT NULL DEFAULT 1
);

-- Personas autorizadas a usar el sistema.
-- La clave se guarda cifrada con SHA-256.
CREATE TABLE tb_usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombres    VARCHAR(80)  NOT NULL,
    apellidos  VARCHAR(80)  NOT NULL,
    usuario    VARCHAR(30)  NOT NULL UNIQUE,
    clave      VARCHAR(64)  NOT NULL,
    correo     VARCHAR(100),
    id_rol     INT          NOT NULL,
    estado     INT          NOT NULL DEFAULT 1,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES tb_rol (id_rol)
);

-- PRODUCTOS

CREATE TABLE tb_categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(60)  NOT NULL UNIQUE,
    descripcion  VARCHAR(150),
    estado       INT          NOT NULL DEFAULT 1
);

-- codigo unico, nombre, categoria y precio mayor que cero.
-- el stock nunca puede ser negativo.
CREATE TABLE tb_producto (
    id_producto  INT AUTO_INCREMENT PRIMARY KEY,
    codigo       VARCHAR(20)   NOT NULL UNIQUE,
    nombre       VARCHAR(100)  NOT NULL,
    id_categoria INT           NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    stock        INT           NOT NULL DEFAULT 0,
    stock_minimo INT           NOT NULL DEFAULT 0,
    estado       INT           NOT NULL DEFAULT 1,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES tb_categoria (id_categoria),
    CONSTRAINT ck_producto_precio CHECK (precio_venta > 0),
    CONSTRAINT ck_producto_stock CHECK (stock >= 0),
    CONSTRAINT ck_producto_minimo CHECK (stock_minimo >= 0)
);

CREATE INDEX ix_producto_nombre ON tb_producto (nombre);

-- INVENTARIO

CREATE TABLE tb_proveedor (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    ruc          VARCHAR(11)  NOT NULL UNIQUE,
    razon_social VARCHAR(120) NOT NULL,
    contacto     VARCHAR(80),
    telefono     VARCHAR(20),
    correo       VARCHAR(100),
    direccion    VARCHAR(150),
    estado       INT          NOT NULL DEFAULT 1
);

-- Ingresos, salidas, ajustes y movimientos generados por las ventas.
-- tipo: INGRESO, SALIDA, AJUSTE, VENTA, ANULACION
-- cantidad: positiva si aumenta el stock, negativa si lo disminuye.
-- todo INGRESO debe tener proveedor y documento sustento.
CREATE TABLE tb_movimiento (
    id_movimiento   INT AUTO_INCREMENT PRIMARY KEY,
    tipo            VARCHAR(15)  NOT NULL,
    fecha           DATETIME     NOT NULL,
    id_producto     INT          NOT NULL,
    cantidad        INT          NOT NULL,
    stock_anterior  INT          NOT NULL,
    stock_resultante INT         NOT NULL,
    id_proveedor    INT,
    documento       VARCHAR(30),
    motivo          VARCHAR(150),
    id_usuario      INT          NOT NULL,
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (id_producto) REFERENCES tb_producto (id_producto),
    CONSTRAINT fk_movimiento_proveedor FOREIGN KEY (id_proveedor) REFERENCES tb_proveedor (id_proveedor),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario)
);

CREATE INDEX ix_movimiento_fecha ON tb_movimiento (fecha);

-- VENTAS

-- cada venta genera una boleta con numero correlativo unico.
-- estado: 1 = Registrada, 0 = Anulada
CREATE TABLE tb_venta (
    id_venta         INT AUTO_INCREMENT PRIMARY KEY,
    nro_boleta       VARCHAR(15)   NOT NULL UNIQUE,
    fecha            DATETIME      NOT NULL,
    total            DECIMAL(10,2) NOT NULL,
    id_usuario       INT           NOT NULL,
    estado           INT           NOT NULL DEFAULT 1,
    motivo_anulacion VARCHAR(150),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario)
);

CREATE INDEX ix_venta_fecha ON tb_venta (fecha);

CREATE TABLE tb_detalle_venta (
    id_detalle      INT AUTO_INCREMENT PRIMARY KEY,
    id_venta        INT           NOT NULL,
    id_producto     INT           NOT NULL,
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES tb_venta (id_venta),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES tb_producto (id_producto),
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0)
);

-- DATOS INICIALES

INSERT INTO tb_rol (nombre, descripcion) VALUES
('Administrador', 'Gestiona usuarios, productos, inventario y reportes, y anula ventas'),
('Cajero', 'Consulta productos, registra ventas e ingresos de mercaderia');

-- Usuarios de prueba (clave cifrada con SHA-256):
--   admin   / admin123
--   cajero  / cajero123
INSERT INTO tb_usuario (nombres, apellidos, usuario, clave, correo, id_rol) VALUES
('Nelly Eleana', 'Ramos Quiliche', 'admin', SHA2('admin123', 256), 'admin@bodega.pe', 1),
('Franklin Alexander', 'Pereda Iparraguirre', 'cajero', SHA2('cajero123', 256), 'cajero@bodega.pe', 2);

INSERT INTO tb_categoria (nombre, descripcion) VALUES
('Abarrotes', 'Arroz, azucar, aceite, fideos y conservas'),
('Bebidas', 'Gaseosas, aguas, jugos y rehidratantes'),
('Lacteos', 'Leche, yogurt, quesos y mantequilla'),
('Snacks', 'Galletas, chocolates y piqueos'),
('Limpieza', 'Detergentes, lejia y articulos de limpieza'),
('Cuidado personal', 'Jabon, shampoo, pasta dental y papel higienico');

INSERT INTO tb_producto (codigo, nombre, id_categoria, precio_venta, stock, stock_minimo) VALUES
('P0001', 'Arroz Costeño 1 kg', 1, 4.80, 50, 10),
('P0002', 'Azucar rubia Cartavio 1 kg', 1, 4.20, 40, 10),
('P0003', 'Aceite Primor 1 L', 1, 10.50, 25, 8),
('P0004', 'Fideo spaghetti Don Vittorio 500 g', 1, 3.60, 30, 10),
('P0005', 'Atun Florida 170 g', 1, 6.90, 6, 8),
('P0006', 'Inca Kola 500 ml', 2, 2.50, 48, 12),
('P0007', 'Coca Cola 1.5 L', 2, 7.00, 20, 6),
('P0008', 'Agua San Luis 625 ml', 2, 1.80, 60, 15),
('P0009', 'Leche Gloria azul 400 g', 3, 4.30, 36, 12),
('P0010', 'Yogurt Gloria fresa 1 L', 3, 7.50, 4, 5),
('P0011', 'Galleta Soda Field', 4, 0.80, 100, 20),
('P0012', 'Chocolate Sublime 30 g', 4, 1.50, 70, 20),
('P0013', 'Detergente Bolivar 750 g', 5, 9.90, 15, 5),
('P0014', 'Lejia Clorox 1 L', 5, 4.50, 3, 5),
('P0015', 'Papel higienico Elite x4', 6, 6.20, 22, 6);

INSERT INTO tb_proveedor (ruc, razon_social, contacto, telefono, correo, direccion) VALUES
('20100055237', 'Alicorp S.A.A.', 'Carlos Medina', '014151800', 'ventas@alicorp.com.pe', 'Av. Argentina 4793, Callao'),
('20100190797', 'Gloria S.A.', 'Rosa Quispe', '014707170', 'pedidos@gloria.com.pe', 'Av. Republica de Panama 2461, La Victoria'),
('20415932376', 'Corporacion Lindley S.A.', 'Miguel Paredes', '013113000', 'distribucion@lindley.pe', 'Jr. Cajamarca 371, Rimac'),
('20512345678', 'Distribuidora El Sol E.I.R.L.', 'Ana Flores', '987654321', 'elsol@gmail.com', 'Av. Los Olivos 123, Los Olivos');

-- Movimientos iniciales para que el kardex refleje el stock cargado.
INSERT INTO tb_movimiento (tipo, fecha, id_producto, cantidad, stock_anterior, stock_resultante, id_proveedor, documento, motivo, id_usuario)
SELECT 'INGRESO', NOW() - INTERVAL 7 DAY, id_producto, stock, 0, stock, 4, 'GR-0001-000100', 'Inventario inicial', 1
FROM tb_producto;
