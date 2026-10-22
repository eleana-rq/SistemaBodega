package com.sistemabodega.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sistemabodega.config.SesionInterceptor;
import com.sistemabodega.modelo.Producto;
import com.sistemabodega.modelo.Usuario;
import com.sistemabodega.service.InventarioService;
import com.sistemabodega.service.ProductoService;
import com.sistemabodega.service.ProveedorService;

import jakarta.servlet.http.HttpSession;

@Controller
public class InventarioController {

    private final InventarioService inventarioServ;
    private final ProductoService productoServ;
    private final ProveedorService proveedorServ;

    public InventarioController(InventarioService inventarioServ, ProductoService productoServ,
            ProveedorService proveedorServ) {
        this.inventarioServ = inventarioServ;
        this.productoServ = productoServ;
        this.proveedorServ = proveedorServ;
    }

    // listar movimientos de inventario
    @GetMapping("/gestioninventario")
    public String listar(Model model) {
        model.addAttribute("listamovimientos", inventarioServ.listarMovimientos());
        model.addAttribute("listaproductos", productoServ.listarActivos());
        model.addAttribute("listaproveedores", proveedorServ.listarActivos());
        return "inventario/inventario";
    }

    // registrar ingreso de mercaderia
    @PostMapping("/registraringreso")
    public String registrarIngreso(@RequestParam Integer idProducto, @RequestParam Integer idProveedor,
            @RequestParam String documento, @RequestParam Integer cantidad,
            HttpSession session, RedirectAttributes flash) {
        try {
            Producto p = inventarioServ.registrarIngreso(idProducto, idProveedor, documento, cantidad,
                    usuarioSesion(session));
            flash.addFlashAttribute("mensaje", "Ingreso registrado. Nuevo stock de " + p.getNombre() + ": " + p.getStock());
            avisarStockBajo(p, flash);
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestioninventario";
    }

    // registrar salida o ajuste de stock
    @PostMapping("/registrarsalida")
    public String registrarSalida(@RequestParam Integer idProducto, @RequestParam String tipo,
            @RequestParam Integer cantidad, @RequestParam String motivo,
            HttpSession session, RedirectAttributes flash) {
        try {
            Producto p = inventarioServ.registrarSalida(idProducto, tipo, cantidad, motivo, usuarioSesion(session));
            flash.addFlashAttribute("mensaje", "Movimiento registrado. Nuevo stock de " + p.getNombre() + ": " + p.getStock());
            avisarStockBajo(p, flash);
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestioninventario";
    }

    // avisar si el producto tiene stock bajo
    private void avisarStockBajo(Producto p, RedirectAttributes flash) {
        if (p.isStockBajo()) {
            flash.addFlashAttribute("alerta", "Atención: " + p.getNombre() + " tiene stock bajo ("
                    + p.getStock() + " / mínimo " + p.getStockMinimo() + ").");
        }
    }

    // obtener el usuario de la sesion
    private Usuario usuarioSesion(HttpSession session) {
        return (Usuario) session.getAttribute(SesionInterceptor.USUARIO_SESION);
    }
}
