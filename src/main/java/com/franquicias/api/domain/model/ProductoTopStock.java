package com.franquicias.api.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductoTopStock {

	
	String sucursalId;
    String sucursalNombre;
    String productoId;
    String productoNombre;
    Integer stock;
}
