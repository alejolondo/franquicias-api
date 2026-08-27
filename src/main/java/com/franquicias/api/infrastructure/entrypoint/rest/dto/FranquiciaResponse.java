package com.franquicias.api.infrastructure.entrypoint.rest.dto;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FranquiciaResponse {

    String id;
    String nombre;
    List<SucursalResponse> sucursales;
}