package com.franquicias.api.domain.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {
	
	private final String codigo;
	
	protected DomainException(String codigo, String mensaje) {
		super(mensaje);
		this.codigo = codigo;
	}

}
