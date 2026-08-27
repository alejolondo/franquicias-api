package com.franquicias.api.infrastructure.drivenadapter.mongo.document;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "franquicias")
public class FranquiciaDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nombre;

    private List<SucursalDocument> sucursales;
}