package com.franquicias.api.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.franquicias.api.domain.exception.DuplicateResourceException;
import com.franquicias.api.domain.exception.ProductoNotFoundException;

import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder = true)
public class Sucursal {
	
	String id;
	String nombre;
	List<Producto> productos;
	
	public static Sucursal crear(String nombre) {
        return Sucursal.builder()
                .id(UUID.randomUUID().toString())
                .nombre(nombre)
                .productos(List.of())
                .build();
    }

    public Optional<Producto> buscarProducto(String productoId) {
        return productos.stream()
                .filter(producto -> producto.getId().equals(productoId))
                .findFirst();
    }

    public Sucursal agregarProducto(Producto producto) {
        boolean yaExiste = productos.stream()
                .anyMatch(actual -> actual.getNombre().equalsIgnoreCase(producto.getNombre()));

        if (yaExiste) {
            throw new DuplicateResourceException("producto", producto.getNombre());
        }

        List<Producto> actualizados = Stream
                .concat(productos.stream(), Stream.of(producto))
                .toList();

        return this.toBuilder()
                .productos(actualizados)
                .build();
    }

    public Sucursal eliminarProducto(String productoId) {
        buscarProducto(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));

        List<Producto> actualizados = productos.stream()
                .filter(producto -> !producto.getId().equals(productoId))
                .toList();

        return this.toBuilder()
                .productos(actualizados)
                .build();
    }

    public Sucursal reemplazarProducto(Producto producto) {
        buscarProducto(producto.getId())
                .orElseThrow(() -> new ProductoNotFoundException(producto.getId()));

        List<Producto> actualizados = productos.stream()
                .map(actual -> actual.getId().equals(producto.getId()) ? producto : actual)
                .toList();

        return this.toBuilder()
                .productos(actualizados)
                .build();
    }

    public Optional<Producto> productoConMayorStock() {
        return productos.stream()
                .max(Comparator.comparingInt(Producto::getStock));
    }

    public Sucursal conNombre(String nuevoNombre) {
        return this.toBuilder()
                .nombre(nuevoNombre)
                .build();
    }

}
