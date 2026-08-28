package com.franquicias.api.infrastructure.entrypoint.rest.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.model.ProductoTopStock;
import com.franquicias.api.domain.model.Sucursal;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.FranquiciaResponse;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.ProductoResponse;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.ProductoTopStockResponse;
import com.franquicias.api.infrastructure.entrypoint.rest.dto.SucursalResponse;

@Component
public class RestMapper {

    public FranquiciaResponse aRespuesta(Franquicia franquicia) {
        return FranquiciaResponse.builder()
                .id(franquicia.getId())
                .nombre(franquicia.getNombre())
                .sucursales(franquicia.getSucursales().stream()
                        .map(this::aRespuesta)
                        .toList())
                .build();
    }

    public List<ProductoTopStockResponse> aRespuestaTopStock(List<ProductoTopStock> productos) {
        return productos.stream()
                .map(producto -> ProductoTopStockResponse.builder()
                        .sucursalId(producto.getSucursalId())
                        .sucursalNombre(producto.getSucursalNombre())
                        .productoId(producto.getProductoId())
                        .productoNombre(producto.getProductoNombre())
                        .stock(producto.getStock())
                        .build())
                .toList();
    }

    private SucursalResponse aRespuesta(Sucursal sucursal) {
        return SucursalResponse.builder()
                .id(sucursal.getId())
                .nombre(sucursal.getNombre())
                .productos(sucursal.getProductos().stream()
                        .map(this::aRespuesta)
                        .toList())
                .build();
    }

    private ProductoResponse aRespuesta(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .stock(producto.getStock())
                .build();
    }
}