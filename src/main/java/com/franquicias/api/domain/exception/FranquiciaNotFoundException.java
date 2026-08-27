package com.franquicias.api.domain.exception;

public class FranquiciaNotFoundException  extends DomainException {
	
	public FranquiciaNotFoundException(String franquiciaId) {
		
		super("FRANQUICIA NOT FOUND","No existe franquicia registrada con id: %s".formatted(franquiciaId));
	}

}
