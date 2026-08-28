package com.franquicias.api.domain.usercase;

import java.util.List;

import com.franquicias.api.domain.exception.DuplicateResourceException;
import com.franquicias.api.domain.exception.FranquiciaNotFoundException;
import com.franquicias.api.domain.gateway.FranquiciaRepository;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.ProductoTopStock;


import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class FranquiciaUseCase {
	
	
	private final FranquiciaRepository franquiciaRepository;
	
	public Mono<Franquicia> crear(String nombre) {
        return franquiciaRepository.existePorNombre(nombre)
                .defaultIfEmpty(false)
                .flatMap(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        return Mono.error(new DuplicateResourceException("franquicia", nombre));
                    }
                    return franquiciaRepository.guardar(Franquicia.crear(nombre));
                });
    }

    public Mono<Franquicia> buscarPorId(String franquiciaId) {
        return franquiciaRepository.buscarPorId(franquiciaId)
                .switchIfEmpty(Mono.error(new FranquiciaNotFoundException(franquiciaId)));
    }

    public Mono<Franquicia> actualizarNombre(String franquiciaId, String nuevoNombre) {
        return buscarPorId(franquiciaId)
                .map(franquicia -> franquicia.conNombre(nuevoNombre))
                .flatMap(franquiciaRepository::guardar);
    }

    public Flux<Franquicia> listar() {
        return franquiciaRepository.listarTodas();
    }

    public Mono<List<ProductoTopStock>> productosConMayorStock(String franquiciaId) {
        return buscarPorId(franquiciaId)
                .map(Franquicia::productosConMayorStockPorSucursal);
    }

}
