package com.franquicias.api.usecase;
import java.util.function.UnaryOperator;

import com.franquicias.api.domain.exception.FranquiciaNotFoundException;
import com.franquicias.api.domain.exception.ProductoNotFoundException;
import com.franquicias.api.domain.exception.SucursalNotFoundException;
import com.franquicias.api.domain.gateway.FranquiciaRepository;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.model.Sucursal;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ProductoUseCase {
	
	private final FranquiciaRepository franquiciaRepository;

    public Mono<Franquicia> agregar(String franquiciaId, String sucursalId,
                                    String nombre, Integer stock) {
        return aplicarSobreSucursal(franquiciaId, sucursalId,
                sucursal -> sucursal.agregarProducto(Producto.crear(nombre, stock)));
    }

    public Mono<Franquicia> eliminar(String franquiciaId, String sucursalId, String productoId) {
        return aplicarSobreSucursal(franquiciaId, sucursalId,
                sucursal -> sucursal.eliminarProducto(productoId));
    }

    public Mono<Franquicia> actualizarStock(String franquiciaId, String sucursalId,
                                            String productoId, Integer nuevoStock) {
        return aplicarSobreSucursal(franquiciaId, sucursalId, sucursal -> {
            Producto producto = obtenerProducto(sucursal, productoId);
            return sucursal.reemplazarProducto(producto.conStock(nuevoStock));
        });
    }

    public Mono<Franquicia> actualizarNombre(String franquiciaId, String sucursalId,
                                             String productoId, String nuevoNombre) {
        return aplicarSobreSucursal(franquiciaId, sucursalId, sucursal -> {
            Producto producto = obtenerProducto(sucursal, productoId);
            return sucursal.reemplazarProducto(producto.conNombre(nuevoNombre));
        });
    }

    private Mono<Franquicia> aplicarSobreSucursal(String franquiciaId, String sucursalId,
                                                  UnaryOperator<Sucursal> operacion) {
        return franquiciaRepository.buscarPorId(franquiciaId)
                .switchIfEmpty(Mono.error(new FranquiciaNotFoundException(franquiciaId)))
                .map(franquicia -> {
                    Sucursal sucursal = franquicia.buscarSucursal(sucursalId)
                            .orElseThrow(() -> new SucursalNotFoundException(sucursalId));
                    return franquicia.reemplazarSucursal(operacion.apply(sucursal));
                })
                .flatMap(franquiciaRepository::guardar);
    }

    private Producto obtenerProducto(Sucursal sucursal, String productoId) {
        return sucursal.buscarProducto(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
    }
	

}
