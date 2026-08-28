package com.franquicias.api.infrastructure.entrypoint.rest;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.franquicias.api.infrastructure.entrypoint.rest.dto.ActualizarNombreRequest;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.CrearSucursalRequest;
import com.franquicias.api.infrastructure.entrypoint.rest.mapper.RequestValidator;
import com.franquicias.api.infrastructure.entrypoint.rest.mapper.RestMapper;
import com.franquicias.api.usecase.SucursalUseCase;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SucursalHandler {

    private final SucursalUseCase sucursalUseCase;
    private final RestMapper mapper;
    private final RequestValidator validator;

    public Mono<ServerResponse> agregar(ServerRequest request) {
        String franquiciaId = request.pathVariable("franquiciaId");

        return request.bodyToMono(CrearSucursalRequest.class)
                .map(validator::validar)
                .flatMap(body -> sucursalUseCase.agregar(franquiciaId, body.getNombre()))
                .map(mapper::aRespuesta)
                .flatMap(respuesta -> ServerResponse.status(201)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(respuesta));
    }

    public Mono<ServerResponse> actualizarNombre(ServerRequest request) {
        String franquiciaId = request.pathVariable("franquiciaId");
        String sucursalId = request.pathVariable("sucursalId");

        return request.bodyToMono(ActualizarNombreRequest.class)
                .map(validator::validar)
                .flatMap(body -> sucursalUseCase.actualizarNombre(
                        franquiciaId, sucursalId, body.getNombre()))
                .map(mapper::aRespuesta)
                .flatMap(respuesta -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(respuesta));
    }
}