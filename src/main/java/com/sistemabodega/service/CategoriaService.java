package com.sistemabodega.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sistemabodega.modelo.Categoria;
import com.sistemabodega.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepo;

    public CategoriaService(CategoriaRepository categoriaRepo) {
        this.categoriaRepo = categoriaRepo;
    }

    // listar categorias
    public List<Categoria> listarCategorias() {
        return categoriaRepo.findAll();
    }

    // listar categorias activas
    public List<Categoria> listarActivas() {
        return categoriaRepo.findByEstado(1);
    }

    // buscar categoria por id
    public Categoria buscarPorId(Integer id) {
        return categoriaRepo.findById(id).orElse(null);
    }

    // crear categoria
    public Categoria registrar(Categoria categoria) {
        validarNombreUnico(categoria);
        categoria.setEstado(1);
        return categoriaRepo.save(categoria);
    }

    // editar categoria
    public Categoria actualizar(Categoria categoria) {
        validarNombreUnico(categoria);
        return categoriaRepo.save(categoria);
    }

    // activar o inactivar categoria
    public void cambiarEstado(Integer id) {
        Categoria categoria = categoriaRepo.findById(id).orElse(null);
        if (categoria != null) {
            categoria.setEstado(categoria.getEstado() == 1 ? 0 : 1);
            categoriaRepo.save(categoria);
        }
    }

    // validar que el nombre no se repita
    private void validarNombreUnico(Categoria categoria) {
        Categoria existente = categoriaRepo.findByNombre(categoria.getNombre());
        if (existente != null && !existente.getIdCategoria().equals(categoria.getIdCategoria())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre '" + categoria.getNombre() + "'.");
        }
    }
}
