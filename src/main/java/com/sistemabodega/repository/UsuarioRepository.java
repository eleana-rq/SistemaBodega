package com.sistemabodega.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sistemabodega.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	// buscar usuario por nombre de usuario
	Usuario findByUsuario(String usuario);

	List<Usuario> findAllByOrderByIdUsuarioAsc();
}
