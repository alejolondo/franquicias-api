package com.franquicias.api.domain.exception;

public class SucursalNotFoundException extends DomainException {

	public SucursalNotFoundException(String sucursalId) {
		super("SUCURSAL_NOT_FOUND", "No existe sucursal con id: %s".formatted(sucursalId));
	}
	
}
