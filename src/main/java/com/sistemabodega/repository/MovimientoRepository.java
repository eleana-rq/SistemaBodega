package com.sistemabodega.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sistemabodega.modelo.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {

	// del mas reciente al mas antiguo
	List<Movimiento> findAllByOrderByIdMovimientoDesc();
}
