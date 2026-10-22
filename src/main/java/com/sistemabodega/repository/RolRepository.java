package com.sistemabodega.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sistemabodega.modelo.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {

	List<Rol> findByEstado(Integer estado);
}
