package com.franquicias.api.domain.exception;

public class ProductoNotFoundException extends DomainException {

	public ProductoNotFoundException(String productoId) {
		super("PRODUCTO_NOT_FOUND", "No existe producto con id: %s".formatted(productoId));
	}

}
