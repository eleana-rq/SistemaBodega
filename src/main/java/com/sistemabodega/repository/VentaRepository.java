package com.sistemabodega.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sistemabodega.modelo.Venta;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

	// del mas reciente al mas antiguo
	List<Venta> findAllByOrderByIdVentaDesc();

	// ultimo id registrado (para el numero de boleta)
	@Query("SELECT COALESCE(MAX(v.idVenta), 0) FROM Venta v")
	Integer obtenerUltimoId();

	// contar ventas desde una fecha
	@Query("SELECT COUNT(v) FROM Venta v WHERE v.estado = 1 AND v.fecha >= :desde")
	long contarDesde(@Param("desde") LocalDateTime desde);

	// sumar ventas desde una fecha
	@Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 1 AND v.fecha >= :desde")
	BigDecimal totalDesde(@Param("desde") LocalDateTime desde);
}
