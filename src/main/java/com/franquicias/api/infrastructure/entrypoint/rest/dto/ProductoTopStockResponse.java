package com.franquicias.api.infrastructure.entrypoint.rest.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductoTopStockResponse {

    String sucursalId;
    String sucursalNombre;
    String productoId;
    String productoNombre;
    Integer stock;
}