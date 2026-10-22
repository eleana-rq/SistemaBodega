package com.sistemabodega.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sistemabodega.modelo.Categoria;
import com.sistemabodega.service.CategoriaService;

@Controller
public class CategoriaController {

    private final CategoriaService categoriaServ;

    public CategoriaController(CategoriaService categoriaServ) {
        this.categoriaServ = categoriaServ;
    }

    // listar categorias
    @GetMapping("/gestioncategorias")
    public String listar(Model model) {
        model.addAttribute("listacategorias", categoriaServ.listarCategorias());
        return "categoria/categorias";
    }

    // crear categoria
    @PostMapping("/registrarcategoria")
    public String registrar(Categoria categoria, RedirectAttributes flash) {
        try {
            categoriaServ.registrar(categoria);
            flash.addFlashAttribute("mensaje", "Categoría registrada correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestioncategorias";
    }

    // editar categoria
    @PostMapping("/actualizarcategoria")
    public String actualizar(Categoria categoria, RedirectAttributes flash) {
        try {
            categoriaServ.actualizar(categoria);
            flash.addFlashAttribute("mensaje", "Categoría actualizada correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestioncategorias";
    }

    // activar o inactivar categoria
    @PostMapping("/estadocategoria")
    public String cambiarEstado(@RequestParam Integer id, RedirectAttributes flash) {
        categoriaServ.cambiarEstado(id);
        flash.addFlashAttribute("mensaje", "Se cambió el estado de la categoría.");
        return "redirect:/gestioncategorias";
    }
}
