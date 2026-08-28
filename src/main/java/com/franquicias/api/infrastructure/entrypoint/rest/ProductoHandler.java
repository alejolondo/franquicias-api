package com.franquicias.api.infrastructure.entrypoint.rest;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.franquicias.api.domain.usercase.ProductoUseCase;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.ActualizarNombreRequest;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.ActualizarStockRequest;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.CrearProductoRequest;
import com.franquicias.api.infrastructure.entrypoint.rest.mapper.RequestValidator;
import com.franquicias.api.infrastructure.entrypoint.rest.mapper.RestMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductoHandler {

    private final ProductoUseCase productoUseCase;
    private final RestMapper mapper;
    private final RequestValidator validator;

    public Mono<ServerResponse> agregar(ServerRequest request) {
        String franquiciaId = request.pathVariable("franquiciaId");
        String sucursalId = request.pathVariable("sucursalId");

        return request.bodyToMono(CrearProductoRequest.class)
                .map(validator::validar)
                .flatMap(body -> productoUseCase.agregar(
                        franquiciaId, sucursalId, body.getNombre(), body.getStock()))
                .map(mapper::aRespuesta)
                .flatMap(respuesta -> ServerResponse.status(201)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(respuesta));
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        return productoUseCase.eliminar(
                        request.pathVariable("franquiciaId"),
                        request.pathVariable("sucursalId"),
                        request.pathVariable("productoId"))
                .map(mapper::aRespuesta)
                .flatMap(this::responderOk);
    }

    public Mono<ServerResponse> actualizarStock(ServerRequest request) {
        String franquiciaId = request.pathVariable("franquiciaId");
        String sucursalId = request.pathVariable("sucursalId");
        String productoId = request.pathVariable("productoId");

        return request.bodyToMono(ActualizarStockRequest.class)
                .map(validator::validar)
                .flatMap(body -> productoUseCase.actualizarStock(
                        franquiciaId, sucursalId, productoId, body.getStock()))
                .map(mapper::aRespuesta)
                .flatMap(this::responderOk);
    }

    public Mono<ServerResponse> actualizarNombre(ServerRequest request) {
        String franquiciaId = request.pathVariable("franquiciaId");
        String sucursalId = request.pathVariable("sucursalId");
        String productoId = request.pathVariable("productoId");

        return request.bodyToMono(ActualizarNombreRequest.class)
                .map(validator::validar)
                .flatMap(body -> productoUseCase.actualizarNombre(
                        franquiciaId, sucursalId, productoId, body.getNombre()))
                .map(mapper::aRespuesta)
                .flatMap(this::responderOk);
    }

    private Mono<ServerResponse> responderOk(Object cuerpo) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo);
    }
}