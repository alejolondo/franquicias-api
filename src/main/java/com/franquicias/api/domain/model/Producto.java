package com.franquicias.api.domain.model;

import java.util.UUID;

import com.franquicias.api.domain.exception.InvalidStockException;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Producto {
	
	String id;
	String nombre;
	Integer stock;
	
	public static Producto crear(String nombre, Integer stock) {
        validarStock(stock);
        return Producto.builder()
                .id(UUID.randomUUID().toString())
                .nombre(nombre)
                .stock(stock)
                .build();
    }

    public Producto conStock(Integer nuevoStock) {
        validarStock(nuevoStock);
        return this.toBuilder()
                .stock(nuevoStock)
                .build();
    }

    public Producto conNombre(String nuevoNombre) {
        return this.toBuilder()
                .nombre(nuevoNombre)
                .build();
    }

    private static void validarStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new InvalidStockException(stock);
        }
    }

}
