package com.franquicias.api.domain.gateway;

import com.franquicias.api.domain.model.Franquicia;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranquiciaRepository {

	Mono<Franquicia> guardar(Franquicia franquicia);

    Mono<Franquicia> buscarPorId(String id);

    Mono<Boolean> existePorNombre(String nombre);

    Flux<Franquicia> listarTodas();
}
