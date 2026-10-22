package com.sistemabodega.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sistemabodega.config.SesionInterceptor;
import com.sistemabodega.modelo.Usuario;
import com.sistemabodega.modelo.Venta;
import com.sistemabodega.service.ProductoService;
import com.sistemabodega.service.ReporteService;
import com.sistemabodega.service.VentaService;

import jakarta.servlet.http.HttpSession;

@Controller
public class VentaController {

    private final VentaService ventaServ;
    private final ProductoService productoServ;
    private final ReporteService reporteServ;

    public VentaController(VentaService ventaServ, ProductoService productoServ, ReporteService reporteServ) {
        this.ventaServ = ventaServ;
        this.productoServ = productoServ;
        this.reporteServ = reporteServ;
    }

    // mostrar pantalla de venta
    @GetMapping("/nuevaventa")
    public String nuevaVenta(Model model) {
        model.addAttribute("listaproductos", productoServ.listarActivos());
        return "venta/nuevaventa";
    }

    // registrar venta
    @PostMapping("/registrarventa")
    public String registrar(@RequestParam(required = false) List<Integer> idProducto,
            @RequestParam(required = false) List<Integer> cantidad,
            HttpSession session, RedirectAttributes flash) {
        try {
            Usuario cajero = (Usuario) session.getAttribute(SesionInterceptor.USUARIO_SESION);
            Venta venta = ventaServ.registrarVenta(idProducto, cantidad, cajero);
            flash.addFlashAttribute("mensaje", "Venta registrada. Boleta " + venta.getNroBoleta());
            flash.addFlashAttribute("idBoleta", venta.getIdVenta());
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/nuevaventa";
    }

    // listar ventas
    @GetMapping("/ventas")
    public String listar(Model model) {
        model.addAttribute("listaventas", ventaServ.listarVentas());
        return "venta/ventas";
    }

    // anular venta
    @PostMapping("/anularventa")
    public String anular(@RequestParam Integer id, @RequestParam String motivo,
            HttpSession session, RedirectAttributes flash) {
        try {
            Usuario admin = (Usuario) session.getAttribute(SesionInterceptor.USUARIO_SESION);
            Venta venta = ventaServ.anular(id, motivo, admin);
            flash.addFlashAttribute("mensaje", "La boleta " + venta.getNroBoleta() + " fue anulada y el stock restituido.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas";
    }

    // generar boleta en pdf
    @GetMapping("/boleta/{id}")
    public ResponseEntity<byte[]> boleta(@PathVariable Integer id) throws Exception {
        byte[] archivo = reporteServ.generarBoletaPDF(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=Boleta-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
    }
}
