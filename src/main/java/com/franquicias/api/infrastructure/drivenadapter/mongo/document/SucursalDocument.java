package com.franquicias.api.infrastructure.drivenadapter.mongo.document;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SucursalDocument {

    private String id;
    private String nombre;
    private List<ProductoDocument> productos;
}