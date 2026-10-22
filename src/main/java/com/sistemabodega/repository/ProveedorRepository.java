package com.sistemabodega.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sistemabodega.modelo.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

	List<Proveedor> findByEstado(Integer estado);

	// buscar proveedor por RUC
	Proveedor findByRuc(String ruc);
}
