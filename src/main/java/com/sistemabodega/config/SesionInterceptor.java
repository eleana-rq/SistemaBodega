package com.sistemabodega.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.sistemabodega.modelo.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// valida la sesion y el rol del usuario antes de cada peticion
@Component
public class SesionInterceptor implements HandlerInterceptor {

    // nombre con el que se guarda el usuario en la sesion
    public static final String USUARIO_SESION = "usuarioSesion";

    // rutas solo para el administrador
    private static final String[] RUTAS_ADMIN = { "/gestionusuarios", "/registrarusuario", "/actualizarusuario",
            "/estadousuario", "/gestioncategorias", "/registrarcategoria", "/actualizarcategoria",
            "/estadocategoria", "/gestionproductos", "/registrarproducto", "/actualizarproducto",
            "/estadoproducto", "/gestionproveedores", "/registrarproveedor", "/actualizarproveedor",
            "/estadoproveedor", "/anularventa", "/reportes" };

    // rutas para el administrador y el cajero
    private static final String[] RUTAS_ADMIN_CAJERO = { "/consultaproductos", "/gestioninventario",
            "/registraringreso", "/registrarsalida", "/ventas", "/boleta" };

    // rutas solo para el cajero
    private static final String[] RUTAS_CAJERO = { "/nuevaventa", "/registrarventa" };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Usuario usuario = (Usuario) request.getSession().getAttribute(USUARIO_SESION);

        // si no hay sesion, ir al login
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        // si el rol no tiene permiso, volver al dashboard
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        String rol = usuario.getRol().getNombre();
        if (!tienePermiso(ruta, rol)) {
            response.sendRedirect(request.getContextPath() + "/dashboard?denegado");
            return false;
        }
        return true;
    }

    // revisar si el rol puede entrar a la ruta
    private boolean tienePermiso(String ruta, String rol) {
        boolean esAdmin = rol.equals("Administrador");
        boolean esCajero = rol.equals("Cajero");

        if (empiezaCon(ruta, RUTAS_ADMIN)) {
            return esAdmin;
        }
        if (empiezaCon(ruta, RUTAS_ADMIN_CAJERO)) {
            return esAdmin || esCajero;
        }
        if (empiezaCon(ruta, RUTAS_CAJERO)) {
            return esCajero;
        }
        // dashboard y cerrar sesion son para todos
        return true;
    }

    // revisar si la ruta empieza con alguna de la lista
    private boolean empiezaCon(String ruta, String[] lista) {
        for (String r : lista) {
            if (ruta.startsWith(r)) {
                return true;
            }
        }
        return false;
    }
}
