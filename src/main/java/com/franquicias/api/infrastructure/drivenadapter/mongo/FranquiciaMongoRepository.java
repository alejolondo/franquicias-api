package com.franquicias.api.infrastructure.drivenadapter.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.franquicias.api.infrastructure.drivenadapter.mongo.document.FranquiciaDocument;

import reactor.core.publisher.Mono;

@Repository
public interface FranquiciaMongoRepository
        extends ReactiveMongoRepository<FranquiciaDocument, String> {

    Mono<Boolean> existsByNombreIgnoreCase(String nombre);
}