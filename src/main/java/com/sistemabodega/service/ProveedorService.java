package com.sistemabodega.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sistemabodega.modelo.Proveedor;
import com.sistemabodega.repository.ProveedorRepository;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepo;

    public ProveedorService(ProveedorRepository proveedorRepo) {
        this.proveedorRepo = proveedorRepo;
    }

    // listar proveedores
    public List<Proveedor> listarProveedores() {
        return proveedorRepo.findAll();
    }

    // listar proveedores activos
    public List<Proveedor> listarActivos() {
        return proveedorRepo.findByEstado(1);
    }

    // buscar proveedor por id
    public Proveedor buscarPorId(Integer id) {
        return proveedorRepo.findById(id).orElse(null);
    }

    // crear proveedor
    public Proveedor registrar(Proveedor proveedor) {
        validar(proveedor);
        proveedor.setEstado(1);
        return proveedorRepo.save(proveedor);
    }

    // editar proveedor
    public Proveedor actualizar(Proveedor proveedor) {
        validar(proveedor);
        return proveedorRepo.save(proveedor);
    }

    // activar o inactivar proveedor
    public void cambiarEstado(Integer id) {
        Proveedor proveedor = proveedorRepo.findById(id).orElse(null);
        if (proveedor != null) {
            proveedor.setEstado(proveedor.getEstado() == 1 ? 0 : 1);
            proveedorRepo.save(proveedor);
        }
    }

    // validar RUC de 11 digitos y que no se repita
    private void validar(Proveedor proveedor) {
        if (proveedor.getRuc() == null || !proveedor.getRuc().matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC debe tener 11 dígitos.");
        }
        Proveedor existente = proveedorRepo.findByRuc(proveedor.getRuc());
        if (existente != null && !existente.getIdProveedor().equals(proveedor.getIdProveedor())) {
            throw new IllegalArgumentException("Ya existe un proveedor con el RUC " + proveedor.getRuc() + ".");
        }
    }
}
