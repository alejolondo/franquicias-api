package com.franquicias.api.infrastructure.drivenadapter.mongo;

import org.springframework.stereotype.Repository;

import com.franquicias.api.domain.gateway.FranquiciaRepository;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.infrastructure.drivenadapter.mongo.mapper.MongoMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class FranquiciaRepositoryAdapter implements FranquiciaRepository {

    private final FranquiciaMongoRepository mongoRepository;
    private final MongoMapper mapper;

    @Override
    public Mono<Franquicia> guardar(Franquicia franquicia) {
        return mongoRepository.save(mapper.aDocumento(franquicia))
                .map(mapper::aDominio);
    }

    @Override
    public Mono<Franquicia> buscarPorId(String id) {
        return mongoRepository.findById(id)
                .map(mapper::aDominio);
    }

    @Override
    public Mono<Boolean> existePorNombre(String nombre) {
        return mongoRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public Flux<Franquicia> listarTodas() {
        return mongoRepository.findAll()
                .map(mapper::aDominio);
    }
}