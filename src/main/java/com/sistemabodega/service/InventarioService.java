package com.sistemabodega.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemabodega.modelo.Movimiento;
import com.sistemabodega.modelo.Producto;
import com.sistemabodega.modelo.Proveedor;
import com.sistemabodega.modelo.Usuario;
import com.sistemabodega.repository.MovimientoRepository;
import com.sistemabodega.repository.ProductoRepository;
import com.sistemabodega.repository.ProveedorRepository;

@Service
public class InventarioService {

    private final MovimientoRepository movimientoRepo;
    private final ProductoRepository productoRepo;
    private final ProveedorRepository proveedorRepo;

    public InventarioService(MovimientoRepository movimientoRepo, ProductoRepository productoRepo,
            ProveedorRepository proveedorRepo) {
        this.movimientoRepo = movimientoRepo;
        this.productoRepo = productoRepo;
        this.proveedorRepo = proveedorRepo;
    }

    // listar movimientos
    public List<Movimiento> listarMovimientos() {
        return movimientoRepo.findAllByOrderByIdMovimientoDesc();
    }

    // registrar ingreso de mercaderia
    @Transactional
    public Producto registrarIngreso(Integer idProducto, Integer idProveedor, String documento,
            Integer cantidad, Usuario usuario) {
        Producto producto = obtenerProductoActivo(idProducto);
        Proveedor proveedor = proveedorRepo.findById(idProveedor == null ? 0 : idProveedor).orElse(null);
        if (proveedor == null || proveedor.getEstado() != 1) {
            throw new IllegalArgumentException("Seleccione un proveedor activo.");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar el documento sustento (guía o factura).");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        aplicarMovimiento(producto, Movimiento.INGRESO, cantidad, proveedor, documento.trim(),
                "Ingreso de mercadería", usuario);
        return producto;
    }

    // registrar salida o ajuste de stock
    @Transactional
    public Producto registrarSalida(Integer idProducto, String tipo, Integer cantidad, String motivo,
            Usuario usuario) {
        Producto producto = obtenerProductoActivo(idProducto);
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo.");
        }
        if (cantidad == null || cantidad < 0) {
            throw new IllegalArgumentException("Ingrese una cantidad válida.");
        }
        int variacion;
        if (Movimiento.AJUSTE.equals(tipo)) {
            variacion = cantidad - producto.getStock();
            if (variacion == 0) {
                throw new IllegalArgumentException("El stock contado es igual al stock del sistema; no hay ajuste.");
            }
        } else {
            if (cantidad == 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            }
            variacion = -cantidad;
            tipo = Movimiento.SALIDA;
        }
        aplicarMovimiento(producto, tipo, variacion, null, null, motivo.trim(), usuario);
        return producto;
    }

    // actualizar el stock y registrar el movimiento
    public Movimiento aplicarMovimiento(Producto producto, String tipo, int variacion, Proveedor proveedor,
            String documento, String motivo, Usuario usuario) {
        int stockAnterior = producto.getStock();
        int stockNuevo = stockAnterior + variacion;
        if (stockNuevo < 0) {
            throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre()
                    + ". Disponible: " + stockAnterior + ".");
        }
        producto.setStock(stockNuevo);
        productoRepo.save(producto);

        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(tipo);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setProducto(producto);
        movimiento.setCantidad(variacion);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockResultante(stockNuevo);
        movimiento.setProveedor(proveedor);
        movimiento.setDocumento(documento);
        movimiento.setMotivo(motivo);
        movimiento.setUsuario(usuario);
        return movimientoRepo.save(movimiento);
    }

    // obtener un producto activo
    private Producto obtenerProductoActivo(Integer idProducto) {
        Producto producto = productoRepo.findById(idProducto == null ? 0 : idProducto).orElse(null);
        if (producto == null) {
            throw new IllegalArgumentException("Seleccione un producto.");
        }
        if (producto.getEstado() != 1) {
            throw new IllegalArgumentException("El producto " + producto.getNombre() + " está inactivo.");
        }
        return producto;
    }
}
