package com.franquicias.api.infrastructure.entrypoint.rest.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.franquicias.api.domain.exception.DomainException;
import com.franquicias.api.domain.exception.DuplicateResourceException;
import com.franquicias.api.domain.exception.InvalidStockException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);
        String path = request.path();

        if (error instanceof DomainException domainException) {
            HttpStatus status = resolverEstado(domainException);
            log.warn("Error de dominio [{}] en {}: {}",
                    domainException.getCodigo(), path, domainException.getMessage());
            return construirRespuesta(status, domainException.getCodigo(),
                    domainException.getMessage(), null, path);
        }

        if (error instanceof ValidationException validationException) {
            log.warn("Error de validacion en {}: {}", path, validationException.getDetalles());
            return construirRespuesta(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "La peticion contiene datos invalidos",
                    validationException.getDetalles(), path);
        }

        log.error("Error no controlado en {}", path, error);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrio un error inesperado", null, path);
    }

    private HttpStatus resolverEstado(DomainException exception) {
        if (exception instanceof DuplicateResourceException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof InvalidStockException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.NOT_FOUND;
    }

    private Map<String, Object> construirRespuesta(HttpStatus status, String codigo,
                                                   String mensaje, List<String> detalles,
                                                   String path) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "codigo", codigo,
                "mensaje", mensaje,
                "detalles", detalles == null ? List.of() : detalles,
                "path", path);
    }
}