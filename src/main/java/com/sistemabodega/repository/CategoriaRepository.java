package com.sistemabodega.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sistemabodega.modelo.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

	List<Categoria> findByEstado(Integer estado);

	// buscar categoria por nombre
	Categoria findByNombre(String nombre);
}
