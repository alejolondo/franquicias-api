package com.franquicias.api.infrastructure.entrypoint.rest;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterRest {

    private static final String BASE = "/api/v1/franquicias";
    private static final String POR_ID = BASE + "/{franquiciaId}";
    private static final String SUCURSALES = POR_ID + "/sucursales";
    private static final String SUCURSAL_POR_ID = SUCURSALES + "/{sucursalId}";
    private static final String PRODUCTOS = SUCURSAL_POR_ID + "/productos";
    private static final String PRODUCTO_POR_ID = PRODUCTOS + "/{productoId}";

    @Bean
    public RouterFunction<ServerResponse> rutas(FranquiciaHandler franquiciaHandler,
                                                SucursalHandler sucursalHandler,
                                                ProductoHandler productoHandler) {
        return route(POST(BASE), franquiciaHandler::crear)
                .andRoute(GET(BASE), franquiciaHandler::listar)
                .andRoute(GET(POR_ID + "/productos/mayor-stock"),
                        franquiciaHandler::productosConMayorStock)
                .andRoute(GET(POR_ID), franquiciaHandler::buscarPorId)
                .andRoute(PATCH(POR_ID + "/nombre"), franquiciaHandler::actualizarNombre)

                .andRoute(POST(SUCURSALES), sucursalHandler::agregar)
                .andRoute(PATCH(SUCURSAL_POR_ID + "/nombre"), sucursalHandler::actualizarNombre)

                .andRoute(POST(PRODUCTOS), productoHandler::agregar)
                .andRoute(DELETE(PRODUCTO_POR_ID), productoHandler::eliminar)
                .andRoute(PATCH(PRODUCTO_POR_ID + "/stock"), productoHandler::actualizarStock)
                .andRoute(PATCH(PRODUCTO_POR_ID + "/nombre"), productoHandler::actualizarNombre);
    }
}