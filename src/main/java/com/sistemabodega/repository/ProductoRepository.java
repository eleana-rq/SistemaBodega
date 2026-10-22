package com.sistemabodega.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sistemabodega.modelo.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

	List<Producto> findByEstado(Integer estado);

	List<Producto> findAllByOrderByCodigoAsc();

	// buscar producto por codigo
	Producto findByCodigo(String codigo);

	long countByEstado(Integer estado);

	// buscar por codigo, nombre o categoria
	@Query("SELECT p FROM Producto p WHERE "
			+ "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :criterio, '%')) "
			+ "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :criterio, '%')) "
			+ "OR LOWER(p.categoria.nombre) LIKE LOWER(CONCAT('%', :criterio, '%')) "
			+ "ORDER BY p.nombre")
	List<Producto> buscar(@Param("criterio") String criterio);

	// productos activos con stock menor o igual al minimo
	@Query("SELECT p FROM Producto p WHERE p.estado = 1 AND p.stock <= p.stockMinimo ORDER BY p.stock")
	List<Producto> listarStockBajo();
}
