package com.sistemabodega.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sistemabodega.config.SesionInterceptor;
import com.sistemabodega.modelo.Usuario;
import com.sistemabodega.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioServ;

    public UsuarioController(UsuarioService usuarioServ) {
        this.usuarioServ = usuarioServ;
    }

    // listar usuarios
    @GetMapping("/gestionusuarios")
    public String listar(Model model) {
        model.addAttribute("listausuarios", usuarioServ.listarUsuarios());
        model.addAttribute("listaroles", usuarioServ.listarRoles());
        return "usuario/usuarios";
    }

    // crear usuario
    @PostMapping("/registrarusuario")
    public String registrar(@ModelAttribute("datosUsuario") Usuario usuario, RedirectAttributes flash) {
        try {
            usuarioServ.registrar(usuario);
            flash.addFlashAttribute("mensaje", "Usuario registrado correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestionusuarios";
    }

    // editar usuario
    @PostMapping("/actualizarusuario")
    public String actualizar(@ModelAttribute("datosUsuario") Usuario usuario, RedirectAttributes flash) {
        try {
            usuarioServ.actualizar(usuario);
            flash.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestionusuarios";
    }

    // activar o inactivar usuario
    @PostMapping("/estadousuario")
    public String cambiarEstado(@RequestParam Integer id, HttpSession session, RedirectAttributes flash) {
        Usuario sesion = (Usuario) session.getAttribute(SesionInterceptor.USUARIO_SESION);
        try {
            Usuario u = usuarioServ.cambiarEstado(id, sesion.getIdUsuario());
            flash.addFlashAttribute("mensaje",
                    "El usuario " + u.getUsuario() + (u.getEstado() == 1 ? " fue activado." : " fue inactivado."));
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestionusuarios";
    }
}
