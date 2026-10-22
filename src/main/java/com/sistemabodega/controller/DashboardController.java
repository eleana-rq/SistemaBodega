package com.sistemabodega.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sistemabodega.service.ProductoService;
import com.sistemabodega.service.VentaService;

@Controller
public class DashboardController {

    private final ProductoService productoServ;
    private final VentaService ventaServ;

    public DashboardController(ProductoService productoServ, VentaService ventaServ) {
        this.productoServ = productoServ;
        this.ventaServ = ventaServ;
    }

    // mostrar dashboard
    @GetMapping("/dashboard")
    public String inicio(Model model) {
        model.addAttribute("totalProductos", productoServ.contarActivos());
        model.addAttribute("ventasHoy", ventaServ.contarVentasHoy());
        model.addAttribute("montoHoy", ventaServ.totalVentasHoy());
        model.addAttribute("listastockbajo", productoServ.listarStockBajo());
        return "dashboard/index";
    }
}
