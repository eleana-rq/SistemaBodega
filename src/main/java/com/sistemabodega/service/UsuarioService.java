package com.sistemabodega.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sistemabodega.modelo.Rol;
import com.sistemabodega.modelo.Usuario;
import com.sistemabodega.repository.RolRepository;
import com.sistemabodega.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;

    public UsuarioService(UsuarioRepository usuarioRepo, RolRepository rolRepo) {
        this.usuarioRepo = usuarioRepo;
        this.rolRepo = rolRepo;
    }

    // iniciar sesion
    public Usuario autenticar(String nombreUsuario, String clave) {
        Usuario usuario = usuarioRepo.findByUsuario(nombreUsuario);
        // validar credenciales
        if (usuario == null || !usuario.getClave().equals(cifrar(clave))) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
        }
        // validar que el usuario este activo
        if (usuario.getEstado() != 1) {
            throw new IllegalArgumentException("Su usuario está inactivo. Contacte al administrador.");
        }
        return usuario;
    }

    // listar usuarios
    public List<Usuario> listarUsuarios() {
        return usuarioRepo.findAllByOrderByIdUsuarioAsc();
    }

    // listar roles
    public List<Rol> listarRoles() {
        return rolRepo.findByEstado(1);
    }

    // buscar usuario por id
    public Usuario buscarPorId(Integer id) {
        return usuarioRepo.findById(id).orElse(null);
    }

    // crear usuario
    public Usuario registrar(Usuario usuario) {
        validarUsuarioUnico(usuario);
        if (usuario.getClave() == null || usuario.getClave().isBlank()) {
            throw new IllegalArgumentException("Debe ingresar una contraseña.");
        }
        usuario.setClave(cifrar(usuario.getClave()));
        usuario.setEstado(1);
        return usuarioRepo.save(usuario);
    }

    // editar usuario
    public Usuario actualizar(Usuario usuario) {
        Usuario actual = usuarioRepo.findById(usuario.getIdUsuario()).orElse(null);
        if (actual == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        validarUsuarioUnico(usuario);
        actual.setNombres(usuario.getNombres());
        actual.setApellidos(usuario.getApellidos());
        actual.setUsuario(usuario.getUsuario());
        actual.setCorreo(usuario.getCorreo());
        actual.setRol(usuario.getRol());
        if (usuario.getClave() != null && !usuario.getClave().isBlank()) {
            actual.setClave(cifrar(usuario.getClave()));
        }
        return usuarioRepo.save(actual);
    }

    // activar o inactivar usuario
    public Usuario cambiarEstado(Integer id, Integer idUsuarioSesion) {
        Usuario usuario = usuarioRepo.findById(id).orElse(null);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        if (usuario.getIdUsuario().equals(idUsuarioSesion)) {
            throw new IllegalArgumentException("No puede inactivar su propia cuenta.");
        }
        usuario.setEstado(usuario.getEstado() == 1 ? 0 : 1);
        return usuarioRepo.save(usuario);
    }

    // validar que el usuario no se repita
    private void validarUsuarioUnico(Usuario usuario) {
        Usuario existente = usuarioRepo.findByUsuario(usuario.getUsuario());
        if (existente != null && !existente.getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new IllegalArgumentException("El usuario '" + usuario.getUsuario() + "' ya existe. Ingrese otro.");
        }
    }

    // cifrar la clave con SHA-256
    public static String cifrar(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña", e);
        }
    }
}
