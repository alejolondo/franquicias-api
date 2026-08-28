package com.franquicias.api.infrastructure.entrypoint.rest.dto;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {

    Instant timestamp;
    int status;
    String codigo;
    String mensaje;
    List<String> detalles;
    String path;
}