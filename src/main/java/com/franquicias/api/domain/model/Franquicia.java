package com.franquicias.api.domain.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.franquicias.api.domain.exception.DuplicateResourceException;
import com.franquicias.api.domain.exception.SucursalNotFoundException;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Franquicia {

	String id;
    String nombre;
    List<Sucursal> sucursales;
    
    public static Franquicia crear(String nombre) {
        return Franquicia.builder()
                .id(UUID.randomUUID().toString())
                .nombre(nombre)
                .sucursales(List.of())
                .build();
    }

    public Optional<Sucursal> buscarSucursal(String sucursalId) {
        return sucursales.stream()
                .filter(sucursal -> sucursal.getId().equals(sucursalId))
                .findFirst();
    }

    public Franquicia agregarSucursal(Sucursal sucursal) {
        boolean yaExiste = sucursales.stream()
                .anyMatch(actual -> actual.getNombre().equalsIgnoreCase(sucursal.getNombre()));

        if (yaExiste) {
            throw new DuplicateResourceException("sucursal", sucursal.getNombre());
        }

        List<Sucursal> actualizadas = Stream
                .concat(sucursales.stream(), Stream.of(sucursal))
                .toList();

        return this.toBuilder()
                .sucursales(actualizadas)
                .build();
    }

    public Franquicia reemplazarSucursal(Sucursal sucursal) {
        buscarSucursal(sucursal.getId())
                .orElseThrow(() -> new SucursalNotFoundException(sucursal.getId()));

        List<Sucursal> actualizadas = sucursales.stream()
                .map(actual -> actual.getId().equals(sucursal.getId()) ? sucursal : actual)
                .toList();

        return this.toBuilder()
                .sucursales(actualizadas)
                .build();
    }

    public Franquicia conNombre(String nuevoNombre) {
        return this.toBuilder()
                .nombre(nuevoNombre)
                .build();
    }

    public List<ProductoTopStock> productosConMayorStockPorSucursal() {
        return sucursales.stream()
                .map(sucursal -> sucursal.productoConMayorStock()
                        .map(producto -> ProductoTopStock.builder()
                                .sucursalId(sucursal.getId())
                                .sucursalNombre(sucursal.getNombre())
                                .productoId(producto.getId())
                                .productoNombre(producto.getNombre())
                                .stock(producto.getStock())
                                .build()))
                .flatMap(Optional::stream)
                .toList();
    }
}
