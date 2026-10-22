package com.sistemabodega.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.stereotype.Controller;

import java.net.URI;
import java.time.LocalDate;

import com.sistemabodega.service.ReporteService;

@Controller
public class ReporteController {

    private static final MediaType EXCEL =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /*
     * Muestra la página principal de reportes.
     * URL: http://localhost:8080/reportes
     */
    // mostrar pagina de reportes
    @GetMapping("/reportes")
    public String mostrarReportes() {
        return "reportes/reportes";
    }

    // reporte de ventas en pdf
    @GetMapping("/reportes/ventas/pdf")
    public ResponseEntity<byte[]> ventasPDF(@RequestParam String fechaInicio, @RequestParam String fechaFin)
            throws Exception {
        if (!periodoValido(fechaInicio, fechaFin)) {
            return periodoInvalido();
        }
        byte[] archivo = reporteService.generarVentasPDF(fechaInicio, fechaFin);
        return descargar(archivo, "VentasPorPeriodo.pdf", MediaType.APPLICATION_PDF);
    }

    // reporte de ventas en excel
    @GetMapping("/reportes/ventas/excel")
    public ResponseEntity<byte[]> ventasExcel(@RequestParam String fechaInicio, @RequestParam String fechaFin)
            throws Exception {
        if (!periodoValido(fechaInicio, fechaFin)) {
            return periodoInvalido();
        }
        byte[] archivo = reporteService.generarVentasExcel(fechaInicio, fechaFin);
        return descargar(archivo, "VentasPorPeriodo.xlsx", EXCEL);
    }

    // reporte de inventario en pdf
    @GetMapping("/reportes/inventario/pdf")
    public ResponseEntity<byte[]> inventarioPDF() throws Exception {
        byte[] archivo = reporteService.generarInventarioPDF();
        return descargar(archivo, "InventarioProductos.pdf", MediaType.APPLICATION_PDF);
    }

    // reporte de inventario en excel
    @GetMapping("/reportes/inventario/excel")
    public ResponseEntity<byte[]> inventarioExcel() throws Exception {
        byte[] archivo = reporteService.generarInventarioExcel();
        return descargar(archivo, "InventarioProductos.xlsx", EXCEL);
    }

    // reporte de stock bajo en pdf
    @GetMapping("/reportes/stockbajo/pdf")
    public ResponseEntity<byte[]> stockBajoPDF() throws Exception {
        byte[] archivo = reporteService.generarStockBajoPDF();
        return descargar(archivo, "ProductosStockBajo.pdf", MediaType.APPLICATION_PDF);
    }

    // reporte de stock bajo en excel
    @GetMapping("/reportes/stockbajo/excel")
    public ResponseEntity<byte[]> stockBajoExcel() throws Exception {
        byte[] archivo = reporteService.generarStockBajoExcel();
        return descargar(archivo, "ProductosStockBajo.xlsx", EXCEL);
    }

    // reporte de movimientos en pdf
    @GetMapping("/reportes/movimientos/pdf")
    public ResponseEntity<byte[]> movimientosPDF(@RequestParam String fechaInicio, @RequestParam String fechaFin)
            throws Exception {
        if (!periodoValido(fechaInicio, fechaFin)) {
            return periodoInvalido();
        }
        byte[] archivo = reporteService.generarMovimientosPDF(fechaInicio, fechaFin);
        return descargar(archivo, "MovimientosInventario.pdf", MediaType.APPLICATION_PDF);
    }

    // reporte de movimientos en excel
    @GetMapping("/reportes/movimientos/excel")
    public ResponseEntity<byte[]> movimientosExcel(@RequestParam String fechaInicio, @RequestParam String fechaFin)
            throws Exception {
        if (!periodoValido(fechaInicio, fechaFin)) {
            return periodoInvalido();
        }
        byte[] archivo = reporteService.generarMovimientosExcel(fechaInicio, fechaFin);
        return descargar(archivo, "MovimientosInventario.xlsx", EXCEL);
    }

    // devolver el archivo como descarga
    private ResponseEntity<byte[]> descargar(byte[] archivo, String nombre, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre)
                .contentType(tipo)
                .body(archivo);
    }

    // validar el periodo
    private boolean periodoValido(String fechaInicio, String fechaFin) {
        try {
            return !LocalDate.parse(fechaFin).isBefore(LocalDate.parse(fechaInicio));
        } catch (Exception e) {
            return false;
        }
    }

    // volver a reportes con aviso de error
    private ResponseEntity<byte[]> periodoInvalido() {
        return ResponseEntity.status(302).location(URI.create("/reportes?periodo=invalido")).build();
    }
}
