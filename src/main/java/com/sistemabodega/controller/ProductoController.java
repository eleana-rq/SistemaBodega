package com.sistemabodega.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sistemabodega.modelo.Producto;
import com.sistemabodega.service.CategoriaService;
import com.sistemabodega.service.ProductoService;

@Controller
public class ProductoController {

    private final ProductoService productoServ;
    private final CategoriaService categoriaServ;

    public ProductoController(ProductoService productoServ, CategoriaService categoriaServ) {
        this.productoServ = productoServ;
        this.categoriaServ = categoriaServ;
    }

    // listar productos
    @GetMapping("/gestionproductos")
    public String listar(Model model) {
        model.addAttribute("listaproductos", productoServ.listarProductos());
        model.addAttribute("listacategorias", categoriaServ.listarActivas());
        return "producto/productos";
    }

    // crear producto
    @PostMapping("/registrarproducto")
    public String registrar(Producto producto, RedirectAttributes flash) {
        try {
            productoServ.registrar(producto);
            flash.addFlashAttribute("mensaje",
                    "Producto registrado. Registre su stock desde Inventario > Ingreso de mercadería.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestionproductos";
    }

    // editar producto
    @PostMapping("/actualizarproducto")
    public String actualizar(Producto producto, RedirectAttributes flash) {
        try {
            productoServ.actualizar(producto);
            flash.addFlashAttribute("mensaje", "Producto actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestionproductos";
    }

    // activar o inactivar producto
    @PostMapping("/estadoproducto")
    public String cambiarEstado(@RequestParam Integer id, RedirectAttributes flash) {
        productoServ.cambiarEstado(id);
        flash.addFlashAttribute("mensaje", "Se cambió el estado del producto.");
        return "redirect:/gestionproductos";
    }

    // consultar productos
    @GetMapping("/consultaproductos")
    public String consultar(@RequestParam(required = false) String criterio, Model model) {
        model.addAttribute("criterio", criterio);
        model.addAttribute("listaproductos", productoServ.buscar(criterio));
        return "producto/consulta";
    }
}
