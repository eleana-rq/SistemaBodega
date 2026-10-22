package com.sistemabodega.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sistemabodega.modelo.Proveedor;
import com.sistemabodega.service.ProveedorService;

@Controller
public class ProveedorController {

    private final ProveedorService proveedorServ;

    public ProveedorController(ProveedorService proveedorServ) {
        this.proveedorServ = proveedorServ;
    }

    // listar proveedores
    @GetMapping("/gestionproveedores")
    public String listar(Model model) {
        model.addAttribute("listaproveedores", proveedorServ.listarProveedores());
        return "proveedor/proveedores";
    }

    // crear proveedor
    @PostMapping("/registrarproveedor")
    public String registrar(Proveedor proveedor, RedirectAttributes flash) {
        try {
            proveedorServ.registrar(proveedor);
            flash.addFlashAttribute("mensaje", "Proveedor registrado correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestionproveedores";
    }

    // editar proveedor
    @PostMapping("/actualizarproveedor")
    public String actualizar(Proveedor proveedor, RedirectAttributes flash) {
        try {
            proveedorServ.actualizar(proveedor);
            flash.addFlashAttribute("mensaje", "Proveedor actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestionproveedores";
    }

    // activar o inactivar proveedor
    @PostMapping("/estadoproveedor")
    public String cambiarEstado(@RequestParam Integer id, RedirectAttributes flash) {
        proveedorServ.cambiarEstado(id);
        flash.addFlashAttribute("mensaje", "Se cambió el estado del proveedor.");
        return "redirect:/gestionproveedores";
    }
}
