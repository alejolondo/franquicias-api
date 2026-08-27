package com.franquicias.api.domain.exception;

public class InvalidStockException extends DomainException {

	public InvalidStockException(Integer stock) {
		super("INVALID_STOCK",
	              "El stock no puede ser negativo. Valor recibido: %s".formatted(stock));
		
	}

}
