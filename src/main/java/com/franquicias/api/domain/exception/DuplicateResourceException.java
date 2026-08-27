package com.franquicias.api.domain.exception;

public class DuplicateResourceException extends DomainException{

	public DuplicateResourceException(String recurso, String nombre) {
		super("DUPLICATE_RESOURCE",
				"Ya existe %s con nombre: %s".formatted(recurso, nombre));
		
	}

}
