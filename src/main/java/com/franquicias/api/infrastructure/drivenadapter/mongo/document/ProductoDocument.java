package com.franquicias.api.infrastructure.drivenadapter.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDocument {

    private String id;
    private String nombre;
    private Integer stock;
}