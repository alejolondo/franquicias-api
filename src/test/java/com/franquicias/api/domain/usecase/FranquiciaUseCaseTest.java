package com.franquicias.api.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.franquicias.api.domain.exception.DuplicateResourceException;
import com.franquicias.api.domain.exception.FranquiciaNotFoundException;
import com.franquicias.api.domain.gateway.FranquiciaRepository;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.model.Sucursal;
import com.franquicias.api.domain.usercase.FranquiciaUseCase;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("FranquiciaUseCase")
class FranquiciaUseCaseTest {

    @Mock
    private FranquiciaRepository franquiciaRepository;

    @InjectMocks
    private FranquiciaUseCase franquiciaUseCase;

    private Franquicia franquicia;

    @BeforeEach
    void prepararDatos() {
        franquicia = Franquicia.crear("Mi Franquicia");
    }

    @Nested
    @DisplayName("al crear una franquicia")
    class Crear {

        @Test
        @DisplayName("la guarda cuando el nombre no existe")
        void guardaCuandoNoExiste() {
            when(franquiciaRepository.existePorNombre("Nueva")).thenReturn(Mono.just(false));
            when(franquiciaRepository.guardar(any(Franquicia.class)))
                    .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));

            StepVerifier.create(franquiciaUseCase.crear("Nueva"))
                    .assertNext(resultado -> {
                        assertThat(resultado.getNombre()).isEqualTo("Nueva");
                        assertThat(resultado.getId()).isNotBlank();
                        assertThat(resultado.getSucursales()).isEmpty();
                    })
                    .verifyComplete();

            verify(franquiciaRepository).guardar(any(Franquicia.class));
        }

        @Test
        @DisplayName("falla con DuplicateResourceException si el nombre ya existe")
        void fallaSiElNombreExiste() {
            when(franquiciaRepository.existePorNombre("Repetida")).thenReturn(Mono.just(true));

            StepVerifier.create(franquiciaUseCase.crear("Repetida"))
                    .expectError(DuplicateResourceException.class)
                    .verify();

            verify(franquiciaRepository, never()).guardar(any());
        }

        @Test
        @DisplayName("trata un Mono vacio como nombre disponible")
        void trataVacioComoDisponible() {
            when(franquiciaRepository.existePorNombre("Nueva")).thenReturn(Mono.empty());
            when(franquiciaRepository.guardar(any(Franquicia.class)))
                    .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));

            StepVerifier.create(franquiciaUseCase.crear("Nueva"))
                    .expectNextCount(1)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("al buscar por id")
    class BuscarPorId {

        @Test
        @DisplayName("devuelve la franquicia cuando existe")
        void devuelveLaFranquicia() {
            when(franquiciaRepository.buscarPorId(franquicia.getId()))
                    .thenReturn(Mono.just(franquicia));

            StepVerifier.create(franquiciaUseCase.buscarPorId(franquicia.getId()))
                    .expectNext(franquicia)
                    .verifyComplete();
        }

        @Test
        @DisplayName("falla con FranquiciaNotFoundException cuando no existe")
        void fallaSiNoExiste() {
            when(franquiciaRepository.buscarPorId("inexistente")).thenReturn(Mono.empty());

            StepVerifier.create(franquiciaUseCase.buscarPorId("inexistente"))
                    .expectError(FranquiciaNotFoundException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("al actualizar el nombre")
    class ActualizarNombre {

        @Test
        @DisplayName("guarda la franquicia con el nuevo nombre")
        void actualizaElNombre() {
            when(franquiciaRepository.buscarPorId(franquicia.getId()))
                    .thenReturn(Mono.just(franquicia));
            when(franquiciaRepository.guardar(any(Franquicia.class)))
                    .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));

            StepVerifier.create(franquiciaUseCase.actualizarNombre(franquicia.getId(), "Renombrada"))
                    .assertNext(resultado -> {
                        assertThat(resultado.getNombre()).isEqualTo("Renombrada");
                        assertThat(resultado.getId()).isEqualTo(franquicia.getId());
                    })
                    .verifyComplete();

            ArgumentCaptor<Franquicia> captor = ArgumentCaptor.forClass(Franquicia.class);
            verify(franquiciaRepository).guardar(captor.capture());
            assertThat(captor.getValue().getNombre()).isEqualTo("Renombrada");
        }

        @Test
        @DisplayName("falla si la franquicia no existe y no guarda nada")
        void fallaSiNoExiste() {
            when(franquiciaRepository.buscarPorId(anyString())).thenReturn(Mono.empty());

            StepVerifier.create(franquiciaUseCase.actualizarNombre("inexistente", "Nombre"))
                    .expectError(FranquiciaNotFoundException.class)
                    .verify();

            verify(franquiciaRepository, never()).guardar(any());
        }
    }

    @Nested
    @DisplayName("al consultar productos con mayor stock")
    class ProductosConMayorStock {

        @Test
        @DisplayName("devuelve un producto por cada sucursal con productos")
        void devuelveElTopPorSucursal() {
            Sucursal centro = Sucursal.crear("Centro")
                    .agregarProducto(Producto.crear("Cafe", 50))
                    .agregarProducto(Producto.crear("Te", 120));
            Franquicia conSucursal = franquicia.agregarSucursal(centro);

            when(franquiciaRepository.buscarPorId(conSucursal.getId()))
                    .thenReturn(Mono.just(conSucursal));

            StepVerifier.create(franquiciaUseCase.productosConMayorStock(conSucursal.getId()))
                    .assertNext(lista -> {
                        assertThat(lista).hasSize(1);
                        assertThat(lista.get(0).getProductoNombre()).isEqualTo("Te");
                        assertThat(lista.get(0).getStock()).isEqualTo(120);
                        assertThat(lista.get(0).getSucursalNombre()).isEqualTo("Centro");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("falla si la franquicia no existe")
        void fallaSiNoExiste() {
            when(franquiciaRepository.buscarPorId("inexistente")).thenReturn(Mono.empty());

            StepVerifier.create(franquiciaUseCase.productosConMayorStock("inexistente"))
                    .expectError(FranquiciaNotFoundException.class)
                    .verify();
        }
    }

    @Test
    @DisplayName("listar delega en el repositorio")
    void listarDelega() {
        when(franquiciaRepository.listarTodas()).thenReturn(Flux.just(franquicia));

        StepVerifier.create(franquiciaUseCase.listar())
                .expectNext(franquicia)
                .verifyComplete();

        verify(franquiciaRepository).listarTodas();
    }
}