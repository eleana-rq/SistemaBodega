package com.sistemabodega.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sistemabodega.config.SesionInterceptor;
import com.sistemabodega.modelo.Usuario;
import com.sistemabodega.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final UsuarioService usuarioServ;

    public LoginController(UsuarioService usuarioServ) {
        this.usuarioServ = usuarioServ;
    }

    // ir al dashboard
    @GetMapping("/")
    public String inicio() {
        return "redirect:/dashboard";
    }

    // mostrar login
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        if (session.getAttribute(SesionInterceptor.USUARIO_SESION) != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    // iniciar sesion
    @PostMapping("/login")
    public String iniciarSesion(@RequestParam String usuario, @RequestParam String clave,
            HttpSession session, Model model) {
        try {
            Usuario u = usuarioServ.autenticar(usuario, clave);
            session.setAttribute(SesionInterceptor.USUARIO_SESION, u);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuario);
            return "login";
        }
    }

    // cerrar sesion
    @PostMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login?salir";
    }
}
