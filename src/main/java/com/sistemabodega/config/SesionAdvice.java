package com.sistemabodega.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.sistemabodega.modelo.Usuario;

import jakarta.servlet.http.HttpSession;

// envia el usuario de la sesion a todas las vistas
@ControllerAdvice
public class SesionAdvice {

    // enviar el usuario de la sesion a las vistas
    @ModelAttribute("usuarioSesion")
    public Usuario usuarioSesion(HttpSession session) {
        return (Usuario) session.getAttribute(SesionInterceptor.USUARIO_SESION);
    }
}
