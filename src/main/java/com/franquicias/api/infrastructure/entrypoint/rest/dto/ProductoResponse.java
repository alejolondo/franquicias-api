package com.franquicias.api.infrastructure.entrypoint.rest.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductoResponse {

    String id;
    String nombre;
    Integer stock;
}