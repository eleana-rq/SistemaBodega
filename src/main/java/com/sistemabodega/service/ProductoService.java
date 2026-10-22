package com.sistemabodega.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sistemabodega.modelo.Producto;
import com.sistemabodega.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepo;

    public ProductoService(ProductoRepository productoRepo) {
        this.productoRepo = productoRepo;
    }

    // listar productos
    public List<Producto> listarProductos() {
        return productoRepo.findAllByOrderByCodigoAsc();
    }

    // listar productos activos
    public List<Producto> listarActivos() {
        return productoRepo.findByEstado(1);
    }

    // buscar producto por id
    public Producto buscarPorId(Integer id) {
        return productoRepo.findById(id).orElse(null);
    }

    // buscar por codigo, nombre o categoria
    public List<Producto> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return listarProductos();
        }
        return productoRepo.buscar(criterio.trim());
    }

    // listar productos con stock bajo
    public List<Producto> listarStockBajo() {
        return productoRepo.listarStockBajo();
    }

    // contar productos activos
    public long contarActivos() {
        return productoRepo.countByEstado(1);
    }

    // crear producto
    public Producto registrar(Producto producto) {
        validar(producto);
        producto.setStock(0);
        producto.setEstado(1);
        return productoRepo.save(producto);
    }

    // editar producto
    public Producto actualizar(Producto producto) {
        Producto actual = productoRepo.findById(producto.getIdProducto()).orElse(null);
        if (actual == null) {
            throw new IllegalArgumentException("El producto no existe.");
        }
        validar(producto);
        actual.setCodigo(producto.getCodigo());
        actual.setNombre(producto.getNombre());
        actual.setCategoria(producto.getCategoria());
        actual.setPrecioVenta(producto.getPrecioVenta());
        actual.setStockMinimo(producto.getStockMinimo());
        return productoRepo.save(actual);
    }

    // activar o inactivar producto
    public void cambiarEstado(Integer id) {
        Producto producto = productoRepo.findById(id).orElse(null);
        if (producto != null) {
            producto.setEstado(producto.getEstado() == 1 ? 0 : 1);
            productoRepo.save(producto);
        }
    }

    // validar codigo unico y precio mayor que cero
    private void validar(Producto producto) {
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero.");
        }
        if (producto.getStockMinimo() == null || producto.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
        Producto existente = productoRepo.findByCodigo(producto.getCodigo());
        if (existente != null && !existente.getIdProducto().equals(producto.getIdProducto())) {
            throw new IllegalArgumentException("El código '" + producto.getCodigo() + "' ya está registrado.");
        }
    }
}
