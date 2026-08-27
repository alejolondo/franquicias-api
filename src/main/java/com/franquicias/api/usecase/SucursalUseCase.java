package com.franquicias.api.usecase;

import com.franquicias.api.domain.exception.FranquiciaNotFoundException;
import com.franquicias.api.domain.exception.SucursalNotFoundException;
import com.franquicias.api.domain.gateway.FranquiciaRepository;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Sucursal;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SucursalUseCase {
	
	private final FranquiciaRepository franquiciaRepository;
	
	
	public Mono<Franquicia> agregar(String franquiciaId, String nombreSucursal) {
        return obtenerFranquicia(franquiciaId)
                .map(franquicia -> franquicia.agregarSucursal(Sucursal.crear(nombreSucursal)))
                .flatMap(franquiciaRepository::guardar);
    }

    public Mono<Franquicia> actualizarNombre(String franquiciaId, String sucursalId, String nuevoNombre) {
        return obtenerFranquicia(franquiciaId)
                .map(franquicia -> {
                    Sucursal sucursal = franquicia.buscarSucursal(sucursalId)
                            .orElseThrow(() -> new SucursalNotFoundException(sucursalId));
                    return franquicia.reemplazarSucursal(sucursal.conNombre(nuevoNombre));
                })
                .flatMap(franquiciaRepository::guardar);
    }

    private Mono<Franquicia> obtenerFranquicia(String franquiciaId) {
        return franquiciaRepository.buscarPorId(franquiciaId)
                .switchIfEmpty(Mono.error(new FranquiciaNotFoundException(franquiciaId)));
    }

}
