package com.franquicias.api.infrastructure.entrypoint.rest;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.franquicias.api.domain.usercase.FranquiciaUseCase;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.ActualizarNombreRequest;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.CrearFranquiciaRequest;
import com.franquicias.api.infrastructure.entrypoint.rest.mapper.RequestValidator;
import com.franquicias.api.infrastructure.entrypoint.rest.mapper.RestMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranquiciaHandler {

    private final FranquiciaUseCase franquiciaUseCase;
    private final RestMapper mapper;
    private final RequestValidator validator;

    public Mono<ServerResponse> crear(ServerRequest request) {
        return request.bodyToMono(CrearFranquiciaRequest.class)
                .map(validator::validar)
                .flatMap(body -> franquiciaUseCase.crear(body.getNombre()))
                .map(mapper::aRespuesta)
                .flatMap(respuesta -> ServerResponse
                        .created(URI.create("/api/v1/franquicias/" + respuesta.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(respuesta));
    }

    public Mono<ServerResponse> actualizarNombre(ServerRequest request) {
        String franquiciaId = request.pathVariable("franquiciaId");

        return request.bodyToMono(ActualizarNombreRequest.class)
                .map(validator::validar)
                .flatMap(body -> franquiciaUseCase.actualizarNombre(franquiciaId, body.getNombre()))
                .map(mapper::aRespuesta)
                .flatMap(this::responderOk);
    }

    public Mono<ServerResponse> buscarPorId(ServerRequest request) {
        return franquiciaUseCase.buscarPorId(request.pathVariable("franquiciaId"))
                .map(mapper::aRespuesta)
                .flatMap(this::responderOk);
    }

    public Mono<ServerResponse> listar(ServerRequest request) {
        return franquiciaUseCase.listar()
                .map(mapper::aRespuesta)
                .collectList()
                .flatMap(this::responderOk);
    }

    public Mono<ServerResponse> productosConMayorStock(ServerRequest request) {
        return franquiciaUseCase.productosConMayorStock(request.pathVariable("franquiciaId"))
                .map(mapper::aRespuestaTopStock)
                .flatMap(this::responderOk);
    }

    private Mono<ServerResponse> responderOk(Object cuerpo) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo);
    }
}