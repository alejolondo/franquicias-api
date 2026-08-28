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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.franquicias.api.domain.exception.DuplicateResourceException;
import com.franquicias.api.domain.exception.FranquiciaNotFoundException;
import com.franquicias.api.domain.exception.InvalidStockException;
import com.franquicias.api.domain.exception.ProductoNotFoundException;
import com.franquicias.api.domain.exception.SucursalNotFoundException;
import com.franquicias.api.domain.gateway.FranquiciaRepository;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.model.Sucursal;
import com.franquicias.api.domain.usercase.ProductoUseCase;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoUseCase")
class ProductoUseCaseTest {

    @Mock
    private FranquiciaRepository franquiciaRepository;

    @InjectMocks
    private ProductoUseCase productoUseCase;

    private Franquicia franquicia;
    private Sucursal sucursal;
    private Producto cafe;

    @BeforeEach
    void prepararDatos() {
        cafe = Producto.crear("Cafe", 50);
        sucursal = Sucursal.crear("Centro").agregarProducto(cafe);
        franquicia = Franquicia.crear("Mi Franquicia").agregarSucursal(sucursal);
    }

    private void simularBusquedaYGuardado() {
        when(franquiciaRepository.buscarPorId(franquicia.getId()))
                .thenReturn(Mono.just(franquicia));
        when(franquiciaRepository.guardar(any(Franquicia.class)))
                .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));
    }

    private void simularSoloBusqueda() {
        when(franquiciaRepository.buscarPorId(franquicia.getId()))
                .thenReturn(Mono.just(franquicia));
    }

    @Nested
    @DisplayName("al agregar un producto")
    class Agregar {

        @Test
        @DisplayName("lo incorpora a la sucursal indicada")
        void agregaProducto() {
            simularBusquedaYGuardado();

            StepVerifier.create(productoUseCase.agregar(
                            franquicia.getId(), sucursal.getId(), "Te", 120))
                    .assertNext(resultado -> {
                        assertThat(resultado.getSucursales().get(0).getProductos()).hasSize(2);
                        assertThat(resultado.getSucursales().get(0).getProductos())
                                .extracting(Producto::getNombre)
                                .containsExactly("Cafe", "Te");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("falla si el producto ya existe en la sucursal")
        void fallaSiEstaDuplicado() {
            simularSoloBusqueda();

            StepVerifier.create(productoUseCase.agregar(
                            franquicia.getId(), sucursal.getId(), "Cafe", 10))
                    .expectError(DuplicateResourceException.class)
                    .verify();

            verify(franquiciaRepository, never()).guardar(any());
        }

        @Test
        @DisplayName("falla si el stock es negativo")
        void fallaConStockNegativo() {
            simularSoloBusqueda();

            StepVerifier.create(productoUseCase.agregar(
                            franquicia.getId(), sucursal.getId(), "Te", -1))
                    .expectError(InvalidStockException.class)
                    .verify();
        }

        @Test
        @DisplayName("falla si la sucursal no existe")
        void fallaSiLaSucursalNoExiste() {
            simularSoloBusqueda();

            StepVerifier.create(productoUseCase.agregar(
                            franquicia.getId(), "sucursal-inexistente", "Te", 10))
                    .expectError(SucursalNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("falla si la franquicia no existe")
        void fallaSiLaFranquiciaNoExiste() {
            when(franquiciaRepository.buscarPorId(anyString())).thenReturn(Mono.empty());

            StepVerifier.create(productoUseCase.agregar(
                            "inexistente", sucursal.getId(), "Te", 10))
                    .expectError(FranquiciaNotFoundException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("al eliminar un producto")
    class Eliminar {

        @Test
        @DisplayName("lo quita de la sucursal")
        void eliminaProducto() {
            simularBusquedaYGuardado();

            StepVerifier.create(productoUseCase.eliminar(
                            franquicia.getId(), sucursal.getId(), cafe.getId()))
                    .assertNext(resultado ->
                            assertThat(resultado.getSucursales().get(0).getProductos()).isEmpty())
                    .verifyComplete();
        }

        @Test
        @DisplayName("falla si el producto no existe")
        void fallaSiNoExiste() {
            simularSoloBusqueda();

            StepVerifier.create(productoUseCase.eliminar(
                            franquicia.getId(), sucursal.getId(), "inexistente"))
                    .expectError(ProductoNotFoundException.class)
                    .verify();

            verify(franquiciaRepository, never()).guardar(any());
        }
    }

    @Nested
    @DisplayName("al actualizar el stock")
    class ActualizarStock {

        @Test
        @DisplayName("cambia el stock conservando id y nombre")
        void actualizaStock() {
            simularBusquedaYGuardado();

            StepVerifier.create(productoUseCase.actualizarStock(
                            franquicia.getId(), sucursal.getId(), cafe.getId(), 999))
                    .assertNext(resultado -> {
                        Producto actualizado = resultado.getSucursales().get(0).getProductos().get(0);
                        assertThat(actualizado.getStock()).isEqualTo(999);
                        assertThat(actualizado.getId()).isEqualTo(cafe.getId());
                        assertThat(actualizado.getNombre()).isEqualTo("Cafe");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("permite stock cero")
        void permiteStockCero() {
            simularBusquedaYGuardado();

            StepVerifier.create(productoUseCase.actualizarStock(
                            franquicia.getId(), sucursal.getId(), cafe.getId(), 0))
                    .assertNext(resultado -> assertThat(
                            resultado.getSucursales().get(0).getProductos().get(0).getStock())
                            .isZero())
                    .verifyComplete();
        }

        @Test
        @DisplayName("rechaza stock negativo")
        void rechazaStockNegativo() {
            simularSoloBusqueda();

            StepVerifier.create(productoUseCase.actualizarStock(
                            franquicia.getId(), sucursal.getId(), cafe.getId(), -10))
                    .expectError(InvalidStockException.class)
                    .verify();

            verify(franquiciaRepository, never()).guardar(any());
        }

        @Test
        @DisplayName("falla si el producto no existe")
        void fallaSiElProductoNoExiste() {
            simularSoloBusqueda();

            StepVerifier.create(productoUseCase.actualizarStock(
                            franquicia.getId(), sucursal.getId(), "inexistente", 10))
                    .expectError(ProductoNotFoundException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("al actualizar el nombre del producto")
    class ActualizarNombre {

        @Test
        @DisplayName("cambia el nombre conservando id y stock")
        void actualizaNombre() {
            simularBusquedaYGuardado();

            StepVerifier.create(productoUseCase.actualizarNombre(
                            franquicia.getId(), sucursal.getId(), cafe.getId(), "Cafe Premium"))
                    .assertNext(resultado -> {
                        Producto actualizado = resultado.getSucursales().get(0).getProductos().get(0);
                        assertThat(actualizado.getNombre()).isEqualTo("Cafe Premium");
                        assertThat(actualizado.getId()).isEqualTo(cafe.getId());
                        assertThat(actualizado.getStock()).isEqualTo(50);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("falla si el producto no existe")
        void fallaSiNoExiste() {
            simularSoloBusqueda();

            StepVerifier.create(productoUseCase.actualizarNombre(
                            franquicia.getId(), sucursal.getId(), "inexistente", "Nuevo"))
                    .expectError(ProductoNotFoundException.class)
                    .verify();
        }
    }
}