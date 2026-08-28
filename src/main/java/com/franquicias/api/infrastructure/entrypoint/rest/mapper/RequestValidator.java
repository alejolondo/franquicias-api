package com.franquicias.api.infrastructure.entrypoint.rest.mapper;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.franquicias.api.infrastructure.entrypoint.rest.exception.ValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> T validar(T objeto) {
        Set<ConstraintViolation<T>> violaciones = validator.validate(objeto);

        if (!violaciones.isEmpty()) {
            List<String> detalles = violaciones.stream()
                    .map(violacion -> "%s: %s".formatted(
                            violacion.getPropertyPath(), violacion.getMessage()))
                    .sorted()
                    .toList();
            throw new ValidationException(detalles);
        }

        return objeto;
    }
}