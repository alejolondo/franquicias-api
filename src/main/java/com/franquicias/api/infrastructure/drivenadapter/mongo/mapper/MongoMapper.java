package com.franquicias.api.infrastructure.drivenadapter.mongo.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.model.Sucursal;
import com.franquicias.api.infrastructure.drivenadapter.mongo.document.FranquiciaDocument;
import com.franquicias.api.infrastructure.drivenadapter.mongo.document.ProductoDocument;
import com.franquicias.api.infrastructure.drivenadapter.mongo.document.SucursalDocument;

@Component
public class MongoMapper {

    public FranquiciaDocument aDocumento(Franquicia franquicia) {
        return FranquiciaDocument.builder()
                .id(franquicia.getId())
                .nombre(franquicia.getNombre())
                .sucursales(franquicia.getSucursales().stream()
                        .map(this::aDocumento)
                        .toList())
                .build();
    }

    public Franquicia aDominio(FranquiciaDocument documento) {
        return Franquicia.builder()
                .id(documento.getId())
                .nombre(documento.getNombre())
                .sucursales(listaSegura(documento.getSucursales()).stream()
                        .map(this::aDominio)
                        .toList())
                .build();
    }

    private SucursalDocument aDocumento(Sucursal sucursal) {
        return SucursalDocument.builder()
                .id(sucursal.getId())
                .nombre(sucursal.getNombre())
                .productos(sucursal.getProductos().stream()
                        .map(this::aDocumento)
                        .toList())
                .build();
    }

    private Sucursal aDominio(SucursalDocument documento) {
        return Sucursal.builder()
                .id(documento.getId())
                .nombre(documento.getNombre())
                .productos(listaSegura(documento.getProductos()).stream()
                        .map(this::aDominio)
                        .toList())
                .build();
    }

    private ProductoDocument aDocumento(Producto producto) {
        return ProductoDocument.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .stock(producto.getStock())
                .build();
    }

    private Producto aDominio(ProductoDocument documento) {
        return Producto.builder()
                .id(documento.getId())
                .nombre(documento.getNombre())
                .stock(documento.getStock())
                .build();
    }

    private <T> List<T> listaSegura(List<T> lista) {
        return lista == null ? List.of() : lista;
    }
}