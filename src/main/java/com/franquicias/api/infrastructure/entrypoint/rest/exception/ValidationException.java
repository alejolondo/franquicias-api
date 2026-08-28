package com.franquicias.api.infrastructure.entrypoint.rest.exception;

import java.util.List;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private final List<String> detalles;

    public ValidationException(List<String> detalles) {
        super("La peticion contiene datos invalidos");
        this.detalles = detalles;
    }
}