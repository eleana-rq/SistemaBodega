package com.sistemabodega.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemabodega.modelo.DetalleVenta;
import com.sistemabodega.modelo.Movimiento;
import com.sistemabodega.modelo.Producto;
import com.sistemabodega.modelo.Usuario;
import com.sistemabodega.modelo.Venta;
import com.sistemabodega.repository.ProductoRepository;
import com.sistemabodega.repository.VentaRepository;

@Service
public class VentaService {

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final InventarioService inventarioServ;

    public VentaService(VentaRepository ventaRepo, ProductoRepository productoRepo,
            InventarioService inventarioServ) {
        this.ventaRepo = ventaRepo;
        this.productoRepo = productoRepo;
        this.inventarioServ = inventarioServ;
    }

    // listar ventas
    public List<Venta> listarVentas() {
        return ventaRepo.findAllByOrderByIdVentaDesc();
    }

    // buscar venta por id
    public Venta buscarPorId(Integer id) {
        return ventaRepo.findById(id).orElse(null);
    }

    // contar ventas del dia
    public long contarVentasHoy() {
        return ventaRepo.contarDesde(LocalDate.now().atStartOfDay());
    }

    // sumar el monto vendido del dia
    public BigDecimal totalVentasHoy() {
        return ventaRepo.totalDesde(LocalDate.now().atStartOfDay());
    }

    // registrar venta
    @Transactional
    public Venta registrarVenta(List<Integer> idsProducto, List<Integer> cantidades, Usuario cajero) {
        if (idsProducto == null || idsProducto.isEmpty() || cantidades == null
                || cantidades.size() != idsProducto.size()) {
            throw new IllegalArgumentException("Agregue al menos un producto a la venta.");
        }

        // crear la venta
        Venta venta = new Venta();
        venta.setNroBoleta(generarNroBoleta());
        venta.setFecha(LocalDateTime.now());
        venta.setUsuario(cajero);
        venta.setEstado(1);

        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < idsProducto.size(); i++) {
            Producto producto = productoRepo.findById(idsProducto.get(i)).orElse(null);
            Integer cantidad = cantidades.get(i);
            if (cantidad == null || cantidad <= 0) {
                throw new IllegalArgumentException("Las cantidades deben ser mayores que cero.");
            }
            if (producto == null || producto.getEstado() != 1) {
                throw new IllegalArgumentException("Uno de los productos no existe o está inactivo.");
            }
            // validar stock
            if (cantidad > producto.getStock()) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre()
                        + ". Disponible: " + producto.getStock() + ".");
            }
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(producto.getPrecioVenta().multiply(BigDecimal.valueOf(cantidad)));
            venta.agregarDetalle(detalle);
            total = total.add(detalle.getSubtotal());
        }
        venta.setTotal(total);

        // guardar venta y detalle
        Venta registrada = ventaRepo.save(venta);

        // descontar stock
        for (DetalleVenta detalle : registrada.getDetalles()) {
            inventarioServ.aplicarMovimiento(detalle.getProducto(), Movimiento.VENTA, -detalle.getCantidad(),
                    null, registrada.getNroBoleta(), "Venta " + registrada.getNroBoleta(), cajero);
        }
        return registrada;
    }

    // anular venta
    @Transactional
    public Venta anular(Integer idVenta, String motivo, Usuario administrador) {
        Venta venta = ventaRepo.findById(idVenta).orElse(null);
        if (venta == null) {
            throw new IllegalArgumentException("La venta no existe.");
        }
        if (venta.getEstado() != 1) {
            throw new IllegalArgumentException("La boleta " + venta.getNroBoleta() + " ya está anulada.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo de la anulación.");
        }
        venta.setEstado(0);
        venta.setMotivoAnulacion(motivo.trim());
        for (DetalleVenta detalle : venta.getDetalles()) {
            inventarioServ.aplicarMovimiento(detalle.getProducto(), Movimiento.ANULACION, detalle.getCantidad(),
                    null, venta.getNroBoleta(), "Anulación " + venta.getNroBoleta() + ": " + motivo.trim(),
                    administrador);
        }
        return ventaRepo.save(venta);
    }

    // generar numero de boleta
    private String generarNroBoleta() {
        int siguiente = ventaRepo.obtenerUltimoId() + 1;
        return String.format("B001-%08d", siguiente);
    }
}
